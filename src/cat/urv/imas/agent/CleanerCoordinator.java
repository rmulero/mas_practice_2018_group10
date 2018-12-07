package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import jade.core.AID;
import java.util.List;

public class CleanerCoordinator extends ImasAgentTuned {
    
    public CleanerCoordinator() {
        super( AgentType.CLEANER_COORDINATOR );
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
        
        // Add behaviour
        //addBehaviour( new SetupBehaviour(this) );
        
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
    }
    
    
    @Override
    public void setupSettings( GameSettings gameSettings ) {
        
        List<Cell> cleaners = gameSettings.getAgentList().get( AgentType.CLEANER );
        for ( int i = 0; i < cleaners.size(); ++i ){
            String name = "clag" + i;
            AID cleanerAgent = searchAgent( AgentType.CLEANER.toString(), name );
            addNextAgent( cleanerAgent );
        }
        
        log( "Number of cleaners = " + getNextAgents().size() );
    }
}
