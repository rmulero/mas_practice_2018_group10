package cat.urv.imas.agent;

import cat.urv.imas.behaviour.SetupBehaviour;
import jade.core.AID;

public class CleanerAgent extends ImasAgentTuned {
    
    public CleanerAgent() {
        super( AgentType.CLEANER );
    }
    
    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerDF();
        
        // Add previous agents (Cleaner Coordinator)
        AID cleanerCoordinator = searchAgent( AgentType.CLEANER_COORDINATOR.toString() );
        addPreviousAgent( cleanerCoordinator );
        
        // Add behaviour
        addBehaviour( new SetupBehaviour(this) );
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
    }
}
