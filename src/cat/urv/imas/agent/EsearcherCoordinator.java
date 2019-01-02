package cat.urv.imas.agent;

import cat.urv.imas.behaviour.coordinator.CoordinatorRequestActionsBehaviour;
import cat.urv.imas.behaviour.coordinator.CoordinatorRequestUpdatesBehaviour;
import cat.urv.imas.behaviour.coordinator.CoordinatorResponseActionsBehaviour;
import cat.urv.imas.behaviour.coordinator.CoordinatorResponseUpdatesBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.List;

public class EsearcherCoordinator extends ImasAgentTuned {
    
    public EsearcherCoordinator() {
        super( AgentType.ESEARCHER_COORDINATOR );
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
        
        List<Cell> searchers = gameSettings.getAgentList().get( AgentType.SEARCHER );
        for ( int i = 0; i < searchers.size(); ++i ){
            String name = "seag" + i;
            AID searcherAgent = searchAgent( AgentType.SEARCHER.toString(), name );
            addNextAgent( searcherAgent );
        }
        
        log( "Number of searchers = " + getNextAgents().size() );
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
        requestUpdate( request );
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
}
