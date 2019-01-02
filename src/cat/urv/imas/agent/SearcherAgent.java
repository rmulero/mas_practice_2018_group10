package cat.urv.imas.agent;

import cat.urv.imas.behaviour.agent.AgentResponseActionsBehaviour;
import cat.urv.imas.behaviour.agent.AgentResponseUpdatesBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.InfoAgent;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.AStarNode;
import cat.urv.imas.utils.ActionUtils;
import cat.urv.imas.utils.AstarPathFinderAlgorithm;
import cat.urv.imas.utils.actions.DetectAction;
import cat.urv.imas.utils.actions.MoveAction;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SearcherAgent extends ImasAgentTuned {
    
    private int maxAutonomy = 0;
    private int currentAutonomy = 0;
    private int currentRow = 0;
    private int currentCol = 0;
    private int chargingStep = 0;
    
    private AstarPathFinderAlgorithm pathFinder;
    private final List<Cell> chargingPoints;
    private final List<String> actions;
    private final List<AStarNode> chargingPointPath;
    
    private State currentState;
    private Direction currentDirection;
    
    private enum State{
        SEARCHING, NEED_CHARGE,
        CHARGING, STOPPED
    }
    
    private enum Direction{
        UP, DOWN, LEFT, RIGHT
    }
    
    public SearcherAgent() {
        super( AgentType.SEARCHER );
        this.chargingPoints = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.chargingPointPath = new ArrayList<>();
        this.currentState = State.SEARCHING;
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
    public void setupSettings( GameSettings gameSettings ) {
        
        // Set the game settings
        setGameSettings( gameSettings );
        
        // Get autonomy
        this.maxAutonomy = gameSettings.geteSearcherMaxSteps();
        this.currentAutonomy = gameSettings.geteSearcherMaxSteps();
        
        // Get charging points
        List<Cell> recycle = gameSettings.getCellsOfType().get( CellType.BATTERIES_CHARGE_POINT );
        this.chargingPoints.addAll( recycle );
        
        // Get agent position
        List<Cell> agents = gameSettings.getAgentList().get( this.type );
        String localName = getLocalName();
        localName = localName.replace("seag", "");
        int index = Integer.valueOf( localName );
        
        Cell agentCell = agents.get( index );
        this.currentRow = agentCell.getRow();
        this.currentCol = agentCell.getCol();
        
        PathCell pc = (PathCell) agentCell;
        InfoAgent a = pc.getAgents().get( type ).get( 0 );
        a.setAID( this.getAID() );
        
        // Choose the initial direction
        chooseInitialDirection();
        
        // Log agent info
        log( "Position [" + currentRow + "," + currentCol + "]" );
        log( "Autonomy (" + maxAutonomy + ")" );
        
        // Set the pathfinder algorithm
        this.pathFinder = new AstarPathFinderAlgorithm( gameSettings );
    }
    
    private void chooseInitialDirection(){
        // Get surrounding cells
        Cell[] cells = {
            getGameSettings().get( currentRow - 1, currentCol ),
            getGameSettings().get( currentRow + 1, currentCol ),
            getGameSettings().get( currentRow, currentCol - 1 ),
            getGameSettings().get( currentRow, currentCol + 1 )
        };
        
        // Get the surrounding path cells
        List<Cell> pathCells = new ArrayList<>();
        for ( Cell cell : cells ){
            if ( cell.getCellType() == CellType.PATH ){
                pathCells.add( cell );
            }
        }
        
        // Choose one of the paths randomly
        Random rand = new Random();
        int index = rand.nextInt( pathCells.size() );
        Cell chosen = pathCells.get( index );
        
        if ( chosen.getRow() > currentRow ){
            this.currentDirection = Direction.DOWN;
            
        } else if ( chosen.getRow() < currentRow ) {
            this.currentDirection = Direction.UP;
            
        } else if ( chosen.getCol() > currentCol ) {
            this.currentDirection = Direction.RIGHT;
            
        } else if ( chosen.getCol() < currentCol ) {
            this.currentDirection = Direction.LEFT;
        }
    }
    
    /////////////////////////////////////
    //      COMMUNICATIONS
    /////////////////////////////////////
    private void waitForActionRequest(){
        
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.and(
                    MessageTemplate.MatchProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST ),
                    MessageTemplate.MatchPerformative( ACLMessage.REQUEST )
                ), 
                MessageTemplate.MatchContent( MessageContent.GET_ACTIONS )
        );
        
        // Add behaviour to wait for REQUEST
        addBehaviour( new AgentResponseActionsBehaviour(this, template) );
    }
    
    @Override
    public void onActionsRequest( ACLMessage request ) {
        performActions();
        request.setContent( String.join( ActionUtils.DELIMITER_ACTION, actions ) );
        waitForUpdateRequest();
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
        addBehaviour( new AgentResponseUpdatesBehaviour(this, template) );
    }
    
    @Override
    public void onUpdateRequest( ACLMessage request ) {
        
        String content = request.getContent();
        
        String[] updates = content.split( ActionUtils.DELIMITER_ACTION );
        for ( String update : updates ){
            if ( update.startsWith( getLocalName() )){
                
                String[] updateParts = update.split( ActionUtils.DELIMITER_PART );
                if ( updateParts[ 1 ].equalsIgnoreCase( ActionUtils.ACTION_MOVE )){
                    MoveAction moveAction = MoveAction.fromString( update );
                    int nextRow = moveAction.getDestinationRow();
                    int nextCol = moveAction.getDestinationCol();
                    
                    currentRow = nextRow;
                    currentCol = nextCol;
                    --currentAutonomy;
                    
                    if ( currentState == State.NEED_CHARGE ){
                        
                        AStarNode node = chargingPointPath.get( 1 );
                        boolean updatedPath = ( node.getRow() == currentRow && 
                                node.getCol() == currentCol );
                        
                        if ( updatedPath ){
                            chargingPointPath.remove( 0 );
                        }
                    }
                    
                    if ( currentAutonomy == 0 ){
                        currentState = State.STOPPED;
                    }
                }
            }
        }
        
        waitForActionRequest();
    }
    
    /////////////////////////////////////
    //      ACTIONS
    /////////////////////////////////////
    
    private void performActions(){
        
        actions.clear();
        
        switch( currentState ){
            case SEARCHING:
                performSearchingActions();
                break;
            case NEED_CHARGE:
                performNeedChargeActions();
                break;
            case CHARGING:
                performChargingActions();
                break;
            case STOPPED:
            default:
                break;
        }
        
    }
    
    /**
     * Actions performed when state is SEARCHING
     */
    private void performSearchingActions(){
        
        detectWastes();
        findClosestChargingPointPath();
        checkCurrentAutonomy();
        moveAttempt();
    }
    
    /**
     * Actions performed when state is NEED_CHARGE
     */
    private void performNeedChargeActions(){
        detectWastes();
        moveAttempt();
    }
    
    /**
     * Actions performed when state is CHARGING
     */
    private void performChargingActions(){
        chargingStep--;
        if ( chargingStep == 0 ){
            currentAutonomy = maxAutonomy;
            currentState = State.SEARCHING;
        }
    }
    
    /**
     * Detect wastes around the current position
     */
    private void detectWastes(){
        
        Cell[] wastes = getGameSettings().detectFieldsWithWaste( currentRow, currentCol );
        for ( Cell wasteCell : wastes ){
            if ( wasteCell != null ){
                // Get cell info
                FieldCell fc = (FieldCell) wasteCell;
                String positionStr = fc.getRow() + "," + fc.getCol();
                
                // Get waste info
                Map<WasteType, Integer> waste = fc.getWaste();
                
                List<WasteType> types = new ArrayList<>( waste.keySet() );
                WasteType wasteType = types.get( 0 );
                int wasteAmount = waste.get( wasteType );
                
                // Build action string
                DetectAction detectAction = new DetectAction(
                        getLocalName(),
                        ActionUtils.ACTION_DETECT,
                        positionStr, 
                        wasteType.getShortString(), 
                        String.valueOf( wasteAmount )
                );
                
                actions.add( detectAction.toString() );
            }
        }
    }
    
    /**
     * Check if the agent has enough autonomy to reach the charging point
     */
    private void checkCurrentAutonomy(){
        
        if( currentAutonomy - 5 < chargingPointPath.size() ){
            currentState = State.NEED_CHARGE;
        }
    }
    
    /**
     * Try to move according to the direction or according to the path.
     * In case that the agent does not requires to charge, 
     * it will follow the direction. In case it need to charge, 
     * it will follow the path to the charging point
     */
    private void moveAttempt(){
        
        switch( currentState ){
            case SEARCHING:
                followDirection();
                break;
            case NEED_CHARGE:
                followPath();
                break;
        }
    }
    
    /**
     * Move according to the direction. In case that the next cell is not a path,
     * the agent will change the direction according to its current position.
     */
    private void followDirection(){
        switch ( currentDirection ){
            case UP:
                checkNextCell( currentRow - 1, currentCol );
                break;
            case DOWN:
                checkNextCell( currentRow + 1, currentCol );
                break;
            case LEFT:
                checkNextCell( currentRow, currentCol - 1 );
                break;
            case RIGHT:
                checkNextCell( currentRow, currentCol + 1 );
                break;
        }
    }
    
    /**
     * Check if the specified cell is PATH
     * @param row   Row of the cell
     * @param col   Columns of the cell
     */
    private void checkNextCell( int row, int col ){
        Cell next = getGameSettings().get( row, col );
        if ( next.getCellType() == CellType.PATH ){
            
            String originStr = currentRow + "," + currentCol;
            String destinationStr = next.getRow() + "," + next.getCol();
            
            // Build action string
            MoveAction action = new MoveAction(
                    getLocalName(), 
                    ActionUtils.ACTION_MOVE, 
                    originStr, 
                    destinationStr, 
                    String.valueOf( currentAutonomy )
            );
            
            actions.add( action.toString() );
            
        } else {
            changeDirection();
        }
    }
    
    /**
     * Change the direction of the agent. If the agent was trying to go UP or DOWN,
     * it will change its direction to LEFT or RIGHT. If the agent was trying to 
     * go LEFT or RIGHT, it will change its direction to UP or DOWN
     */
    private void changeDirection(){
        
        Random rand = new Random();
        
        switch ( currentDirection ){
            case UP:    // choose left - right
            case DOWN:
                CellType left = getGameSettings().get( currentRow, currentCol - 1 ).getCellType();
                CellType right = getGameSettings().get( currentRow, currentCol + 1 ).getCellType();
                
                if ( left == CellType.PATH && right != CellType.PATH ){
                    currentDirection = Direction.LEFT;
                } else if ( left != CellType.PATH && right == CellType.PATH ) {
                    currentDirection = Direction.RIGHT;
                } else {
                    int n = rand.nextInt( 2 );
                    currentDirection = ( n == 0 ) ? Direction.LEFT : Direction.RIGHT;
                }
                
                break;
            case LEFT:  // choose up - down
            case RIGHT:
                CellType upper = getGameSettings().get( currentRow - 1, currentCol ).getCellType();
                CellType down = getGameSettings().get( currentRow + 1, currentCol ).getCellType();
                
                if ( upper == CellType.PATH && down != CellType.PATH ){
                    currentDirection = Direction.UP;
                } else if ( upper != CellType.PATH && down == CellType.PATH ) {
                    currentDirection = Direction.DOWN;
                } else {
                    int n = rand.nextInt( 2 );
                    currentDirection = ( n == 0 ) ? Direction.UP : Direction.DOWN;
                }
                break;
        }
        
        followDirection();
    }
    
    /**
     * Move according to the path. In case that the agent has reached to the 
     * charging point, it will start to charge the battery.
     */
    private void followPath(){
        
        if ( chargingPointPath.size() == 1 ){
            currentState = State.CHARGING;
            chargingStep = 3;
            
        } else if ( chargingPointPath.size() > 1 ){
            AStarNode node = chargingPointPath.get( 0 );
            if ( node.getRow() == currentRow && node.getCol() == currentCol ){
                node = chargingPointPath.get( 1 );
                
            } else if ( chargingPointPath.size() > 2 ){
                pathFinder.setNodeToAvoid( chargingPointPath.get( 1 ) );
                AStarNode currentOrigin = new AStarNode( currentRow, currentCol );
                Set<AStarNode> destination = new HashSet<>();
                destination.add( chargingPointPath.get( 2 ) );
                List<AStarNode> deviation = pathFinder.findPath( currentOrigin, destination );
                pathFinder.unsetNodeToAvoid();
                
                chargingPointPath.remove( 2 );
                chargingPointPath.remove( 1 );
                chargingPointPath.remove( 0 );
                
                deviation.addAll( chargingPointPath );
                chargingPointPath.clear();
                chargingPointPath.addAll( deviation );
                node = chargingPointPath.get( 1 );
                
            } else {
                node = null;
            }
            
            if ( node != null ){
                String originStr = currentRow + "," + currentCol;
                String destinationStr = node.getRow() + "," + node.getCol();

                MoveAction action = new MoveAction(
                        getLocalName(), 
                        ActionUtils.ACTION_MOVE, 
                        originStr, 
                        destinationStr, 
                        String.valueOf( currentAutonomy ),
                        ActionUtils.FLAG_NEED_CHARGE
                );

                actions.add( action.toString() );
            }
        }
    }
    
    /**
     * Find the shortest path to the closest charging point. The path is chosen 
     * using the A* algorithm, which is similar to Dijkstra, but it considers 
     * that there may be obstacles.
     */
    private void findClosestChargingPointPath(){
        
        int pathSteps = Integer.MAX_VALUE;
        List<AStarNode> bestPath = new ArrayList<>();
        
        AStarNode origin = new AStarNode( currentRow, currentCol );
        
        for ( Cell chargingPoint : chargingPoints ){
            
            Set<AStarNode> destinations = pathFinder.getSurroundingPathCells( chargingPoint );
            
            List<AStarNode> path = pathFinder.findPath( origin, destinations );
            if ( pathSteps > path.size() && !path.isEmpty() ){
                pathSteps = path.size();
                bestPath.clear();
                bestPath.addAll( path );
            }
        }
        
        if ( !bestPath.isEmpty() ){
            chargingPointPath.clear();
            chargingPointPath.addAll( bestPath );
        }
    }
    
}
