package cat.urv.imas.agent;

import cat.urv.imas.behaviour.SetupBehaviour;
import jade.core.AID;

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
        
        // Add previous agents (Coordinator Agent)
        AID coordinatorAgent = searchAgent( AgentType.COORDINATOR.toString() );
        addPreviousAgent( coordinatorAgent );
        
        // Add next agents (Searcher Workers)
        AID searcherAgent1 = searchAgent( AgentType.SEARCHER.toString(), "seag1" );
        AID searcherAgent2 = searchAgent( AgentType.SEARCHER.toString(), "seag2" );
        AID searcherAgent3 = searchAgent( AgentType.SEARCHER.toString(), "seag3" );
        AID searcherAgent4 = searchAgent( AgentType.SEARCHER.toString(), "seag4" );
        addNextAgent( searcherAgent1 );
        addNextAgent( searcherAgent2 );
        addNextAgent( searcherAgent3 );
        addNextAgent( searcherAgent4 );
        
        // Add behaviour
        addBehaviour( new SetupBehaviour(this) );
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
    }
}
