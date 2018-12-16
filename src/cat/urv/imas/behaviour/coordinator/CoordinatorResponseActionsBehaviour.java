/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.ImasAgentTuned;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 *
 * @author Rafael Mulero
 */
public class CoordinatorResponseActionsBehaviour extends AchieveREResponder {
    
    private final ImasAgentTuned imasAgent;
    
    public CoordinatorResponseActionsBehaviour( ImasAgentTuned a, MessageTemplate template){
        super(a, template);
        this.imasAgent = a;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        imasAgent.log( "Actions REQUEST received from " + request.getSender().getName() );

        imasAgent.onActionsRequest( request );

        imasAgent.log( "Sending AGREE (actions)" );
        ACLMessage agree = request.createReply();
        agree.setPerformative(ACLMessage.AGREE);
        return agree;
    }
}
