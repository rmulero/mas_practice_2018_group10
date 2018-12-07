package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import jade.core.AID;
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
        
        // Add behaviour
        //addBehaviour( new SetupBehaviour(this) );
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
}
