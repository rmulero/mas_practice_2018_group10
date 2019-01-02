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
public class ProposeAction extends ActionBase {
    
    private final int wasteRow;
    private final int wasteCol;
    private final String wasteType;
    private final int steps;
    private final int capacity;
    
    public ProposeAction( String agent, String action, String position, String type, String steps, String capacity ) {
        super( agent, action );
        
        String[] coords = position.split( "," );
        this.wasteRow = Integer.valueOf( coords[ 0 ] );
        this.wasteCol = Integer.valueOf( coords[ 1 ] );
        
        this.wasteType = type;
        this.steps = Integer.valueOf( steps );
        this.capacity = Integer.valueOf( capacity );
    }
    
    public static ProposeAction fromString( String actionStr ){
        String[] parts = actionStr.split( ActionUtils.DELIMITER_PART );
        
        String agent = parts[ 0 ];
        String action = parts[ 1 ];
        String position = parts[ 2 ];
        String type = parts[ 3 ];
        String steps = parts[ 4 ];
        String capacity = parts[ 5 ];
        
        return new ProposeAction(agent, action, position, type, steps, capacity);
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
    
    public int getSteps() {
        return steps;
    }

    public int getCapacity() {
        return capacity;
    }
    
    @Override
    public String toString() {
        
        String[] parts = {
            getAgent(),
            getAction(),
            getWasteRow() + "," + getWasteCol(),
            getWasteType(),
            String.valueOf( getSteps() ),
            String.valueOf( getCapacity() )
        };
        
        String str = String.join( ActionUtils.DELIMITER_PART, parts );
        return str;
    }
}
