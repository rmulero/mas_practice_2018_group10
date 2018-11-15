package cat.urv.imas.agent;

import cat.urv.imas.behaviour.SetupBehaviour;
import jade.core.AID;

public class SearcherAgent extends ImasAgentTuned {
    
    public SearcherAgent() {
        super( AgentType.SEARCHER );
    }
    
    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerDF();
        
        // Add previous agents (Searcher Coordinator)
        AID searcherCoordinator = searchAgent( AgentType.ESEARCHER_COORDINATOR.toString() );
        addPreviousAgent(searcherCoordinator );
        
        // Add behaviour
        addBehaviour( new SetupBehaviour(this) );
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
    }
}
