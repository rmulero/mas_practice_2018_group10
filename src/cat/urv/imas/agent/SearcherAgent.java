package cat.urv.imas.agent;

import cat.urv.imas.behaviour.coordinator.CoordinatorResponseActionsBehaviour;
import cat.urv.imas.behaviour.coordinator.CoordinatorResponseUpdatesBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.List;

public class SearcherAgent extends ImasAgentTuned {
    
    private int maxSteps = 0;
    private int currentStep = 0;
    private Cell position;
    private final List<Cell> chargingPoints;
    
    public SearcherAgent() {
        super( AgentType.SEARCHER );
        this.chargingPoints = new ArrayList<>();
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
        
        // Add previous agents (Searcher Coordinator)
        AID searcherCoordinator = searchAgent( AgentType.ESEARCHER_COORDINATOR.toString() );
        addPreviousAgent( searcherCoordinator );
        
        // Wait first request
        waitForActionRequest();
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
    }
    
    @Override
    public void setupSettings(GameSettings gameSettings) {
        // get autonomy
        this.maxSteps = gameSettings.geteSearcherMaxSteps();
        
        // get charging points
        List<Cell> recycle = gameSettings.getCellsOfType().get( CellType.BATTERIES_CHARGE_POINT );
        this.chargingPoints.addAll( recycle );
        
        // get agent position
        List<Cell> agents = gameSettings.getAgentList().get( this.type );
        String localName = getLocalName();
        localName = localName.replace("seag", "");
        int index = Integer.valueOf( localName );
        this.position = agents.get( index );
        
        log( "Position [" + position.getRow() + "," + position.getCol() + "]");
        log( "Autonomy (" + maxSteps + ")" );
    }
    
    /*******  Communications  ********/
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
        
    }
}
