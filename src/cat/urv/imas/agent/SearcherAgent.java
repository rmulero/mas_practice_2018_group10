package cat.urv.imas.agent;

import cat.urv.imas.behaviour.agent.AgentResponseActionsBehaviour;
import cat.urv.imas.behaviour.agent.AgentResponseUpdatesBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.AStarNode;
import cat.urv.imas.utils.ActionUtils;
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
    public void setupSettings(GameSettings gameSettings) {
        
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
        
        // Log agent info
        log( "Position [" + currentRow + "," + currentCol + "]");
        log("Autonomy (" + maxAutonomy + ")" );
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
        request.setContent( String.join( ";", actions) );
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
                String[] actionParts = {
                    getLocalName(),
                    ActionUtils.DETECT_ACTION,
                    positionStr,
                    wasteType.getShortString(),
                    String.valueOf( wasteAmount )
                };
                actions.add( String.join(ActionUtils.ACTION_PART_DELIMITER, actionParts) );
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
            
            String[] actionParts = {
                getLocalName(),
                ActionUtils.MOVE_ACTION,
                originStr,
                destinationStr,
                String.valueOf( currentAutonomy )
            };
            
            actions.add( String.join( ActionUtils.ACTION_PART_DELIMITER, actionParts) );
            
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
        
        AStarNode node = chargingPointPath.get( 0 );
        if ( chargingPointPath.size() == 1 ){
            currentState = State.CHARGING;
            chargingStep = 3;
            
        } else if ( chargingPointPath.size() > 1 ){
            if ( node.getRow() == currentRow && node.getCol() == currentCol ){
                node = chargingPointPath.get( 1 );
            }
            
            String originStr = currentRow + "," + currentCol;
            String destinationStr = node.getRow() + "," + node.getCol();

            String[] actionParts = {
                getLocalName(),
                ActionUtils.MOVE_ACTION,
                originStr,
                destinationStr,
                String.valueOf( currentAutonomy ),
                ActionUtils.NEED_CHARGE_FLAG
            };

            actions.add( String.join( ActionUtils.ACTION_PART_DELIMITER, actionParts) );
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
            
            Set<AStarNode> destinations = getSurroundingPathCells( chargingPoint );
            
            List<AStarNode> path = aStarPathFinderAlgorithm( origin, destinations );
            if ( pathSteps > path.size() ){
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
    
    private Set<AStarNode> getSurroundingPathCells( Cell centerCell ){
        
        Set<AStarNode> nodes = new HashSet<>();
        
        int row = centerCell.getRow();
        int col = centerCell.getCol();
        
        Cell[] cells = {
            getGameSettings().get( row - 1, col ),  // top
            getGameSettings().get( row, col + 1 ),  // right
            getGameSettings().get( row + 1, col ),  // bottom
            getGameSettings().get( row, col - 1 ),  // left
            getGameSettings().get( row - 1, col + 1 ),  // top-right
            getGameSettings().get( row - 1, col - 1 ),  // top-left
            getGameSettings().get( row + 1, col + 1 ),  // bottom-right
            getGameSettings().get( row + 1, col - 1 )   // bottom-left
        };
        
        for ( Cell cell : cells ){
            if ( cell.getCellType() == CellType.PATH ){
                AStarNode node = new AStarNode( cell.getRow(), cell.getCol() );
                nodes.add( node );
            }
        }
        
        return nodes;
    }
    
    /**
     * Perform the A* algorithm. The destinations are the PATH cells around
     * the chosen charging point
     * @param origin    Origin node
     * @param destinations  Set of destination nodes
     * @return  The shortest path between the origin and destination node
     */
    private List<AStarNode> aStarPathFinderAlgorithm( AStarNode origin, Set<AStarNode> destinations ){
        
        List<AStarNode> path = new ArrayList<>();
        
        List<AStarNode> openList = new ArrayList<>();
        List<AStarNode> closedList = new ArrayList<>();

        openList.add( origin );

        while ( !openList.isEmpty() && path.isEmpty() ) {

            AStarNode node = openList.remove( 0 );
            closedList.add( node );

            // Get adjacent points
            List<AStarNode> adjacents = getAdjacentNodes( node );

            // Check adjacent nodes
            for ( AStarNode adjNode : adjacents ) {

                int adjRow = adjNode.getRow();
                int adjCol = adjNode.getCol();
                Cell adjCell = getGameSettings().get( adjRow, adjCol );
                boolean isPathCell = adjCell.getCellType() == CellType.PATH;
                boolean isInClosedList = closedList.contains( adjNode );
                boolean isInOpenList = openList.contains( adjNode );

                // Destination found
                if ( destinations.contains( adjNode ) ) {
                    AStarNode current = adjNode;
                    path.add( current );
                    while ( current.getParent() != null ) {
                        path.add( current.getParent() );
                        current = current.getParent();
                    }

                    Collections.reverse( path );

                // Check if is path cell and is not already checked
                } else if ( isPathCell && !isInClosedList ) {

                    int gValue = computeGvalue( node );

                    if ( isInOpenList ) {

                        // Check if new g is better than current g
                        if ( adjNode.getgValue() > gValue ) {
                            adjNode.setgValue( gValue );
                            adjNode.setParent( node );
                        }

                    } else {
                        // Set parent node and values
                        int hValue = computeHvalue( adjNode, destinations );
                        
                        adjNode.setParent( node );
                        adjNode.setgValue( gValue );
                        adjNode.sethValue( hValue );

                        // Add node to open list
                        openList.add( adjNode );
                    }
                }
            }
            
            // Sort open list
            Collections.sort( openList );
        }
        
        return path;
    }
    
    /**
     * Get the list of nodes that are adjacent to the given node. The nodes 
     * considered as adjacents are the ones that can be reach from the node in 
     * vertical or horizontal direction (not diagonals)
     * @param node  Node being evaluated
     * @return List of adjacent nodes
     */
    private List<AStarNode> getAdjacentNodes( AStarNode node ){
        
        int nodeRow = node.getRow();
        int nodeCol = node.getCol();

        List<AStarNode> adjacents = new ArrayList<>();
        adjacents.add( new AStarNode(nodeRow - 1, nodeCol) );   // top
        adjacents.add( new AStarNode(nodeRow + 1, nodeCol) );   // bottom
        adjacents.add( new AStarNode(nodeRow, nodeCol - 1) );   // left
        adjacents.add( new AStarNode(nodeRow, nodeCol + 1) );   // right
        
        return adjacents;
    }
    
    /**
     * Compute the G value from the node to the origin
     * @param node  Node being evaluated
     * @return The G value for the node
     */
    private int computeGvalue( AStarNode node ){
        
        int g = 1;
        
        AStarNode current = node;
        while ( current.getParent() != null ) {
            g += 1;
            current = current.getParent();
        }
        
        return g;
    }
    
    /**
     * Compute the H value from the node to the destinations.
     * @param origin    Path node being evaluated
     * @param destinations  Set of destinations to reach
     * @return Smallest Manhattan distance from origin to destinations
     */
    private int computeHvalue( AStarNode origin, Set<AStarNode> destinations ){
        
        int minDist = Integer.MAX_VALUE;
        
        for ( AStarNode dest : destinations ){
            int dist = Math.abs( origin.getRow() - dest.getRow() );
            dist += Math.abs( origin.getCol() - dest.getCol() );
            
            if( minDist > dist ){
                minDist = dist;
            }
        }
        
        return minDist;
    }
}
