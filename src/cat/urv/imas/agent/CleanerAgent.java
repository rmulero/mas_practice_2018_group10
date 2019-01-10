package cat.urv.imas.agent;

import cat.urv.imas.behaviour.agent.AgentResponseActionsBehaviour;
import cat.urv.imas.behaviour.agent.AgentResponseCnetBehaviour;
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
import cat.urv.imas.utils.actions.CollectAction;
import cat.urv.imas.utils.actions.DetectAction;
import cat.urv.imas.utils.actions.MoveAction;
import cat.urv.imas.utils.actions.ProposeAction;
import cat.urv.imas.utils.actions.RecycleAction;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CleanerAgent extends ImasAgentTuned {
    
    private int maxCapacity = 0;
    private int currentCapacity = 0;
    private int currentRow = 0;
    private int currentCol = 0;
    private String currentWasteType = "";
    
    private AstarPathFinderAlgorithm pathFinder;
    private final List<Cell> recyclingPoints; 
    private final Map<Cell, Integer> assignedWastes;
    private final List<Cell> collectedWastes;
    private final List<AStarNode> wastesPath;
    private final List<String> actions;
    
    private State currentState;
    
    private enum State{
        WAITING, MOVING,
        COLLECTING, RECYCLING
    }
    
    public CleanerAgent() {
        super( AgentType.CLEANER );
        this.recyclingPoints = new ArrayList<>();
        this.assignedWastes = new HashMap<>();
        this.collectedWastes = new ArrayList<>();
        this.wastesPath = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.currentState = State.WAITING;
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
        
        // Add previous agents (Cleaner Coordinator)
        AID cleanerCoordinator = searchAgent( AgentType.CLEANER_COORDINATOR.toString() );
        addPreviousAgent( cleanerCoordinator );
        
        // Add behaviour
        waitForCfp();
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
        
        // Get capacity
        this.maxCapacity = gameSettings.getCleanerCapacity();
        this.currentCapacity = gameSettings.getCleanerCapacity();
        
        // Get recycling points
        List<Cell> recycle = gameSettings.getCellsOfType().get( CellType.RECYCLING_POINT_CENTER );
        this.recyclingPoints.addAll( recycle );
        
        // Get agent position
        List<Cell> agents = gameSettings.getAgentList().get( this.type );
        String localName = getLocalName();
        localName = localName.replace("clag", "");
        int index = Integer.valueOf( localName );
        
        Cell agentCell = agents.get( index );
        this.currentRow = agentCell.getRow();
        this.currentCol = agentCell.getCol();
        
        PathCell pc = (PathCell) agentCell;
        InfoAgent a = pc.getAgents().get( type ).get( 0 );
        a.setAID( this.getAID() );
        
        // Log agent info
        log( "Position [" + currentRow + "," + currentCol + "]" );
        log( "Capacity (" + maxCapacity + ")" );
        
        // Set the pathfinder algorithm
        this.pathFinder = new AstarPathFinderAlgorithm( gameSettings );
        
        // Assign the closest recycling point
        Cell closestRecyclingPoint = null;
        int pathSize = Integer.MAX_VALUE;
        
        for ( Cell recPoint : recyclingPoints ){
            
            AStarNode origin = new AStarNode( currentRow, currentCol );
            Set<AStarNode> destinations = pathFinder.getSurroundingPathCells( recPoint );
            List<AStarNode> path = pathFinder.findPath(origin, destinations);
            if ( pathSize > path.size() ){
                pathSize = path.size();
                closestRecyclingPoint = recPoint;
            }
        }
        
        recyclingPoints.clear();
        recyclingPoints.add( closestRecyclingPoint );
        
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
        addBehaviour( new AgentResponseUpdatesBehaviour( this, template ) );
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
                    
                    AStarNode node = wastesPath.get( 1 );
                    boolean updatedPath = ( node.getRow() == currentRow && 
                            node.getCol() == currentCol );

                    if ( updatedPath ){
                        wastesPath.remove( 0 );
                    }
                }
            }
        }
        
        waitForActionRequest();
    }
    
    private void waitForCfp(){
        
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol( FIPANames.InteractionProtocol.FIPA_CONTRACT_NET ),
                MessageTemplate.MatchPerformative( ACLMessage.CFP )
        );
        
        // Add behaviour to wait for ContractNet
        addBehaviour( new AgentResponseCnetBehaviour( this, template ) );
    }

    @Override
    public ACLMessage onCfpReceived( ACLMessage cfp ) {
        
        // Get the detections
        String content = cfp.getContent();
        List<String> actions = Arrays.asList( content.split( ActionUtils.DELIMITER_ACTION ) );
        List<DetectAction> detections = new ArrayList<>();
        
        for ( String action : actions ){
            detections.add( DetectAction.fromString( action ) );
        }
        
        // Keeps the detections of the current waste
        if ( !currentWasteType.isEmpty() ){
            for ( int i = 0; i < detections.size(); ++i ){
                DetectAction dAct = detections.get( i );
                if ( !dAct.getWasteType().equalsIgnoreCase( currentWasteType ) ){
                    detections.remove( i );
                    --i;
                }
            }
        }
        
        // Send the reply
        ACLMessage reply = cfp.createReply();
        
        DetectAction detectAction = evaluateDetections( detections );
        if ( detectAction == null ){
            reply.setPerformative( ACLMessage.REFUSE );
            
        } else {
            // Generate the proposal content
            int wRow = detectAction.getWasteRow();
            int wCol = detectAction.getWasteCol();
            Cell wasteCell = getGameSettings().get( wRow, wCol );
            
            int steps;
            
            if ( wastesPath.isEmpty() ){
                List<AStarNode> path = generatePath( wasteCell );
                steps = path.size();
            } else {
                int currentPathVariation = checkCurrentPath( wasteCell ) * 2;
                steps = wastesPath.size() + currentPathVariation;
            }
            
            // Build the proposal
            ProposeAction proposal = new ProposeAction(
                    getLocalName(),
                    ActionUtils.ACTION_PROPOSE,
                    wRow + "," + wCol,
                    detectAction.getWasteType(),
                    String.valueOf( steps ),
                    String.valueOf( currentCapacity )
            );
            
            // Set the content
            reply.setPerformative( ACLMessage.PROPOSE );
            reply.setContent( proposal.toString() );
        }
        
        waitForCfp();
        
        return reply;
    }

    @Override
    public void onProposalAccepted( ACLMessage accept ) {
        
        // Get the proposal content
        String content = accept.getContent();
        ProposeAction proposal = ProposeAction.fromString( content );
        
        // Set the waste type
        currentWasteType = proposal.getWasteType();
        
        // Get the waste cell
        int wRow = proposal.getWasteRow();
        int wCol = proposal.getWasteCol();
        FieldCell wasteCell = (FieldCell) getGameSettings().get( wRow, wCol );
        WasteType wasteType = WasteType.fromShortString( currentWasteType );
        Integer amount = wasteCell.getWaste().get( wasteType );
        
        assignedWastes.put( wasteCell, amount );
        
        // Create or modify the path
        switch( currentState ){
            case WAITING:
                List<AStarNode> path = generatePath( wasteCell );
                wastesPath.clear();
                wastesPath.addAll( path );
                currentState = State.MOVING;
                break;
                
            case MOVING:
            case COLLECTING:
                modifyPath( wasteCell );
                break;
        }
    }
    
    /////////////////////////////////////
    //      ACTIONS
    /////////////////////////////////////
    private void performActions(){
        
        actions.clear();
        
        switch( currentState ){
            case MOVING:
                performMovingActions();
                break;
                
            case COLLECTING:
                performCollectingActions();
                break;
                
            case RECYCLING:
                performRecyclingActions();
                break;
                
            case WAITING:
            default:
                break;
        }
    }
    
    /**
     * Actions performed when state is SEARCHING
     */
    private void performMovingActions(){
        detectAssignedWastes();
        moveAttempt();
    }
    
    /**
     * Actions performed when state is COLLECTING
     */
    private void performCollectingActions(){
        collectWastes();
    }
    
    /**
     * Actions performed when state is RECYCLING
     */
    private void performRecyclingActions(){
        recycleWastes();
    }
    
    /**
     * Detect assigned wastes around the current position
     */
    private void detectAssignedWastes(){
        
        int[] rows = { currentRow - 1, currentRow, currentRow + 1 };
        int[] cols = { currentCol - 1, currentCol, currentCol + 1 };
        
        for ( int r : rows ){
            for ( int c : cols ){
                
                boolean currentCell = ( r == currentRow && c == currentCol );
                
                if ( !currentCell ){
                    Cell cell = getGameSettings().get( r, c );
                    boolean assigned = assignedWastes.keySet().contains( cell );
                    boolean collected = collectedWastes.contains( cell );
                    if ( assigned && !collected ){
                        currentState = State.COLLECTING;
                        
                        int amount = assignedWastes.get( cell );
                        CollectAction action = new CollectAction(
                                getLocalName(),
                                ActionUtils.ACTION_COLLECT_START,
                                cell.getRow() + "," + cell.getCol(),
                                currentWasteType,
                                String.valueOf( amount )
                        );

                        actions.add( action.toString() );
                    }
                }
            }
        }
    }
    
    private void moveAttempt(){
        if ( currentState == State.MOVING ){
            followPath();
        }
    }
    
    /**
     * Move according to the path. In case that the agent has reached to the 
     * charging point, it will start to charge the battery.
     */
    private void followPath(){
        
        if ( wastesPath.size() == 1 ){
            currentState = State.RECYCLING;
            
        } else if ( wastesPath.size() > 1 ){
            
            AStarNode node = wastesPath.get( 0 );
            if ( node.getRow() == currentRow && node.getCol() == currentCol ){
                node = wastesPath.get( 1 );
                
            } else if ( wastesPath.size() > 2 ){
                pathFinder.setNodeToAvoid( wastesPath.get( 1 ) );
                AStarNode currentOrigin = new AStarNode( currentRow, currentCol );
                Set<AStarNode> destination = new HashSet<>();
                destination.add( wastesPath.get( 2 ) );
                List<AStarNode> deviation = pathFinder.findPath( currentOrigin, destination );
                pathFinder.unsetNodeToAvoid();
                
                wastesPath.remove( 2 );
                wastesPath.remove( 1 );
                wastesPath.remove( 0 );
                
                deviation.addAll( wastesPath );
                wastesPath.clear();
                wastesPath.addAll( deviation );
                node = wastesPath.get( 1 );
                
            } else {
                node = null;
            }
            
            if ( node != null ){
                String originStr = currentRow + "," + currentCol;
                String destinationStr = node.getRow() + "," + node.getCol();

                // Build action string
                MoveAction action = new MoveAction(
                        getLocalName(),
                        ActionUtils.ACTION_MOVE,
                        originStr,
                        destinationStr,
                        String.valueOf( wastesPath.size() ),
                        ActionUtils.FLAG_NEED_CHARGE
                );

                actions.add( action.toString() );
            }
        }
    }
    
    /**
     * Collect the wastes near the current position
     */
    private void collectWastes(){
        
        boolean pendingWastes = false;
        int[] rows = { currentRow - 1, currentRow, currentRow + 1 };
        int[] cols = { currentCol - 1, currentCol, currentCol + 1 };
        
        for ( int r : rows ){
            for ( int c : cols ){
                
                boolean currentCell = ( r == currentRow && c == currentCol );
                if ( !currentCell ){
                    Cell cell = getGameSettings().get( r, c );
                    boolean assigned = assignedWastes.keySet().contains( cell );
                    boolean collected = collectedWastes.contains( cell );
                    if ( assigned && !collected ){
                        pendingWastes = true;
                        FieldCell fCell = (FieldCell) cell;
                
                        fCell.removeWaste();
                        --currentCapacity;

                        if ( fCell.getWaste().isEmpty() ){
                            collectedWastes.add( cell );
                            
                            int amount = assignedWastes.get( cell );
                            CollectAction action = new CollectAction(
                                    getLocalName(),
                                    ActionUtils.ACTION_COLLECT_END,
                                    cell.getRow() + "," + cell.getCol(),
                                    currentWasteType,
                                    String.valueOf( amount )
                            );

                            actions.add( action.toString() );
                        }
                    }
                }
            }
        }
        
        if ( !pendingWastes ){
            currentState = State.MOVING;
        }
    }
    
    /**
     * Recycle the wastes
     */
    private void recycleWastes(){
        
        for ( Cell wasteCell : assignedWastes.keySet() ){
            
            Integer amount = assignedWastes.get( wasteCell );
            
            RecycleAction action = new RecycleAction(
                    getLocalName(),
                    ActionUtils.ACTION_RECYCLE,
                    wasteCell.getRow() + "," + wasteCell.getCol(),
                    currentWasteType,
                    String.valueOf( amount )
            );

            actions.add( action.toString() );
        }
        
        assignedWastes.clear();
        collectedWastes.clear();
        
        currentState = State.WAITING;
        currentWasteType = "";
        currentCapacity = maxCapacity;
    }
    
    /**
     * Decide which of the detected wastes will be collected
     * @param detections List of detected wastes
     * @return The waste to collect. Null if the agent can't collect any of them
     */
    private DetectAction evaluateDetections( List<DetectAction> detections ){
        
        int pathSize = Integer.MAX_VALUE;
        DetectAction chosenAction = null;
        
        for ( DetectAction detection : detections ){
            int wRow = detection.getWasteRow();
            int wCol = detection.getWasteCol();
            
            Cell wasteCell = getGameSettings().get( wRow, wCol );
            
            switch( currentState ){
                case WAITING:
                    List<AStarNode> path = generatePath( wasteCell );
                    if ( pathSize > path.size() && !path.isEmpty() ){
                        pathSize = path.size();
                        chosenAction = detection;
                    }
                    break;
                    
                case MOVING:
                case COLLECTING:
                    // Check the waste type
                    boolean sameType = detection.getWasteType().equalsIgnoreCase( currentWasteType );

                    if ( sameType ){
                        
                        // Check the capacity
                        int simulatedCapacity = 0;
                        for ( Cell cell : assignedWastes.keySet() ){
                            Integer amount = assignedWastes.get( cell );
                            simulatedCapacity += amount;
                        }
                        
                        simulatedCapacity += detection.getWasteAmount();

                        boolean notOverCapacity = simulatedCapacity <= maxCapacity;

                        // Check if the waste is in the current path
                        int currentPathVariation = checkCurrentPath( wasteCell ); 
                        boolean isInCurrentPath = currentPathVariation <= 1;

                        if ( notOverCapacity && isInCurrentPath ){
                            chosenAction = detection;
                        }
                    }
                    
                    break;
                    
                case RECYCLING:
                default:
                    break;
            }
        }
        
        return chosenAction;
    }
    
    /**
     * Generate the path to the given waste and to the recycling point. The 
     * recycling point is the closest recycling point to the given waste
     * @param waste Cell of the detected waste
     * @return Path for bringing the waste to the recycling point
     */
    private List<AStarNode> generatePath( Cell waste ){
        
        if ( getLocalName().equalsIgnoreCase( "clag0" ) ){
            System.out.print( "" );
        }
        
        // Get path from origin to waste
        AStarNode origin = new AStarNode( currentRow, currentCol );
        Set<AStarNode> wasteNodes = pathFinder.getSurroundingPathCells( waste );
        List<AStarNode> wastePath = pathFinder.findPath( origin, wasteNodes );
        
        // Get path from waste to recycling point
        if ( !wastePath.isEmpty() ){
            AStarNode wasteNode = wastePath.remove( wastePath.size() - 1 );
            wasteNode.setParent( null );
            Cell closestPoint = getClosestRecyclingPoint( wasteNode );
            Set<AStarNode> recPointNodes = pathFinder.getSurroundingPathCells( closestPoint );
            List<AStarNode> recPointPath = pathFinder.findPath( wasteNode, recPointNodes );
            
            // Merge paths
            wastePath.addAll( recPointPath );
        }
        
        return wastePath;
    }
    
    /**
     * Get the closest recycling point for the given origin cell
     * @param origin Cell of the origin point to the recycling point
     * @return Cell of the closest recycling point for the given origin
     */
    private Cell getClosestRecyclingPoint( AStarNode origin ){
        
        Cell chosenPoint = null;
        int pathSteps = Integer.MAX_VALUE;
        
        for ( Cell recyclingPoint : recyclingPoints ){
            Set<AStarNode> destinations = pathFinder.getSurroundingPathCells( recyclingPoint );
            
            List<AStarNode> path = pathFinder.findPath( origin, destinations );
            if ( pathSteps > path.size() ){
                pathSteps = path.size();
                chosenPoint = recyclingPoint;
            }
        }
        
        return chosenPoint;
    }
    
    /**
     * Check if a waste is in the current path with the minimum deviation
     * @param newWaste Cell to check
     * @return Minimum difference between the path and the given node
     */
    private int checkCurrentPath( Cell newWaste ){
        
        boolean found = false;
        int minDifference = Integer.MAX_VALUE;
        
        Set<AStarNode> wasteNodes = pathFinder.getSurroundingPathCells( newWaste );
        
        for ( int i = 0; i < wastesPath.size() && !found; ++i ){
            
            AStarNode node = wastesPath.get( i );
            for ( AStarNode wNode : wasteNodes ){
                int difference = Math.abs( node.getRow() - wNode.getRow() ) + 
                        Math.abs( node.getCol() - wNode.getCol() );
                
                if ( difference < minDifference ){
                    minDifference = difference;
                }
            }
            
            if ( minDifference <= 1 ){
                found = true;
            }
        }
        
        return minDifference;
    }
    
    /**
     * Modify the current path to include the given cell
     * @param newWaste Cell to include in the path
     */
    private void modifyPath( Cell newWaste ){
        
        boolean found = false;
        int minDifference = Integer.MAX_VALUE;
        int minDiffIndex = Integer.MAX_VALUE;
        AStarNode minDiffNode = null;
        
        Set<AStarNode> wasteNodes = pathFinder.getSurroundingPathCells( newWaste );
        
        for ( int i = 0; i < wastesPath.size() && !found; ++i ){
            
            AStarNode node = wastesPath.get( i );
            for ( AStarNode wNode : wasteNodes ){
                int difference = Math.abs( node.getRow() - wNode.getRow() ) + 
                        Math.abs( node.getCol() - wNode.getCol() );
                
                if ( difference < minDifference ){
                    minDifference = difference;
                    minDiffNode = wNode;
                    minDiffIndex = i;
                }
            }
            
            if ( minDifference <= 1 ){
                found = true;
            }
        }
        
        if ( found && minDifference == 1 ){
            AStarNode node = wastesPath.get( minDiffIndex );
            wastesPath.add( minDiffIndex + 1, minDiffNode );
            wastesPath.add( minDiffIndex + 2, node );
        }
    }
}
