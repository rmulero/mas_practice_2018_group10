/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.system.SystemRequestActionsBehaviour;
import cat.urv.imas.behaviour.system.SystemRequestUpdatesBehaviour;
import cat.urv.imas.ontology.InitialGameSettings;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.MessageContent;
import jade.core.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.List;


/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgentTuned {

    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private InitialGameSettings game;

    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super( AgentType.SYSTEM );
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the
     * stantard output.
     *
     * @param log String to show
     */
    @Override
    public void log( String log ) {
        if ( gui != null ) {
            gui.log( getLocalName()+ ": " + log + "\n" );
        }
        super.log(log);
    }

    /**
     * An error message is shown in the log area of the GUI, as well as in the
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog( String error) {
        if (gui != null) {
            gui.log( "ERROR: " + getLocalName()+ ": " + error + "\n" );
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Adds (if probability matches) new elements onto the map
     * for every simulation step.
     * This method is expected to be run from the corresponding Behaviour
     * to add new elements onto the map at each simulation step.
     */
    public void addElementsForThisSimulationStep() {
        this.game.addElementsForThisSimulationStep();
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);

        // 1. Register the agent to the DF
        registerDF();

        // 2. Load game settings.
        InitialGameSettings settings = InitialGameSettings.load( "game.settings" );
        this.game = settings;
        
        log( "Initial configuration settings loaded" );
        settings.addElementsForThisSimulationStep();
        setupSettings( settings );
       
        // 3. Load GUI
        try {
            this.gui = new GraphicInterface( settings );
            gui.setVisible(true);
            log( "GUI loaded" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        // Create the agents dynamically
        Object[] arguments = { settings };
        UtilsAgents.createAgent( this.getContainerController(), "ca", CoordinatorAgent.class.getName(), arguments);
        UtilsAgents.createAgent( this.getContainerController(), "cc", CleanerCoordinator.class.getName(), arguments);
        UtilsAgents.createAgent( this.getContainerController(), "sc", EsearcherCoordinator.class.getName(), arguments);
        
        List<Cell> cleaners = settings.getAgentList().get( AgentType.CLEANER );
        for( int i = 0; i < cleaners.size(); ++i ){
            String name = "clag" + i;
            UtilsAgents.createAgent( this.getContainerController(), name, CleanerAgent.class.getName(), arguments );
        }
        
        List<Cell> searchers = settings.getAgentList().get( AgentType.SEARCHER );
        for( int i = 0; i < searchers.size(); ++i ){
            String name = "seag" + i;
            UtilsAgents.createAgent( this.getContainerController(), name, SearcherAgent.class.getName(), arguments );
        }
        
        // Add next agents (Coordinator Agent)
        AID coordinatorAgent = searchAgent( AgentType.COORDINATOR.toString() );
        addNextAgent( coordinatorAgent );
        
        // Start simulation
        requestActions();
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
        gui.dispose();
    }
    
    public void updateGUI() {
        this.gui.updateGame();
    }

    @Override
    public void setupSettings(GameSettings gameSettings) {
        log( gameSettings.toString() );
        maxSteps = gameSettings.getSimulationSteps();
    }
    
    private int maxSteps = 0;
    private int currentStep = 1;
    
    /*******  Communications  ********/
    private void requestActions(){
        
        log( "Requesting actions for step " + currentStep );
        
        ACLMessage message = new ACLMessage( ACLMessage.REQUEST );
        message.setProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST );
        message.setContent( MessageContent.GET_ACTIONS );
        
        for( AID nextAgent : getNextAgents() ){
            message.addReceiver( nextAgent );
        }
        
        // Add behaviour to handle REQUEST responses
        addBehaviour(new SystemRequestActionsBehaviour(this, message) );
    }
    
    @Override
    public void onActionsReceived( String actions ){
        log( "Actions received" );
        
        String updates = checkActions( actions );
        requestUpdate( updates );
    }
    
    private String checkActions( String actions ){
        return actions;
    }
    
    private void requestUpdate( String updates ){
        log("Requesting updates for step " + currentStep);
        
        ACLMessage message = new ACLMessage( ACLMessage.REQUEST );
        message.setProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST );
        message.setContent( updates );
        
        for( AID nextAgent : getNextAgents() ){
            message.addReceiver( nextAgent );
        }
        
        // Add behaviour to handle REQUEST responses
        addBehaviour(new SystemRequestUpdatesBehaviour(this, message) );
    }
    
    @Override
    public void onUpdateConfirmed(){
        log( "Updates confirmed" );
        
        currentStep++;
        
        if ( currentStep <= maxSteps ){
            addElementsForThisSimulationStep();
            updateGUI();
            
            requestActions();
        } else {
            log( "SIMULATION FINISHED" );
        }
    }
}
