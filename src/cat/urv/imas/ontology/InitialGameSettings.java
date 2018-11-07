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
import cat.urv.imas.ontology.CleanerInfoAgent;
import cat.urv.imas.map.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Initial game settings and automatic loading from file.
 *
 * Use the GenerateGameSettings to build the game.settings configuration file.
 */
@XmlRootElement(name = "InitialGameSettings")
public class InitialGameSettings extends GameSettings {

    /**
     * Path cell.
     */
    public static final int P = 0;
    /**
     * Cleaner cell.
     */
    public static final int CC = -1;
    /**
     * eSearcher cell.
     */
    public static final int SC = -2;
    /**
     * Reciclying point cell.
     */
    public static final int RPC = -3;
    /**
     * Field cell.
     */
    public static final int F = -4;
     /**
     * Batteries charge cell.
     */
    public static final int BCC = -5;
    
  
    
    /**
     * City initialMap. Each number is a cell. The type of each is expressed by a
     * constant 
     * - current configuration: 4 searchers, 8 cleaners, 
     *    4 recycling points, and 3 charging points
     */
    private int[][] initialMap
            = {
                {F, F,  F,  RPC, F,   F,  F,  F,  F,   F,  F,  F,  F, F, F, F,   F, F,  F,  F},
                {F, P,  P,  P,   P,   P,  P,  P,  P,   P,  P,  CC, P, P, P, P,   P, P,  P,  F},
                {F, P,  SC, P,   P,   P,  P,  CC, P,   P,  P,  P,  P, P, P, P,   P, P,  CC, F},
                {F, P,  P,  F,   F,   F,  F,  F,  F,   P,  P,  F,  F, F, F, F,   F, F,  F,  F},
                {F, P,  P,  F,   F,   F,  F,  F,  RPC, P,  P,  F,  F, F, F, F,   F, F,  F,  F},
                {F, SC, P,  F,   F,   P,  P,  P,  P,   P,  P,  F,  F, P, P, P,   P, P,  P,  F},
                {F, P,  P,  F,   F,   P,  P,  P,  P,   P,  P,  F,  F, P, P, P,   P, P,  P,  F},
                {F, P,  P,  F,   F,   P,  P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,   P,  CC, F,  F,   P,  P,  F,  F, P, P, BCC, F, P,  P,  F},
                {F, P,  P,  F,   F,   P,  P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,   P,  P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,   P,  P,  F,  F,   P,  SC, F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,   P,  P,  F,  F,   P,  P,  F,  F, P, P, RPC, F, P,  P,  F},
                {F, P,  P,  BCC, BCC, P,  P,  F,  F,   P,  P,  F,  F, P, P, F,   F, CC, P,  F},
                {F, P,  P,  F,   F,   P,  P,  F,  F,   P,  P,  F,  F, P, P, F,   F, CC, P,  F},
                {F, P,  P,  F,   F,   P,  P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  RPC, F,   P,  P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  SC, P,   CC,  P,  P,  F,  F,   P,  P,  P,  P, P, P, F,   F, P,  P,  F},
                {F, P,  P,  P,   P,   P,  P,  F,  F,   P,  P,  P,  P, P, P, F,   F, CC, P,  F},
                {F, F,  F,  F,   F,   F,  F,  F,  F,   F,  F,  F,  F, F, F, F,   F, F,  F,  F},
            };

    /**
     * Number of initial elements to put in the map.
     */
    private int numberInitialElements = 0;
    /**
     * Number of those initial elements which will be visible from
     * the very beginning. At maximum, this value will be numberInitialElements.
     * @see numberInitialElements
     */
    private int numberVisibleInitialElements = 0;
    /**
     * Random number generator.
     */
    private Random numberGenerator;

    @XmlElement(required = true)
    public void setNumberInitialElements(int initial) {
        numberInitialElements = initial;
    }

    public int getNumberInitialElements() {
        return numberInitialElements;
    }

