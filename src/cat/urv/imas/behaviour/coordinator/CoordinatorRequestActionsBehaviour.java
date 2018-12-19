/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.ImasAgentTuned;
import cat.urv.imas.utils.ActionUtils;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Rafael Mulero
 */
public class CoordinatorRequestActionsBehaviour extends AchieveREInitiator {
    
    private final ImasAgentTuned imasAgent;
    private final ACLMessage request;
    
    public CoordinatorRequestActionsBehaviour(ImasAgentTuned a, ACLMessage msg, ACLMessage request) {
        super(a, msg);
        this.imasAgent = a;
        this.request = request;
    }
    
    @Override
    protected void handleAgree( ACLMessage agree ) {
        imasAgent.log( "Actions AGREE received from " + ((AID) agree.getSender()).getLocalName() );
    }

    @Override
    protected void handleRefuse( ACLMessage refuse ) {
        imasAgent.log( "Actions REFUSE received from " + ((AID) refuse.getSender()).getLocalName() );
    }

    @Override
    protected void handleInform( ACLMessage inform ) {
        imasAgent.log( "Actions INFORM received from " + ((AID) inform.getSender()).getLocalName() );
    }

    @Override
    protected void handleFailure( ACLMessage failure ) {
        imasAgent.log( "Actions FAILURE received from " + ((AID) failure.getSender()).getLocalName() );
    }
    
    @Override
    protected void handleAllResultNotifications( Vector resultNotifications ) {
        List<String> actions = new ArrayList<>();
        for ( Object notification : resultNotifications ){
            ACLMessage response = (ACLMessage) notification;
            String content = response.getContent();
            actions.add( content );
        }
        
        String actionsStr = String.join( ActionUtils.DELIMITER_ACTION, actions );
        imasAgent.onActionsReceived( request, actionsStr );
    }
}
