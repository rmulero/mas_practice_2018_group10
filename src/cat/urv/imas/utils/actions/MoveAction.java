/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.utils.actions;

import cat.urv.imas.utils.ActionUtils;

/**
 *
 * @author Rafael Mulero
 */
public class MoveAction extends ActionBase {
    
    private final int originRow;
    private final int originCol;
    private int destinationRow;
    private int destinationCol;
    private final int autonomy;
    private final String flag;
    
    public MoveAction( String agent, String action, String origin, String destination, String autonomy, String flag ) {
        super( agent, action );
        
        String[] coords = origin.split( "," );
        this.originRow = Integer.valueOf( coords[ 0 ] );
        this.originCol = Integer.valueOf( coords[ 1 ] );
        
        coords = destination.split( "," );
        this.destinationRow = Integer.valueOf( coords[ 0 ] );
        this.destinationCol = Integer.valueOf( coords[ 1 ] );
        
        this.autonomy = Integer.valueOf( autonomy );
        
        this.flag = flag;
    }
    
    public MoveAction( String agent, String action, String origin, String destination, String autonomy ) {
        this( agent, action, origin, destination, autonomy, "" );
    }

    public static MoveAction fromString( String actionStr ){
        String[] parts = actionStr.split( ActionUtils.DELIMITER_PART );
        
        String agent = parts[ 0 ];
        String action = parts[ 1 ];
        String origin = parts[ 2 ];
        String destination = parts[ 3 ];
        String flag = "";
        if ( parts.length == 5 ){
            flag = parts[ 4 ];
        }
        
        return new MoveAction(agent, action, origin, destination, flag);
    }
    
    public int getOriginRow() {
        return originRow;
    }

    public int getOriginCol() {
        return originCol;
    }

    public int getDestinationRow() {
        return destinationRow;
    }

    public void setDestinationRow(int destinationRow) {
        this.destinationRow = destinationRow;
    }

    public int getDestinationCol() {
        return destinationCol;
    }

    public void setDestinationCol(int destinationCol) {
        this.destinationCol = destinationCol;
    }

    public int getAutonomy() {
        return autonomy;
    }

    public String getFlag() {
        return flag;
    }

    @Override
    public String toString() {
        
        String[] parts = {
            getAgent(),
            getAction(),
            getOriginRow() + "," + getOriginCol(),
            getDestinationRow() + "," + getDestinationCol(),
            String.valueOf( getAutonomy() )
        };
        
        String str = String.join( ActionUtils.DELIMITER_PART, parts );
        if ( !getFlag().isEmpty() ){
            str += ":" + getFlag();
        }
        
        return str;
    }
}
