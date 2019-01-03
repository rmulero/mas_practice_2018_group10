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
    private final String wasteType;
    private final int wasteAmount;
    
    public RecycleAction(String agent, String action, String position, String type, String amount ) {
        super( agent, action );
        
        String[] coords = position.split( "," );
        this.wasteRow = Integer.valueOf( coords[ 0 ] );
        this.wasteCol = Integer.valueOf( coords[ 1 ] );
        
        this.wasteType = type;
        this.wasteAmount = Integer.valueOf( amount );
    }
    
    public static RecycleAction fromString( String actionStr ){
        String[] parts = actionStr.split( ActionUtils.DELIMITER_PART );
        
        String agent = parts[ 0 ];
        String action = parts[ 1 ];
        String position = parts[ 2 ];
        String type = parts[ 3 ];
        String amount = parts[ 4 ];
        
        return new RecycleAction(agent, action, position, type, amount);
    }
    
    public int getWasteRow() {
        return wasteRow;
    }

    public int getWasteCol() {
        return wasteCol;
    }

    public String getWasteType() {
        return wasteType;
    }

    public int getWasteAmount() {
        return wasteAmount;
    }
    
    @Override
    public String toString() {
        
        String[] parts = {
            getAgent(),
            getAction(),
            getWasteRow() + "," + getWasteCol(),
            getWasteType(),
            String.valueOf( getWasteAmount() )
        };
        
        String str = String.join( ActionUtils.DELIMITER_PART, parts );
        return str;
    }
}
