/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.map;

import cat.urv.imas.gui.CellVisualizer;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;

/**
 * Recyling point cell
 * @author ImatgeDavid
 */
public class RecyclerPointCell extends Cell {


    /**
     * Initializes a cell; a recycling point treats any type of waste (industrial or municipal) 
     *
     * @param row row number (zero based).
     * @param col col number (zero based).
     */
    public RecyclerPointCell(int row, int col) {
        super(CellType.RECYCLING_POINT_CENTER, row, col);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void draw(CellVisualizer visual) {
        visual.drawRecyclingPoint(this);
    }

    @Override
    public String getMapMessage() {
//        return price + ":" + metal.getShortString();
        return null;
    }

    
}
