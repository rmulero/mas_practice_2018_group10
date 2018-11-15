/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour;

import cat.urv.imas.agent.ImasAgentTuned;
import cat.urv.imas.ontology.GameSettings;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;

/**
 *
 * @author Rafael Mulero
 */
public class SetupBehaviour extends Behaviour{

    public static final int RECEIVE_MAP = 0;
    public static final int SEND_MAP = 1;
    public static final int RECEIVE_ACK = 2;
    public static final int SEND_ACK = 3;
    public static final int DONE = 4;
    
    private final ImasAgentTuned agent;
    
    private int step;
    private int totalAcks = 0;
    private int okAcks = 0;
    
    public SetupBehaviour(ImasAgentTuned agent) {
        this.agent = agent;
        this.step = RECEIVE_MAP;
        agent.log("Started behaviour to deal with setup");
    }
    
    public SetupBehaviour(ImasAgentTuned agent, int initialStep) {
        this.agent = agent;
        this.step = initialStep;
        agent.log("Started behaviour to deal with setup");
    }
    
    @Override
    public void action() {
        switch( step ) {
            case RECEIVE_MAP:
                receiveInitialMap();
                break;
            case SEND_MAP:
                sendInitialMap();
                break;
            case RECEIVE_ACK:
                receiveAck();
                break;
            case SEND_ACK:
                sendAck();
                break;
        }
    }

    @Override
    public boolean done() {
        return step == DONE;
    }
    
    ////////////////////////
    
    private void receiveInitialMap() {
        
        MessageTemplate tpl = MessageTemplate.or(
                MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                MessageTemplate.MatchPerformative( ACLMessage.FAILURE ));
        
        ACLMessage msg = agent.receive( tpl );
        if ( msg != null ) {
            String sender = ((AID) msg.getSender()).getLocalName();
            
            switch( msg.getPerformative() ){
                case ACLMessage.INFORM: // An Inform is received from previous agent
                    try {
                        // Process the message
                        GameSettings game = (GameSettings) msg.getContentObject();
                        agent.setGameSettings( game );
                        agent.log( "Initial map received from " + sender );
                        
                        // Send map to next agents
                        step = SEND_MAP;
                        
                    } catch (UnreadableException ex) {
                        agent.log( "Unreadable content in map received from " + sender );
                        
                        // Send failure to next or previous agent
                        if ( agent.getNextAgents() != null ) {
                            sendFailureNext();
                        } else if ( agent.getPreviousAgents() != null ) {
                            sendFailurePrev();
                        }
                    }
                    break;
                    
                case ACLMessage.FAILURE: // A Failure is received from the previous agent
                    agent.log( "Failure received from " + sender );
                        
                    // Send failure to next or previous agent
                    if ( agent.getNextAgents() != null ) {
                        sendFailureNext();
                    } else if ( agent.getPreviousAgents() != null ) {
                        sendFailurePrev();
                    }
                    
                    break;
            }
        } else {
            agent.log( "Wait for map" );
            block();
        }
    }
    
    private void sendFailureNext() {
        ACLMessage msg = new ACLMessage( ACLMessage.FAILURE );
        for ( AID next : agent.getNextAgents() ) {
            msg.addReceiver( next );
        }
        agent.send( msg );
        
        step = RECEIVE_ACK;
    }
    
    private void sendFailurePrev() {
        ACLMessage msg = new ACLMessage( ACLMessage.FAILURE );
        for ( AID next : agent.getPreviousAgents() ) {
            msg.addReceiver( next );
        }
        agent.send( msg );
        
        step = DONE;
    }
    
    private void sendInitialMap() {
        
        if ( !agent.getNextAgents().isEmpty() ) {
            
            ACLMessage msg = new ACLMessage( ACLMessage.INFORM );
            for ( AID next : agent.getNextAgents() ) {
                msg.addReceiver( next );
            }
            
            try {
                msg.setContentObject( agent.getGameSettings() );
                agent.log( "Initial map sent to next agents" );
                agent.send( msg );
                
                step = RECEIVE_ACK;
                
            } catch (IOException ex) {
                agent.log( "Failed to send the map" );
                
                // Send failure to next agents
                sendFailureNext();
            }

        } else {
            step = SEND_ACK;
        }
    }
    
    private void receiveAck() {
        
        MessageTemplate tpl = MessageTemplate.or(
                MessageTemplate.MatchPerformative( ACLMessage.AGREE ),
                MessageTemplate.MatchPerformative( ACLMessage.FAILURE ));

        ACLMessage msg = agent.receive( tpl );
        if ( msg != null ) {
            ++totalAcks;
            String sender = ((AID) msg.getSender()).getLocalName();
            
            switch( msg.getPerformative() ){
                case ACLMessage.AGREE:
                    ++okAcks;
                    agent.log( "Ack received from " + sender );
                    break;
                case ACLMessage.FAILURE:
                    agent.log( "Failure received from " + sender );
                    break;
            }
            
            boolean allAcksReceived = ( totalAcks == agent.getNextAgents().size() );
            boolean allAcksOk = ( okAcks == totalAcks );
            if ( allAcksReceived && allAcksOk ){
                agent.log( "All acks received succesfully" );
                step = SEND_ACK;
            } else if ( allAcksReceived && !allAcksOk ){
                agent.log( "Some acks had problems" );
                if ( !agent.getPreviousAgents().isEmpty() ) {
                    sendFailurePrev();
                } else {
                    step = DONE;
                }
            }
            
        } else {
            block();
        }
    }
    
    private void sendAck() {
        if ( !agent.getPreviousAgents().isEmpty() ){
            ACLMessage msg = new ACLMessage( ACLMessage.AGREE );
            for ( AID prev : agent.getPreviousAgents() ) {
                msg.addReceiver( prev );
            }
            agent.log( "ACK sent" );
            agent.send( msg );
        }
        
        step = DONE;
    }
}
