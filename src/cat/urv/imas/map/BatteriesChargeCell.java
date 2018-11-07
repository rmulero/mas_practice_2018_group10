/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.map;

import cat.urv.imas.gui.CellVisualizer;

/**
 * Point to charge the batteries of a eSearchar Agent cell
 * @author ImatgeDavid
 */
public class BatteriesChargeCell extends Cell {

    /**
     * Initializes a batteries charge point  
     *
     * @param row row number (zero based).
     * @param col col number (zero based).
     */
    public BatteriesChargeCell(int row, int col) {
        super(CellType.BATTERIES_CHARGE_POINT, row, col);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void draw(CellVisualizer visual) {
        visual.drawBatteriesChargePoint(this);
    }

    @Override
    public String getMapMessage() {
//        return price + ":" + metal.getShortString();
        return null;
    }

    
}
