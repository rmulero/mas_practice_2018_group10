package cat.urv.imas.agent;

import cat.urv.imas.behaviour.SetupBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import jade.core.AID;
import java.util.ArrayList;
import java.util.List;

public class CleanerAgent extends ImasAgentTuned {
    
    private int maxCapacity = 0;
    private int currentLoad = 0;
    private Cell position;
    private final List<Cell> recyclingPoints; 
    
    public CleanerAgent() {
        super( AgentType.CLEANER );
        this.recyclingPoints = new ArrayList<>();
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
    
    @Override
    public void onSettingsReceived(GameSettings gameSettings) {
        // get capacity
        this.maxCapacity = gameSettings.getCleanerCapacity();
        
        // get recycling points
        List<Cell> recycle = gameSettings.getCellsOfType().get( CellType.RECYCLING_POINT_CENTER );
        this.recyclingPoints.addAll( recycle );
        
        // get agent position
        List<Cell> agents = gameSettings.getAgentList().get( this.type );
        String localName = getLocalName();
        localName = localName.replace("clag", "");
        int index = Integer.valueOf( localName );
        this.position = agents.get( index-1 );
        
        log( "Position [" + position.getRow() + "," + position.getCol() + "]");
        log( "Capacity (" + maxCapacity  + ")" );
    }
}
