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
package cat.urv.imas.ontology;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Current game settings. Cell coordinates are zero based: row and column values
 * goes from [0..n-1], both included.
 *
 * Run the class GenerateGameSettings to build the game.settings configuration file.
 *
 */
@XmlRootElement(name = "GameSettings")
public class GameSettings implements java.io.Serializable {

    /* Default values set to all attributes, just in case. */
    /**
     * Seed for random numbers.
     */
    private long seed = 0;
    
    /**
     * Total number of simulation steps.
     */
    private int simulationSteps = 100;
    /**
     * City map.
     */
    protected Cell[][] map;
    
    
    protected int maxAmountOfWastes = 10;
    
    public int getMaxAmountOfWastes() {
        return this.maxAmountOfWastes;
    }
    
    @XmlElement(required=true)
    public void setMaxAmountOfWastes(int maxAmountOfWastes) {
        this.maxAmountOfWastes = maxAmountOfWastes;
    }
            
    /**
     * probability of having a waste in the city at every step of the simulation
     * the range is: 0 (minimum) - 100 (maximum)
     */
    protected int newWasteProbability = 30;
        
    /**
     * Get the value of the newWasteProbability
     * @return current value of newWasteProbability
     */
    public int getNewWasteProbability() {
        return newWasteProbability;
    }

    @XmlElement(required=true)
    public void setNewWasteProbability(int newWasteProbability) {
        this.newWasteProbability = newWasteProbability;
    }

    
    /**
     * in the case of a cell with waste, this value represents the 
     * probability of having a municipal waste (it means that the probability, 
     * of having an industrial is 100 - this value
     * the range is: 0 (minimum) - 100 (maximum)
     */
    protected int probabilityMunicipalWaste = 50; 
    
    /**
     * Get the value of the probabilityMunicipalWaste
     * @return current value of probabilityMunicipalWaste
     */
    public int getProbabilityMunicipalWaste() {
        return newWasteProbability;
    }

    @XmlElement(required=true)
    public void setProbabilityMunicipalWaste(int probabilityMunicipalWaste) {
        this.probabilityMunicipalWaste = probabilityMunicipalWaste;
    }
        
    /**
     * number of wastes that can carry on a cleaner 
     */
    protected int cleanerCapacity = 12;

    public int getCleanerCapacity() {
        return cleanerCapacity;
    }
    
    @XmlElement(required=true)
    public void setCleanerCapacity(int cleanerCapacity) {
        this.cleanerCapacity = cleanerCapacity;
    }
    
    
    /**
     * number of steps that can go a e-searcher without charging its batteries 
     */
    protected int eSearcherMaxSteps = 20;

    public int geteSearcherMaxSteps() {
        return eSearcherMaxSteps;
    }

    @XmlElement(required=true)    
    public void seteSearcherMaxSteps(int eSearcherMaxSteps) {
        this.eSearcherMaxSteps = eSearcherMaxSteps;
    }
    
    /**
     * Computed summary of the position of agents in the city. For each given
     * type of mobile agent, we get the list of their positions.
     */
    protected Map<AgentType, List<Cell>> agentList;
    /**
     * Title to set to the GUI.
     */
    protected String title = "Default game settings (1819)";
    /**
     * List of cells per type of cell.
     */
    protected Map<CellType, List<Cell>> cellsOfType;

    /**
     * Method to intialise the see of the random class
     * @return 
     */
    public long getSeed() {
        return seed;
    }

    @XmlElement(required = true)
    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getSimulationSteps() {
        return simulationSteps;
    }

    @XmlElement(required = true)
    public void setSimulationSteps(int simulationSteps) {
        this.simulationSteps = simulationSteps;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement(required=true)
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the full current city map.
     * @return the current city map.
     */
    @XmlTransient
    public Cell[][] getMap() {
        return map;
    }

    public Cell[] detectFieldsWithWaste(int row, int col) {
        //TODO: find all surrounding cells to (row,col) that are
        //      buildings and have garbage on it.
        //      Use: FieldCell.detectMetal() to do so.  FieldCell.detectWaste()
        
        Cell[] cells = {
            map[ row - 1 ][ col ], // top
            map[ row ][ col + 1 ], // right
            map[ row + 1 ][ col ], // bottom
            map[ row ][ col - 1 ], // left
            map[ row - 1 ][ col + 1 ], // top-right
            map[ row - 1 ][ col - 1 ], // top-left
            map[ row + 1 ][ col + 1 ], // bottom-right
            map[ row + 1 ][ col - 1 ] // bottom-left
        };
        
        for ( int i = 0; i < cells.length; ++i ){
            Cell cell = cells[ i ];
            
            if ( cell.getCellType() == CellType.FIELD ){
                FieldCell fc = (FieldCell) cell;
                
                if ( fc.detectWaste().isEmpty() ){
                    cells[ i ] = null;
                }
            } else {
                cells[ i ] = null;
            }
        }
        
        return cells;
    }

    /**
     * Gets the cell given its coordinate.
     * @param row row number (zero based)
     * @param col column number (zero based).
     * @return a city's Cell.
     */
    public Cell get(int row, int col) {
        return map[row][col];
    }

    @XmlTransient
    public Map<AgentType, List<Cell>> getAgentList() {
        return agentList;
    }

    public void setAgentList(Map<AgentType, List<Cell>> agentList) {
        this.agentList = agentList;
    }

    public String toString() {
        //TODO: show a human readable summary of the game settings.
        
        StringBuilder sb = new StringBuilder();
        sb.append( "---------------------------\n" );
        sb.append( "Agent positions\n");
        sb.append( "---------------------------\n" );
        for ( AgentType type : agentList.keySet() ){
            List<Cell> cells = agentList.get( type );
            sb.append( type ).append( " :" );
            for ( Cell cell : cells ) {
                sb.append( " [" ).append( cell.getRow() );
                sb.append( "," ).append( cell.getCol() );
                sb.append( "]" );
            }
            sb.append( "\n" );
        }
        
        sb.append( "---------------------------\n" );
        sb.append( "Charging / Recycling points\n" );
        sb.append( "---------------------------\n" );
        CellType[] cellTypes = {CellType.BATTERIES_CHARGE_POINT, CellType.RECYCLING_POINT_CENTER};
        for ( CellType type : cellTypes ) {
            List<Cell> cells = cellsOfType.get( type );
            sb.append( type ).append( " :" );
            for ( Cell cell : cells ) {
                sb.append( " [" ).append( cell.getRow() );
                sb.append( "," ).append( cell.getCol() );
                sb.append( "]" );
            }
            sb.append( "\n" );
        }
        sb.append( "---------------------------" );
        
        return sb.toString();
    }

    public String getShortString() {
        //TODO: list of agents
        return "Game settings: agent related string";
    }

    @XmlTransient
    public Map<CellType, List<Cell>> getCellsOfType() {
        return cellsOfType;
    }

    public void setCellsOfType(Map<CellType, List<Cell>> cells) {
        cellsOfType = cells;
    }

    public int getNumberOfCellsOfType(CellType type) {
        return cellsOfType.get(type).size();
    }

    public int getNumberOfCellsOfType(CellType type, boolean empty) {
        int max = 0;
        for(Cell cell : cellsOfType.get(type)) {
            if (cell.isEmpty()) {
                max++;
            }
        }
        return max;
    }

}
