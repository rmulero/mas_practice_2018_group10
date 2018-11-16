package cat.urv.imas.behaviour;

import cat.urv.imas.agent.ImasAgentTuned;
import cat.urv.imas.ontology.GameSettings;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.List;

/**
 * Behaviour for the initial communication process. Includes all the steps 
 * needed for sending and receiving the initial settings and the agreements
 */
public class SetupBehaviour extends Behaviour{

    /* Define the steps */
    public static final int RECEIVE_MAP = 0;
    public static final int SEND_MAP = 1;
    public static final int RECEIVE_ACK = 2;
    public static final int SEND_ACK = 3;
    public static final int DONE = 4;
    
    /* Pointer to the agent */ 
    private final ImasAgentTuned agent;
    
    /* Current step */
    private int step;
    
    /* Number of received ACKs */
    private int totalAcks = 0;
    
    /* Number of received AGREEs */
    private int okAcks = 0;
    
    /**
     * Constructor for the class
     * @param agent Agent invoking the behaviour
     */
    public SetupBehaviour(ImasAgentTuned agent) {
        this.agent = agent;
        this.step = RECEIVE_MAP;
        agent.log("Started behaviour to deal with setup");
    }
    
    /**
     * Constructor for the class
     * @param agent Agent invoking the behaviour
     * @param initialStep Step in which the behaviour starts
     */
    public SetupBehaviour(ImasAgentTuned agent, int initialStep) {
        this.agent = agent;
        this.step = initialStep;
        agent.log("Started behaviour to deal with setup");
    }
    
    @Override
    public void action() {
        switch( step ) {
            case RECEIVE_MAP:
                receiveInitialMapStep();
                break;
            case SEND_MAP:
                sendInitialMapStep();
                break;
            case RECEIVE_ACK:
                receiveAckStep();
                break;
            case SEND_ACK:
                sendAckStep();
                break;
        }
    }

    @Override
    public boolean done() {
        return step == DONE;
    }
    
    ////////////////////////
    
    /**
     * Step in which the agent receives the initial settings or waits until
     * some agent sends them to it
     */
    private void receiveInitialMapStep() {
        
        // Define message template
        MessageTemplate tpl = MessageTemplate.or(
                MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                MessageTemplate.MatchPerformative( ACLMessage.FAILURE ));
        
        // Receive a message
        ACLMessage msg = agent.receive( tpl );
        if ( msg != null ) {
            switch( msg.getPerformative() ){
                // An INFORM is received from previous agent
                case ACLMessage.INFORM: 
                    processInform( msg );
                    break;
                    
                // A FAILURE is received from the previous agent
                case ACLMessage.FAILURE: 
                    processFailure( msg );
                    break;
            }
            
        } else {
            // Wait for the message
            agent.log( "Wait for map" );
            block();
        }
    }
    
    /**
     * Process the received INFORM
     * @param msg Message that has been received
     */
    private void processInform( ACLMessage msg ){
        // Get the sender
        String sender = ((AID) msg.getSender()).getLocalName();
        try {
            // Get the game settings
            GameSettings game = (GameSettings) msg.getContentObject();
            agent.setGameSettings(game);
            agent.log( "Initial map received from " + sender );
            agent.onSettingsReceived(game);

            // Go to "send map" step
            step = SEND_MAP;

        } catch (UnreadableException ex) {
            agent.log( "Unreadable content in map received from " + sender );

            // Send failure to next or previous agent
            if ( !agent.getNextAgents().isEmpty() ) {
                sendFailure( agent.getNextAgents(), RECEIVE_ACK );
            } else if ( !agent.getPreviousAgents().isEmpty() ) {
                sendFailure( agent.getPreviousAgents(), DONE );
            }
        }
    }
    
    /**
     * Process the received FAILURE
     * @param msg Message that has been received
     */
    private void processFailure( ACLMessage msg ) {
        // Get sender
        String sender = ((AID) msg.getSender()).getLocalName();
        agent.log( "Failure received from " + sender );
                        
        // Send failure to next or previous agent
        if ( !agent.getNextAgents().isEmpty() ) {
            sendFailure( agent.getNextAgents(), RECEIVE_ACK );
        } else if ( !agent.getPreviousAgents().isEmpty() ) {
            sendFailure( agent.getPreviousAgents(), DONE );
        }
    }
    
