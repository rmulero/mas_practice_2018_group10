package cat.urv.imas.agent;

import cat.urv.imas.behaviour.coordinator.CoordinatorRequestActionsBehaviour;
import cat.urv.imas.behaviour.coordinator.CoordinatorRequestCnetBehaviour;
import cat.urv.imas.behaviour.coordinator.CoordinatorRequestUpdatesBehaviour;
import cat.urv.imas.behaviour.coordinator.CoordinatorResponseActionsBehaviour;
import cat.urv.imas.behaviour.coordinator.CoordinatorResponseUpdatesBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.ActionUtils;
import cat.urv.imas.utils.actions.CollectAction;
import cat.urv.imas.utils.actions.DetectAction;
import cat.urv.imas.utils.actions.ProposeAction;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class CleanerCoordinator extends ImasAgentTuned {
    
    private final Set<Cell> detectedWastes;
    private final Set<Cell> assignedWastes;
    
    private ACLMessage bufferedUpdate;
    
    public CleanerCoordinator() {
        super( AgentType.CLEANER_COORDINATOR );
        
        this.detectedWastes = new HashSet<>();
        this.assignedWastes = new HashSet<>();
    }

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerDF();
        
        // Get initial game settings
        GameSettings settings = (GameSettings) getArguments()[0];
        setupSettings( settings );
        
        // Add previous agents (Coordinator Agent)
        AID coordinatorAgent = searchAgent( AgentType.COORDINATOR.toString() );
        addPreviousAgent( coordinatorAgent );
        
        // Wait first request
        waitForActionRequest();
        
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
    }
    
    
    @Override
    public void setupSettings( GameSettings gameSettings ) {
        
        setGameSettings( gameSettings );
        
        List<Cell> cleaners = gameSettings.getAgentList().get( AgentType.CLEANER );
        for ( int i = 0; i < cleaners.size(); ++i ){
            String name = "clag" + i;
            AID cleanerAgent = searchAgent( AgentType.CLEANER.toString(), name );
            addNextAgent( cleanerAgent );
        }
        
        log( "Number of cleaners = " + getNextAgents().size() );
    }
    
    /////////////////////////////////////
    //      COMMUNICATIONS
    /////////////////////////////////////
    private void waitForActionRequest(){
        
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.and(
                    MessageTemplate.MatchProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST ),
                    MessageTemplate.MatchPerformative( ACLMessage.REQUEST )
                ), 
                MessageTemplate.MatchContent( MessageContent.GET_ACTIONS )
        );
        
        // Add behaviour to wait for REQUEST
        addBehaviour( new CoordinatorResponseActionsBehaviour(this, template) );
    }
    
    @Override
    public void onActionsRequest( ACLMessage request ) {
        requestActions( request );
    }
    
    private void requestActions( ACLMessage request ){
        log( "Actions requested" );
        
        ACLMessage message = new ACLMessage( ACLMessage.REQUEST );
        message.setProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST );
        message.setContent( MessageContent.GET_ACTIONS );
        
        for( AID nextAgent : getNextAgents() ){
            message.addReceiver( nextAgent );
        }
        
        // Add behaviour to handle REQUEST responses
        addBehaviour( new CoordinatorRequestActionsBehaviour(this, message, request) );
    }
    
    @Override
    public void onActionsReceived( ACLMessage request, String actions ){
        
        log( "Sending INFORM (actions)" );
        
        ACLMessage inform = request.createReply();
        inform.setPerformative( ACLMessage.INFORM );
        inform.setContent( actions );
        send( inform );
        
        waitForUpdateRequest();
    }
    
    private void waitForUpdateRequest(){
        
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.and(
                    MessageTemplate.MatchProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST ),
                    MessageTemplate.MatchPerformative( ACLMessage.REQUEST )
                ),
                MessageTemplate.not(
                    MessageTemplate.MatchContent( MessageContent.GET_ACTIONS )
                )
        );
        
        // Add behaviour to wait for REQUEST
        addBehaviour( new CoordinatorResponseUpdatesBehaviour(this, template) );
    }
    
    @Override
    public void onUpdateRequest( ACLMessage request ) {
        
        bufferedUpdate = null;
        
        String content = request.getContent();
        String[] updates = content.split( ActionUtils.DELIMITER_ACTION );
        for ( String update : updates ){
            
            String[] updateParts = update.split( ActionUtils.DELIMITER_PART );
            switch( updateParts[ 1 ] ){
                // Waste detected
                case ActionUtils.ACTION_DETECT:
                    DetectAction detectAction = DetectAction.fromString( update );
                    int wasteRow = detectAction.getWasteRow();
                    int wasteCol = detectAction.getWasteCol();

                    Cell wasteCell = getGameSettings().get( wasteRow, wasteCol );
                    if ( !assignedWastes.contains( wasteCell ) ){
                        detectedWastes.add( wasteCell );
                    }
                    break;
                    
                // Waste collected
                case ActionUtils.ACTION_COLLECT_END:
                    CollectAction collectAction = CollectAction.fromString( update );
                    wasteRow = collectAction.getWasteRow();
                    wasteCol = collectAction.getWasteCol();

                    wasteCell = getGameSettings().get( wasteRow, wasteCol );
                    assignedWastes.remove( wasteCell );
                    break;
                    
                default:
                    break;
            }
        }
        
        if ( !detectedWastes.isEmpty() ){
            requestCNet();
            bufferedUpdate = request;
        } else {
            requestUpdate( request );
        }
    }
    
    private void requestUpdate( ACLMessage request ){
        log( "Update requested" );
        
        ACLMessage message = new ACLMessage( ACLMessage.REQUEST );
        message.setProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST );
        message.setContent( request.getContent() );
        
        for( AID nextAgent : getNextAgents() ){
            message.addReceiver( nextAgent );
        }
        
        // Add behaviour to handle REQUEST responses
        addBehaviour( new CoordinatorRequestUpdatesBehaviour( this, message, request ) );
    }
    
    @Override
    public void onUpdateConfirmed( ACLMessage request ){
        log( "Sending INFORM (updates)" );
        ACLMessage inform = request.createReply();
        inform.setPerformative( ACLMessage.INFORM );
        send( inform );
        
        waitForActionRequest();
    }
    
    private void requestCNet(){
        
        List<String> detections = new ArrayList<>();
        
        for ( Cell wCell : detectedWastes ){
            FieldCell fCell = (FieldCell) wCell;
            
            WasteType wType = null;
            for ( Map.Entry<WasteType, Integer> entry : fCell.getWaste().entrySet() ){
                wType = entry.getKey();
            }
            
            int amount = fCell.getWaste().get( wType );
            
            DetectAction action = new DetectAction(
                getLocalName(),
                ActionUtils.ACTION_DETECT,
                fCell.getRow() + "," + fCell.getCol(), 
                wType.getShortString(), 
                String.valueOf( amount )
            );

            detections.add( action.toString() );
        }
        
        String detectionsStr = String.join( ActionUtils.DELIMITER_ACTION, detections );
        
        ACLMessage msg = new ACLMessage( ACLMessage.CFP );
        msg.setProtocol( FIPANames.InteractionProtocol.FIPA_CONTRACT_NET );
        msg.setContent( detectionsStr );
        
        for( AID nextAgent : getNextAgents() ){
            msg.addReceiver( nextAgent );
        }
        
        addBehaviour( new CoordinatorRequestCnetBehaviour( this, msg ) );
    }
    
    @Override
    public void evaluateProposals( Vector responses, Vector acceptances ) {
        
        List<ACLMessage> proposals = new ArrayList<>();
        
        // Get the proposal messages
        for ( int i = 0; i < responses.size(); ++i ){
            ACLMessage msg = (ACLMessage) responses.get( i );
            
            if ( msg.getPerformative() == ACLMessage.PROPOSE ){
                proposals.add( msg );
            }
        }
        
        // Evaluate the best proposals
        Map<String, ACLMessage> bestProposals = new HashMap<>();
        for ( ACLMessage msg : proposals ){
            ProposeAction propose = ProposeAction.fromString( msg.getContent() );
            String position = propose.getWasteRow() + "," + propose.getWasteCol();
            
            ACLMessage previous = bestProposals.get( position );
            if ( previous == null ){
                bestProposals.put( position, msg );
            } else {
                ACLMessage winner = compareProposals( msg, previous );
                bestProposals.put( position, winner );
            }
        }
        
        // Create the replies to the proposals
        for ( String position : bestProposals.keySet() ){
            ACLMessage msg = bestProposals.get( position );
            
            ProposeAction propose = ProposeAction.fromString( msg.getContent() );
            Cell wasteCell = getGameSettings().get( propose.getWasteRow(), propose.getWasteCol() );
            detectedWastes.remove( wasteCell );
            assignedWastes.add( wasteCell );
            
            ACLMessage reply = msg.createReply();
            reply.setPerformative( ACLMessage.ACCEPT_PROPOSAL );
            reply.setContent( propose.toString() );
            acceptances.add( reply );
            
            proposals.remove( msg );
        }
        
        for ( ACLMessage msg : proposals ){
            ACLMessage reply = msg.createReply();
            reply.setPerformative( ACLMessage.REJECT_PROPOSAL );
            acceptances.add( reply );
        }
        
        if ( bufferedUpdate != null ){
            requestUpdate( bufferedUpdate );
        }
    }
    
    /////////////////////////////////////
    //      ACTIONS
    /////////////////////////////////////
    private ACLMessage compareProposals( ACLMessage msgA, ACLMessage msgB ){
        
        ProposeAction proposeA = ProposeAction.fromString( msgA.getContent() );
        ProposeAction proposeB = ProposeAction.fromString( msgB.getContent() );
        
        if ( proposeA.getCapacity() > proposeB.getCapacity() ){
            return msgA;
            
        } else if ( proposeA.getCapacity() < proposeB.getCapacity() ){
            return msgB;
            
        } else {
            if ( proposeA.getSteps() < proposeB.getSteps() ){
                return msgA;
                
            } else if ( proposeA.getSteps() > proposeB.getSteps() ){
                return msgB;
            }
        }
        
        Random rand = new Random();
        int number = rand.nextInt( 2 );
        return ( number == 0 ) ? msgA : msgB;
    }
    
}
