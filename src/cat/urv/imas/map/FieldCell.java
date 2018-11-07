/**
 * IMAS base code for the practical work.
 * Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.map;

import cat.urv.imas.gui.CellVisualizer;
import cat.urv.imas.ontology.WasteType;
import java.util.HashMap;
import java.util.Map;

/**
 * Field cell.
 */
public class FieldCell extends Cell {

    /**
     * When a waste is not found yet, an empty list is returned.
     */
    protected static Map<WasteType, Integer> empty = new HashMap();

    /**
     * Waste of the field: it can only be of one type at a time.
     * But, once generated, it can be of any type and amount.
     */
    protected Map<WasteType, Integer> waste;
    
    /**
     * If true, prospectors have found this metal. false when prospectors have
     * to find it yet.
     */
    protected boolean found = false;

    /**
     * Builds a cell corresponding to a field.
     *
     * @param row row number.
     * @param col column number.
     */
    public FieldCell(int row, int col) {
        super(CellType.FIELD, row, col);
        waste = new HashMap();
    }

    /**
     * Detects waste on this field.
     * @return the waste on it
     */
    public Map<WasteType, Integer> detectWaste() {
        found = (!waste.isEmpty());
        return waste;
    }

    /**
     * Whenever the waste has been detected, it informs about the
     * current type on this field. 
     * @return the waste on it.
     */
    public Map<WasteType, Integer> getWaste() {
        return (found) ? waste : empty;
    }

    @Override
    public boolean isEmpty() {
        return found;
    }
   
    /**
     * remove a waste from a location; in each step of the simulation
     * the units of waste are decreased until the field is empty
     */
    public void removeWaste() {
        if (found && waste.size() > 0) {
            for (Map.Entry<WasteType, Integer> entry: waste.entrySet()) {
                if (entry.getValue() == 1) {
                    waste.clear();
                    found = false;
                } else {
                    waste.replace(entry.getKey(), entry.getValue()-1);
                }
            }
        }
    }

    /* ***************** Map visualization API ********************************/

    @Override
    public void draw(CellVisualizer visual) {
        visual.drawField(this);
    }

    /**
     * Shows the type of metal and the amount of it, with the form:
     * <pre>
     *    {type}:{amount}
     * </pre>
     * or an empty string if no element is present. A star is placed at the end
     * of the string if the wate is found by a searcher.
     * @return String detail of the element (waste) present in a field.
     */
    @Override
    public String getMapMessage() {
        if (waste.isEmpty()) {
            return "";
        }
        for (Map.Entry<WasteType, Integer> entry: waste.entrySet()) {
            return entry.getKey().getShortString() + ":" + entry.getValue() +
                    ((found) ? "*" : "");
        }
        return "";
    }
}
