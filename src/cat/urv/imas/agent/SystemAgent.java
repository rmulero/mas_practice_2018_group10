/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.system.SystemRequestActionsBehaviour;
import cat.urv.imas.behaviour.system.SystemRequestUpdatesBehaviour;
import cat.urv.imas.ontology.InitialGameSettings;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.map.Agents;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.InfoAgent;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.ActionUtils;
import cat.urv.imas.utils.StatisticalInformation;
import cat.urv.imas.utils.actions.CollectAction;
import cat.urv.imas.utils.actions.DetectAction;
import cat.urv.imas.utils.actions.MoveAction;
import cat.urv.imas.utils.actions.RecycleAction;
import jade.core.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgentTuned {

    private final StatisticalInformation statistics;
    
    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private InitialGameSettings game;

    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super( AgentType.SYSTEM );
        
        this.statistics = new StatisticalInformation();
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the
     * stantard output.
     *
     * @param log String to show
     */
    @Override
    public void log( String log ) {
        if ( gui != null ) {
            gui.log( getLocalName()+ ": " + log + "\n" );
        }
        super.log(log);
    }

    /**
     * An error message is shown in the log area of the GUI, as well as in the
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog( String error) {
        if (gui != null) {
            gui.log( "ERROR: " + getLocalName()+ ": " + error + "\n" );
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Adds (if probability matches) new elements onto the map
     * for every simulation step.
     * This method is expected to be run from the corresponding Behaviour
     * to add new elements onto the map at each simulation step.
     */
    public void addElementsForThisSimulationStep() {
        this.game.addElementsForThisSimulationStep();
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);

        // 1. Register the agent to the DF
        registerDF();

        // 2. Load game settings.
        InitialGameSettings settings = InitialGameSettings.load( "game.evaluation.seconddate.settings" );
        this.game = settings;
        
        log( "Initial configuration settings loaded" );
        settings.addElementsForThisSimulationStep();
        setupSettings( settings );
       
        // 3. Load GUI
        try {
            this.gui = new GraphicInterface( settings );
            gui.setVisible(true);
            log( "GUI loaded" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        // Create the agents dynamically
        Object[] arguments = { settings };
        UtilsAgents.createAgent( this.getContainerController(), "ca", CoordinatorAgent.class.getName(), arguments);
        UtilsAgents.createAgent( this.getContainerController(), "cc", CleanerCoordinator.class.getName(), arguments);
        UtilsAgents.createAgent( this.getContainerController(), "sc", EsearcherCoordinator.class.getName(), arguments);
        
        List<Cell> cleaners = settings.getAgentList().get( AgentType.CLEANER );
        for( int i = 0; i < cleaners.size(); ++i ){
            String name = "clag" + i;
            UtilsAgents.createAgent( this.getContainerController(), name, CleanerAgent.class.getName(), arguments );
        }
        
        List<Cell> searchers = settings.getAgentList().get( AgentType.SEARCHER );
        for( int i = 0; i < searchers.size(); ++i ){
            String name = "seag" + i;
            UtilsAgents.createAgent( this.getContainerController(), name, SearcherAgent.class.getName(), arguments );
        }
        
        // Add next agents (Coordinator Agent)
        AID coordinatorAgent = searchAgent( AgentType.COORDINATOR.toString() );
        addNextAgent( coordinatorAgent );
        
        // Start simulation
        requestActions();
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
        gui.dispose();
    }
    
    public void updateGUI() {
        this.gui.updateGame();
    }

    @Override
    public void setupSettings(GameSettings gameSettings) {
        setGameSettings( gameSettings );
        log( gameSettings.toString() );
        maxSteps = gameSettings.getSimulationSteps();
    }
    
    private int maxSteps = 0;
    private int currentStep = 1;
    
    /////////////////////////////////////
    //      COMMUNICATIONS
    /////////////////////////////////////
    private void requestActions(){
        
        log( "Requesting actions for step " + currentStep );
        
        ACLMessage message = new ACLMessage( ACLMessage.REQUEST );
        message.setProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST );
        message.setContent( MessageContent.GET_ACTIONS );
        
        for( AID nextAgent : getNextAgents() ){
            message.addReceiver( nextAgent );
        }
        
        // Add behaviour to handle REQUEST responses
        addBehaviour( new SystemRequestActionsBehaviour(this, message) );
    }
    
    @Override
    public void onActionsReceived( String actions ){
        log( "Actions received" );
        
        String updates = checkActions( actions );
        requestUpdate( updates );
    }
    
    private void requestUpdate( String updates ){
        log("Requesting updates for step " + currentStep);
        
        ACLMessage message = new ACLMessage( ACLMessage.REQUEST );
        message.setProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST );
        message.setContent( updates );
        
        for( AID nextAgent : getNextAgents() ){
            message.addReceiver( nextAgent );
        }
        
        // Add behaviour to handle REQUEST responses
        addBehaviour(new SystemRequestUpdatesBehaviour(this, message) );
    }
    
    @Override
    public void onUpdateConfirmed(){
        log( "Updates confirmed" );
        
        currentStep++;
        
        if ( currentStep <= maxSteps ){
            try {
                Thread.sleep( 500 );
            } catch ( InterruptedException ex ) {
                
            }
            
            try {
                addElementsForThisSimulationStep();
            } catch ( Error er ) {
                
            }
            
            updateGUI();
            
            requestActions();
            
        } else {
            
            String stats = getStatistics();
            gui.showStatistics( stats );
            
            log( "SIMULATION FINISHED" );
        }
    }
    
    private String getStatistics(){
        StringBuilder sb = new StringBuilder();
        
        int mUnits = statistics.getRecycledUnitsOfType( WasteType.MUNICIPAL.getShortString() );
        int iUnits = statistics.getRecycledUnitsOfType( WasteType.INDUSTRIAL.getShortString() );
        
        sb.append( "\nWASTES TREATED\n" );
        sb.append( "--------------\n" );
        sb.append( "Municipal: " ).append( mUnits ).append( "\n" );
        sb.append( "Industrial: " ).append( iUnits ).append( "\n\n" );
        
        float unitsPerCleaner = statistics.getAverageCollectedUnits();
        sb.append( "UNITS OF WASTE PER CLEANER\n" );
        sb.append( "--------------------------\n" );
        List<Cell> cleaners = getGameSettings().getAgentList().get( AgentType.CLEANER );
        for( int i = 0; i < cleaners.size(); ++i ){
            String name = "clag" + i;
            int units = statistics.getCollectedUnitsPerCleaner( name );
            sb.append( name ).append( " = " ).append( units ).append( "\n" );
        }
        sb.append( "Average units per cleaner = " ).append( unitsPerCleaner ).append( "\n\n" );
        
        float averageDiscoveryTime = statistics.getAverageDiscoveryTime();
        float averageCollectionTime = statistics.getAverageCollectionTime();
        sb.append( "AVERAGE TIMES\n" );
        sb.append( "-------------\n" );
        sb.append( "Discovery = " ).append( averageDiscoveryTime ).append( "\n" );
        sb.append( "Collection = " ).append( averageCollectionTime ).append( "\n\n" );
        
        float discoveryRatio = statistics.discoveredWastesRatio();
        sb.append( "Discovered wastes ratio = ");
        sb.append( discoveryRatio * 100 ).append( "%\n" );
        
        return sb.toString();
    }
    
    /////////////////////////////////////
    //      ACTIONS
    /////////////////////////////////////
    private String checkActions( String actions ){
        
        // Get the actions by type
        Map<String, List<String>> actionsByType = new HashMap<>();
        
        String[] individualActions = actions.split( ActionUtils.DELIMITER_ACTION );
        for ( String action : individualActions ){
            if ( !action.isEmpty() ){
                String[] parts = action.split( ActionUtils.DELIMITER_PART );
                String actionType = parts[ 1 ];
                
                if ( !actionType.isEmpty() ){
                    List<String> list = actionsByType.get( actionType );
                    if ( list == null ){
                        list = new ArrayList<>();
                        actionsByType.put( actionType, list );
                    }
                    list.add( action );
                }
            }
        }
        
        // Check actions
        List<String> checkedActions = new ArrayList<>();
        for ( String type : actionsByType.keySet() ){
            List<String> list = actionsByType.get( type );
            
            switch( type ){
                case ActionUtils.ACTION_MOVE:
                    checkMoves( list );
                    checkedActions.addAll( list );
                    break;
                    
                case ActionUtils.ACTION_RECYCLE:
                    // Add recycled wastes statistics
                    checkRecycled( list );
                case ActionUtils.ACTION_DETECT:
                case ActionUtils.ACTION_COLLECT_END:
                case ActionUtils.ACTION_COLLECT_START:
                    checkedActions.addAll( list );
                    break;
            }
        }
        
        // Add generated wastes statistics
        checkGenerated();
        
        // Add detection statisticss
        List<String> detections = actionsByType.get( ActionUtils.ACTION_DETECT );
        if ( detections == null ){
            detections = new ArrayList<>();
        }
        checkDetections( detections );
        
        // Add collection statistics
        List<String> collects = actionsByType.get( ActionUtils.ACTION_COLLECT_START );
        if ( collects == null ){
            collects = new ArrayList<>();
        }
        checkCollections( collects );
        
        return String.join( ActionUtils.DELIMITER_ACTION, checkedActions );
    }
    
    private void checkGenerated(){
        List<Cell> fields = getGameSettings().getCellsOfType().get( CellType.FIELD );
        
        List<Cell> wastes = new ArrayList<>();
        for ( Cell cell : fields ){
            String message = cell.getMapMessage();
            if ( !message.isEmpty() ){
                wastes.add( cell );
            }
        }
        
        statistics.addGeneratedWastes( wastes );
    }
    
    private void checkDetections( List<String> actions ){
        
        List<Cell> cells = new ArrayList<>();
        
        for ( String action : actions ){
            DetectAction detection = DetectAction.fromString( action );
            
            int row = detection.getWasteRow();
            int col = detection.getWasteCol();
            Cell cell = getGameSettings().get( row, col );
            cells.add( cell );
        }
        
        statistics.addDiscoveredWastes( cells );
    }
    
    private void checkCollections( List<String> actions ){
        List<Cell> cells = new ArrayList<>();
        
        for ( String action : actions ){
            CollectAction collect = CollectAction.fromString( action );
            
            int row = collect.getWasteRow();
            int col = collect.getWasteCol();
            Cell cell = getGameSettings().get( row, col );
            cells.add( cell );
            
            String agentName = collect.getAgent();
            int amount = collect.getWasteAmount();
            statistics.updateCleanerStats( agentName, amount );
        }
        
        statistics.addCollectedWastes( cells );
    }
    
    private void checkRecycled( List<String> actions ){
        
        for ( String action : actions ){
            RecycleAction collect = RecycleAction.fromString( action );
            
            String type = collect.getWasteType();
            int amount = collect.getWasteAmount();
            statistics.updateRecycledWasteUnits( type, amount );
        }
    }
    
    private void checkMoves( List<String> actions ){
        
        // Generate the actions
        List<MoveAction> moveActions = new ArrayList<>();
        for ( String action : actions ){
            moveActions.add( MoveAction.fromString( action ) );
        }
        
        boolean isConflict;
        int attempts = 5;
        do{
            // Check if the moves are allowed
            checkMovesContraint( moveActions );
        
            // Check if the destinations are not empty
            boolean emptyDestConflict = isEmptyDestinationConflict( moveActions );
        
            // Check if the destinations are the same for two agents
            boolean sameDestConflict = isSameDestinationConflict( moveActions );
            
            isConflict = emptyDestConflict || sameDestConflict;
            --attempts;
            
        } while( isConflict && attempts > 0 );
        
        if ( isConflict ){
            moveActions.clear();
        }
        
        // Update actions list
        actions.clear();
        for ( MoveAction action : moveActions ){
            actions.add( action.toString() );
        }
        
        if ( !moveActions.isEmpty() ){
            updateMap( moveActions );
        }
    }
    
    private void checkMovesContraint( List<MoveAction> actions ){
        
        for ( int i = 0; i < actions.size(); ++i ){
            MoveAction action = actions.get( i );
            
            int oRow = action.getOriginRow();
            int oCol = action.getOriginCol();
            int dRow = action.getDestinationRow();
            int dCol = action.getDestinationCol();
            
            int sum = Math.abs( oRow - dRow ) + Math.abs( oCol - dCol );
            if ( sum != 1 ){
                actions.remove( i );
                --i;
            }
        }
    }
    
    private boolean isEmptyDestinationConflict( List<MoveAction> actions ){
        
        boolean existConflict = false;
        
        for ( int i = 0; i < actions.size(); ++i ){
            MoveAction currentAction = actions.get( i );
            
            int dRow = currentAction.getDestinationRow();
            int dCol = currentAction.getDestinationCol();
            
            // Check if the cell is empty
            boolean emptyDest = true;
            Map<AgentType, List<Cell>> agents = getGameSettings().getAgentList();
            for ( Map.Entry<AgentType, List<Cell>> entry : agents.entrySet() ){
                for ( Cell cell : entry.getValue() ){
                    if ( cell.getRow() == dRow && cell.getCol() == dCol ){
                        emptyDest = false;
                    }
                }
            }
            
            // If the cell is not empty, check if the agent will move
            if ( !emptyDest ){
                
                // Get the conflictive action
                int conflictIndex = getConflictiveActionIndex( actions, dRow, dCol );
                
                // If the agents are going to collide, move one of them to another direction
                if ( conflictIndex >= 0 ){
                    MoveAction conflict = actions.get( conflictIndex );
                    
                    boolean collisionPrediction = ( 
                            conflict.getDestinationRow() == currentAction.getOriginRow() && 
                            conflict.getDestinationCol() == currentAction.getOriginCol() 
                    );
                    
                    if ( collisionPrediction ){
                        solveConflict( currentAction, conflict );
                        existConflict = true;
                    }
                    
                } else {
                    deviate( currentAction );
                }
            }
        }
        
        return existConflict;
    }
    
    private boolean isSameDestinationConflict( List<MoveAction> actions ){
        
        boolean existConflict = false;
        
        for ( int i = 0; i < actions.size() - 1; ++i ){
            MoveAction currentAction = actions.get( i );
            
            for ( int j = 0; j < actions.size(); ++j ){
                MoveAction otherAction = actions.get( j );
                if ( i != j ){
                    
                    boolean sameDestination = ( 
                            currentAction.getDestinationRow() == otherAction.getDestinationRow() && 
                            currentAction.getDestinationCol() == otherAction.getDestinationCol()
                    );
                    
                    if ( sameDestination ){
                        solveConflict( currentAction, otherAction );
                        existConflict = true;
                    }
                }
            }
        }
        
        return existConflict;
    }
    
    private void solveConflict( MoveAction actionA, MoveAction actionB ) {
        
        boolean aFlagEmpty = actionA.getFlag().isEmpty();
        boolean bFlagEmpty = actionB.getFlag().isEmpty();

        if ( aFlagEmpty && !bFlagEmpty ){
            deviate( actionA );

        } else if ( !aFlagEmpty && bFlagEmpty ){
            deviate( actionB );

        } else {

            if ( actionA.getAutonomy() > actionB.getAutonomy() ){
                deviate( actionA );
            } else {
                deviate( actionB );
            }    
        }
    }
    
    private void deviate( MoveAction action ){
        
        int currentRow = action.getOriginRow();
        int currentCol = action.getOriginCol();
        
        // Get the adjacent path cells, excluding the current destination
        Cell[] cells = {
            getGameSettings().get( currentRow - 1, currentCol ),
            getGameSettings().get( currentRow + 1, currentCol ),
            getGameSettings().get( currentRow, currentCol - 1 ),
            getGameSettings().get( currentRow, currentCol + 1 )
        };
        
        // Get the opposite cell to the destination
        int rowDifference = action.getDestinationRow() - action.getOriginRow();
        int colDifference = action.getDestinationCol() - action.getOriginCol();
        int oppositeRow = currentRow - rowDifference;
        int oppositeCol = currentCol - colDifference;
        
        // Get the possible candidates
        List<Cell> candidates = new ArrayList<>();
        for ( Cell cell : cells ){
            
            boolean currentDestination = (
                    action.getDestinationRow() == cell.getRow() &&
                    action.getDestinationCol() == cell.getCol()
            );
            
            boolean oppositeDestination = ( 
                    cell.getRow() == oppositeRow && 
                    cell.getCol() == oppositeCol
            );
            
            boolean allowedDestination = !currentDestination && !oppositeDestination;
            boolean pathType = cell.getCellType() == CellType.PATH;
            
            if ( pathType && allowedDestination ){
                candidates.add( cell );
            }
        }
        
        // Choose one of the cells
        Random rand = new Random();
        int index = rand.nextInt( candidates.size() );
        Cell cell = candidates.get( index );
        action.setDestinationRow( cell.getRow() );
        action.setDestinationCol( cell.getCol() );
    }
    
    private int getConflictiveActionIndex( List<MoveAction> actions, int dRow, int dCol ){
        
        int index = -1;
        
        for ( int i = 0; i < actions.size() && index < 0; ++i ){
            MoveAction action = actions.get( i );
            
            boolean sameRow = action.getOriginRow() == dRow;
            boolean sameCol = action.getOriginCol() == dCol;
            
            if ( sameRow && sameCol ){
                index = i;
            }
        }
        
        return index;
    }
    
    private void updateMap( List<MoveAction> actions ){
        
        Map<AgentType, List<Cell>> agentList = getGameSettings().getAgentList();
        
        List<MoveAction> pendingActions = new ArrayList<>();
        pendingActions.addAll( actions );
        while( !pendingActions.isEmpty() ){
            
            MoveAction action = pendingActions.remove( 0 );
            
            PathCell current = getPathCell( action.getOriginRow(), action.getOriginCol() );
            PathCell next = getPathCell( action.getDestinationRow(), action.getDestinationCol() );
            
            if ( next.getAgents().isEmpty() ){
                
                InfoAgent agent = null;
                
                for ( AgentType agentType : agentList.keySet() ){
                    List<Cell> agentTypeCells = agentList.get( agentType );
                    for ( int i = 0; i < agentTypeCells.size() && agent == null; ++i ){

                        Cell cell = agentTypeCells.get( i );
                        boolean sameRow = cell.getRow() == current.getRow();
                        boolean sameCol = cell.getCol() == current.getCol();

                        if ( sameRow && sameCol ){
                            Agents agents = current.getAgents();
                            List<InfoAgent> infoAgents = agents.get( agentType );
                            agent = infoAgents.get( 0 );
                            
                            // Remove the agent from the current cell
                            try {
                                current.removeAgent( agent );
                            } catch (Exception ex) {
                            }

                            // Add the agent to the next cell
                            try {
                                next.addAgent( agent );
                            } catch (Exception ex) {
                            }

                            // Update the agents list
                            agentTypeCells.set( i , next );
                        }
                    }
                }
                
            } else {
                pendingActions.add( action );
            }
        }
    }
    
    private PathCell getPathCell( int row, int col ){
        
        Cell cell = getGameSettings().get( row, col );
        PathCell pCell = (PathCell) cell;
        return pCell;
    }
}