    @XmlElement(required = true)
    public void setNumberVisibleInitialElements(int initial) {
        numberVisibleInitialElements = initial;
    }

    public int getNumberVisibleInitialElements() {
        return numberVisibleInitialElements;
    }

    public int[][] getInitialMap() {
        return initialMap;
    }

    @XmlElement(required = true)
    public void setInitialMap(int[][] initialMap) {
        this.initialMap = initialMap;
    }

    public static final InitialGameSettings load(String filename) {
        if (filename == null) {
            filename = "game.settings";
        }
        try {
            // create JAXBContext which will be used to update writer
            JAXBContext context = JAXBContext.newInstance(InitialGameSettings.class);
            Unmarshaller u = context.createUnmarshaller();
            InitialGameSettings starter = (InitialGameSettings) u.unmarshal(new FileReader(filename));
            starter.initMap();
            return starter;
        } catch (Exception e) {
            System.err.println("Loading of settings from file '" + filename + "' failed!");
            System.exit(-1);
        }
        return null;
    }

    /**
     * Initializes the cell map.
     * @throws Exception if some error occurs when adding agents.
     */
    private void initMap() throws Exception {
        int rows = this.initialMap.length;
        int cols = this.initialMap[0].length;
        map = new Cell[rows][cols];

        this.agentList = new HashMap();
        numberGenerator = new Random(this.getSeed());

        int cell;
        PathCell c;
        Map<CellType, List<Cell>> cells = new HashMap();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cell = initialMap[row][col];
                switch (cell) {
                    case CC:
                        // cleaner cell
                        c = new PathCell(row, col);
                        int maxCapacityCleaners = this.getCleanerCapacity();                        
                        WorkerAgent newAgent = new WorkerAgent(AgentType.CLEANER, maxCapacityCleaners);
                        c.addAgent(newAgent);
                        map[row][col] = c;
                        addAgentToList(AgentType.CLEANER, c);
                        break;
                    case SC:
                        // eSearcher cell
                        c = new PathCell(row, col);
                        c.addAgent(new InfoAgent(AgentType.SEARCHER));
                        map[row][col] = c;
                        addAgentToList(AgentType.SEARCHER, c);
                        break;
                    case P:
                        // path
                        map[row][col] = new PathCell(row, col);
                        break;
                    case BCC:
                        // batteries charge cell
                        map[row][col] = new BatteriesChargeCell(row, col);
                        break;    
                    case RPC:
                        // recycling center
                        map[row][col] = new RecyclerPointCell(row, col);
                        break;
                    case F:
                        // Only SystemAgent can access to the SettableFieldCell
                        map[row][col] = new SettableFieldCell(row, col);
                        break;
                    default:
                        throw new Error(getClass().getCanonicalName() + " : Unexpected type of content in the 2D map");
                }
                CellType type = map[row][col].getCellType();
                List<Cell> list;
                if (cells.containsKey(type)) {
                    list = cells.get(type);
                } else {
                    list = new LinkedList();
                    cells.put(type, list);
                }
                list.add(map[row][col]);
            }
        }

        this.setCellsOfType(cells);
        
        int availableCells = getNumberOfCellsOfType(CellType.FIELD);
        if (availableCells < this.getNumberInitialElements()) {
            throw new Error(getClass().getCanonicalName() + " : You set up more new initial elements ("+ this.getNumberInitialElements() +")than existing cells ("+ availableCells +").");
        }
        if (0 > this.getNumberVisibleInitialElements()) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed negative number of visible elements.");
        }
        if (this.getNumberVisibleInitialElements() > this.getNumberInitialElements()) {
            throw new Error(getClass().getCanonicalName() + " : More visible elements than initial elements.");
        }

        int maxInitial = this.getNumberInitialElements();
        int maxVisible = this.getNumberVisibleInitialElements();

        addElements(maxInitial, maxVisible);
    }


    public void addElements(int maxElements, int maxVisible) {
        CellType ctype = CellType.FIELD;
        int maxCells = getNumberOfCellsOfType(ctype);
        int freeCells = this.getNumberOfCellsOfType(ctype, true);

        if (maxElements < 0) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed negative number of elements.");
        }
        if (maxElements > freeCells) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed add more elements than empty cells.");
        }
        if (maxVisible < 0) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed negative number of visible elements.");
        }
        if (maxVisible > maxElements) {
            throw new Error(getClass().getCanonicalName() + " : More visible elements than number of elements.");
        }

        System.out.println(getClass().getCanonicalName() + " : Adding " + maxElements +
                " elements (" + maxVisible + " of them visible) on a map with " +
                maxCells + " cells (" + freeCells + " of them candidate).");

        if (0 == maxElements) {
            return;
        }

        Set<Integer> initialSet = new TreeSet();
        int index;
        while (initialSet.size() < maxElements) {
            index = numberGenerator.nextInt(maxCells);
            if (isEmpty(index)) {
                initialSet.add(index);
            }
        }

        Set<Integer> visibleSet = new TreeSet();
        Object[] initialCells = initialSet.toArray();
        while (visibleSet.size() < maxVisible) {
            visibleSet.add((Integer)initialCells[numberGenerator.nextInt(maxElements)]);
        }

        WasteType[] typesWaste = WasteType.values();
        WasteType type;
        int amount;
        boolean visible;
        for (int i: initialSet) {
            type = typesWaste[numberGenerator.nextInt(typesWaste.length)];
            amount = numberGenerator.nextInt(this.getMaxAmountOfWastes()) + 1;
            visible = visibleSet.contains(i);
            setElements(type, amount, visible, i);
        }
    }

    /**
     * Tells whether the given cell is empty of elements.
     * @param ncell nuber of cell.
     * @return true when empty.
     */
    private boolean isEmpty(int ncell) {
        return ((SettableFieldCell)cellsOfType.get(CellType.FIELD).get(ncell)).isEmpty();
    }

    /**
     * Set up the amount of elements of the given type on the cell specified by
     * ncell. It will be visible whenever stated.
     * @param type type of elements to put in the map.
     * @param amount amount of elements to put into.
     * @param ncell number of cell from a given list.
     * @param visible visible to agents?
     */
    private void setElements(WasteType type, int amount, boolean visible, int ncell) {
        SettableFieldCell cell = (SettableFieldCell)cellsOfType.get(CellType.FIELD).get(ncell);
        cell.setElements(type, amount);
        if (visible) {
            cell.detectWaste();
        }
    }

    /**
     * Process the request of adding new elements onto the map to be run
     * every simulation step.
     *
     * Mainly, it checks the probability of having new elements. If so,
     * it finds the number of cells with new elements, to finally add
     * new elements to the given number of cells.
     *
     * The process takes into account the permitted maximimum number of elements. 
     * Otherwise and error is thrown.
     */
    public void addElementsForThisSimulationStep() {
        int probabilityOfNewElements = this.getNewWasteProbability();
        int stepProbability = numberGenerator.nextInt(100) +1;

        if (stepProbability < probabilityOfNewElements) {
            System.out.println(getClass().getCanonicalName() + " : " + stepProbability +
                    " < " + probabilityOfNewElements +
                    " (step probability for new elements < probability of new elements)");
            return;
        }
        
        int maxCells = this.getMaxAmountOfWastes();
        int numberCells = numberGenerator.nextInt(maxCells) + 1;

        // add elements to the given number of cells for this simulation step.
        // all of them hidden.
        addElements(numberCells, 0);
    }

    /**
     * Ensure agent list is correctly updated.
     *
     * @param type agent type.
     * @param cell cell where appears the agent.
     */
    private void addAgentToList(AgentType type, Cell cell) {
        List<Cell> list = this.agentList.get(type);
        if (list == null) {
            list = new ArrayList();
            this.agentList.put(type, list);
        }
        list.add(cell);
    }
}
