package cat.urv.imas.agent;

import cat.urv.imas.behaviour.SetupBehaviour;
import jade.core.AID;

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
        
        // Add previous agents (Coordinator Agent)
        AID coordinatorAgent = searchAgent( AgentType.COORDINATOR.toString() );
        addPreviousAgent( coordinatorAgent );
        
        // Add next agents (Cleaner Workers)
        AID cleanerAgent1 = searchAgent( AgentType.CLEANER.toString(), "clag1" );
        AID cleanerAgent2 = searchAgent( AgentType.CLEANER.toString(), "clag2" );
        AID cleanerAgent3 = searchAgent( AgentType.CLEANER.toString(), "clag3" );
        addNextAgent( cleanerAgent1 );
        addNextAgent( cleanerAgent2 );
        addNextAgent( cleanerAgent3 );
        
        // Add behaviour
        addBehaviour( new SetupBehaviour(this) );
        
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
    }
    
}