    /**
     * Send a FAILURE message to the given receivers
     * @param receivers Receivers of the message
     * @param nextStep Next step to perform
     */
    private void sendFailure( List<AID> receivers, int nextStep ) {
        // Create FAILURE message
        ACLMessage msg = new ACLMessage( ACLMessage.FAILURE );
        
        // Add receivers
        for ( AID receiver : receivers ) {
            msg.addReceiver( receiver );
        }
        
        // Send message
        agent.send( msg );
        
        // Go to next step
        step = nextStep;
    }
    
    /**
     * Step in which the agent sends the map to the next agents
     */
    private void sendInitialMapStep() {
        
        if ( !agent.getNextAgents().isEmpty() ) {
            sendMap( agent.getNextAgents() );
            
        } else {
            step = SEND_ACK;
        }
    }
    
    /**
     * Send the game settings to the given receivers
     * @param receivers Agents that will receive the settings
     */
    private void sendMap( List<AID> receivers ) {
        
        // Create INFORM message
        ACLMessage msg = new ACLMessage( ACLMessage.INFORM );
        
        // Add receivers
        for (AID next : receivers ) {
            msg.addReceiver( next );
        }

        try {
            // Add the map as the content
            msg.setContentObject( agent.getGameSettings() );
            
            StringBuilder sb = new StringBuilder( "Initial map sent to next agents" );
            sb.append( " (" );
            for ( AID next : receivers ){
                sb.append(" ").append( next.getLocalName() );
            }
            sb.append(" )");
            agent.log( sb.toString() );
            
            // Go to "receive ACK" step
            step = RECEIVE_ACK;

        } catch (IOException ex) {
            agent.log( "Failed to send the map" );

            // Send failure to next agents
            msg.setPerformative( ACLMessage.FAILURE );
        }
        
        // Send the message
        agent.send( msg );
    }
    
    /**
     * Step in which the agent receives one or more ACKs or waits until other 
     * agents send them to it 
     */
    private void receiveAckStep() {
        
        // Define message template
        MessageTemplate tpl = MessageTemplate.or(
                MessageTemplate.MatchPerformative( ACLMessage.AGREE ),
                MessageTemplate.MatchPerformative( ACLMessage.FAILURE ));

        // Receive message
        ACLMessage msg = agent.receive( tpl );
        if ( msg != null ) {
            // Update the number of received ACKs
            ++totalAcks;
            
            // Get the sender
            String sender = ((AID) msg.getSender()).getLocalName();
            
            switch( msg.getPerformative() ){
                case ACLMessage.AGREE:
                    // Update the number of received AGREEs
                    ++okAcks;
                    agent.log( "ACK received from " + sender );
                    break;
                case ACLMessage.FAILURE:
                    agent.log( "Failure received from " + sender );
                    break;
            }
            
            // Check if all expected ACKs have been received
            boolean allAcksReceived = ( totalAcks == agent.getNextAgents().size() );
            boolean allAcksOk = ( okAcks == totalAcks );
            if ( allAcksReceived && allAcksOk ){
                agent.log( "All ACKs received succesfully" );
                step = SEND_ACK;
            } else if ( allAcksReceived && !allAcksOk ){
                agent.log( "Some ACKs had problems" );
                if ( !agent.getPreviousAgents().isEmpty() ) {
                    sendFailure( agent.getPreviousAgents(), DONE );
                } else {
                    step = DONE;
                }
            }
            
        } else {
            // Wait for the message
            block();
        }
    }
    
    /**
     * Step in which the agent sends an ACK to the previous agents
     */
    private void sendAckStep() {
        if ( !agent.getPreviousAgents().isEmpty() ){
            sendAck( agent.getPreviousAgents() );
        }
        
        // Set the behaviour as finished
        step = DONE;
    }
    
    private void sendAck(List<AID> receivers) {
        // Create AGREE message
        ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
        
        // Add receivers
        for (AID prev : agent.getPreviousAgents()) {
            msg.addReceiver(prev);
        }
        
        StringBuilder sb = new StringBuilder( "ACK sent to previous agents" );
        sb.append( " (" );
        for ( AID next : receivers ){
            sb.append(" ").append( next.getLocalName() );
        }
        sb.append(" )");
        agent.log( sb.toString() );
        
        // Send message
        agent.send(msg);
    }
}
