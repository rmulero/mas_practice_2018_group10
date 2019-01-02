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
public class RecycleAction extends ActionBase {
    
    private final int wasteRow;
    private final int wasteCol;
    
    public RecycleAction(String agent, String action, String position ) {
        super( agent, action );
        
        String[] coords = position.split( "," );
        this.wasteRow = Integer.valueOf( coords[ 0 ] );
        this.wasteCol = Integer.valueOf( coords[ 1 ] );
    }
    
    public static RecycleAction fromString( String actionStr ){
        String[] parts = actionStr.split( ActionUtils.DELIMITER_PART );
        
        String agent = parts[ 0 ];
        String action = parts[ 1 ];
        String position = parts[ 2 ];
        
        return new RecycleAction(agent, action, position);
    }
    
    public int getWasteRow() {
        return wasteRow;
    }

    public int getWasteCol() {
        return wasteCol;
    }
    
    @Override
    public String toString() {
        
        String[] parts = {
            getAgent(),
            getAction(),
            getWasteRow() + "," + getWasteCol()
        };
        
        String str = String.join( ActionUtils.DELIMITER_PART, parts );
        return str;
    }
}
