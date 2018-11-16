package cat.urv.imas.agent;

import cat.urv.imas.behaviour.SetupBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import jade.core.AID;
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
    
    @Override
    public void onSettingsReceived(GameSettings gameSettings) {
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
        this.position = agents.get( index-1 );
        
        log( "Position [" + position.getRow() + "," + position.getCol() + "]");
        log( "Autonomy (" + maxSteps + ")" );
    }
}
