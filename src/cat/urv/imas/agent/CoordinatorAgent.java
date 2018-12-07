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

import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgentTuned {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    
    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerDF();

        // Get initial game settings
        GameSettings settings = (GameSettings) getArguments()[0];
        setupSettings( settings );
        
        // Add previous agents (System Agent)
        AID systemAgent = searchAgent( AgentType.SYSTEM.toString() );
        addPreviousAgent( systemAgent );
        
        // Add next agents (Cleaner and Searcher Coordinators)
        AID cleanerCoordinator = searchAgent( AgentType.CLEANER_COORDINATOR.toString() );
        AID searcherCoordinator = searchAgent( AgentType.ESEARCHER_COORDINATOR.toString() );
        addNextAgent( cleanerCoordinator );
        addNextAgent( searcherCoordinator );
        
        // Wait first REQUEST
        waitForActions();
    }

    @Override
    protected void takeDown() {
        deRegisterDF();
    }
    
    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    @Override
    public void setupSettings(GameSettings gameSettings) {
        setGame( gameSettings );
    }
    
    /*******  Communications  ********/
    public void waitForActions(){
        MessageTemplate protocolAndPerformative = MessageTemplate.and(
  		MessageTemplate.MatchProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST ),
  		MessageTemplate.MatchPerformative( ACLMessage.REQUEST )
        );
        
        MessageTemplate template = MessageTemplate.and(
                protocolAndPerformative, 
                MessageTemplate.MatchContent( MessageContent.GET_ACTIONS )
        );
        
        addBehaviour(new AchieveREResponder(this, template) {
            
            @Override
            protected ACLMessage prepareResponse(ACLMessage request) {
                log( "REQUEST received from " + request.getSender().getName() + ". Action is ( " + request.getContent() + " )" );
                
                log( "Sending AGREE" );
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }

            @Override
            protected ACLMessage prepareResultNotification( ACLMessage request, ACLMessage response ) {
                ACLMessage reqResponse = requestActions( request );
                waitForUpdate();
                return reqResponse;
            }
        });
    }
    
    public ACLMessage requestActions( ACLMessage request ){
        log( "Actions requested" );
        return onActionsReceived( request );
    }
    
    public ACLMessage onActionsReceived( ACLMessage request ){
        log( "Sending INFORM (actions)" );
        ACLMessage inform = request.createReply();
        inform.setPerformative( ACLMessage.INFORM );
        return inform;
    }
    
    public void waitForUpdate(){
        MessageTemplate protocolAndPerformative = MessageTemplate.and(
  		MessageTemplate.MatchProtocol( FIPANames.InteractionProtocol.FIPA_REQUEST ),
  		MessageTemplate.MatchPerformative( ACLMessage.REQUEST )
        );
        
        MessageTemplate noActions = MessageTemplate.not(
                MessageTemplate.MatchContent( MessageContent.GET_ACTIONS )
        );
        
        MessageTemplate template = MessageTemplate.and(
                protocolAndPerformative, noActions
        );
        
        addBehaviour(new AchieveREResponder(this, template) {
            
            @Override
            protected ACLMessage prepareResponse(ACLMessage request) {
                log( "REQUEST received from " + request.getSender().getName() + ". Action is ( Update agents )" );
                
                log( "Sending AGREE" );
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }

            @Override
            protected ACLMessage prepareResultNotification( ACLMessage request, ACLMessage response ) {
                ACLMessage reqResponse = requestUpdate( request );
                waitForActions();
                return reqResponse;
            }
        });
    }
    
    public ACLMessage requestUpdate( ACLMessage request ){
        log( "Update requested" );
        return onUpdateConfirmed( request );
    }
    
    public ACLMessage onUpdateConfirmed( ACLMessage request ){
        log( "Sending INFORM (updates)" );
        ACLMessage inform = request.createReply();
        inform.setPerformative( ACLMessage.INFORM );
        return inform;
    }
}
