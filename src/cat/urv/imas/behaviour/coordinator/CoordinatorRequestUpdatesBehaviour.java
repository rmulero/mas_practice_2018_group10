/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.ImasAgentTuned;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import java.util.Vector;

/**
 *
 * @author Rafael Mulero
 */
public class CoordinatorRequestUpdatesBehaviour extends AchieveREInitiator {

    private final ImasAgentTuned imasAgent;
    private final ACLMessage request;

    public CoordinatorRequestUpdatesBehaviour(ImasAgentTuned a, ACLMessage msg, ACLMessage request) {
        super(a, msg);
        this.imasAgent = a;
        this.request = request;
    }

    @Override
    protected void handleAgree(ACLMessage agree) {
        imasAgent.log("Update AGREE received from " + ((AID) agree.getSender()).getLocalName());
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        imasAgent.log("Update REFUSE received from " + ((AID) refuse.getSender()).getLocalName());
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        imasAgent.log("Update INFORM received from " + ((AID) inform.getSender()).getLocalName());
        imasAgent.onUpdateConfirmed();
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        imasAgent.log("Update FAILURE received from " + ((AID) failure.getSender()).getLocalName());
    }
    
    @Override
    protected void handleAllResultNotifications( Vector resultNotifications ) {
        imasAgent.onUpdateConfirmed( request );
    }

}
