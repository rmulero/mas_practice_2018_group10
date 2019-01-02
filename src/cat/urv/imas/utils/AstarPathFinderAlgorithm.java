/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.utils;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Rafael Mulero
 */
public class AstarPathFinderAlgorithm {
    
    private final GameSettings gameSettings;
    private AStarNode nodeToAvoid;
    
    public AstarPathFinderAlgorithm( GameSettings gameSettings ){
        this.gameSettings = gameSettings;
    }
    
    public void setNodeToAvoid( AStarNode node ){
        this.nodeToAvoid = node;
    }
    
    public void unsetNodeToAvoid(){
        this.nodeToAvoid = null;
    }
    
    public Set<AStarNode> getSurroundingPathCells( Cell centerCell ){
        
        Set<AStarNode> nodes = new HashSet<>();
        
        int row = centerCell.getRow();
        int col = centerCell.getCol();
        
        int[] rows = { row - 1, row, row + 1 };
        int[] cols = { col - 1, col, col + 1 };
        
        List<Cell> cells = new ArrayList<>();
        for ( int r : rows ){
            for ( int c : cols ){
                try {
                    if ( c != r ){
                        Cell cell = gameSettings.get( r, c );
                        cells.add( cell );
                    }
                } catch ( IndexOutOfBoundsException ex ){
                }
            }
        }
        
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
    public List<AStarNode> findPath( AStarNode origin, Set<AStarNode> destinations ){
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
                Cell adjCell = gameSettings.get( adjRow, adjCol );
                boolean isPathCell = adjCell.getCellType() == CellType.PATH;
                boolean isInClosedList = closedList.contains( adjNode );
                boolean isInOpenList = openList.contains( adjNode );
                boolean isForbidden = adjNode.equals( nodeToAvoid );

                // Destination found
                boolean destinationFound = false;
                for ( AStarNode destNode : destinations ){
                    if ( destNode.equals( adjNode ) ){
                        destinationFound = true;
                    }
                }
                
                if ( destinationFound ) {
                    AStarNode current = adjNode;
                    path.add( current );
                    while ( current.getParent() != null ) {
                        path.add( current.getParent() );
                        current = current.getParent();
                    }

                    Collections.reverse( path );

                // Check if is path cell and is not already checked
                } else if ( isPathCell && !isInClosedList && !isForbidden ) {

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
        
        if ( path.isEmpty() ){
            System.out.print( "" );
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
        
        for ( AStarNode adjNode : adjacents ){
            adjNode.setParent( node );
        }
        
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
