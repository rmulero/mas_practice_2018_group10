/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.agent;

import cat.urv.imas.agent.ImasAgentTuned;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 *
 * @author Rafael Mulero
 */
public class AgentResponseUpdatesBehaviour extends AchieveREResponder {
    
    private final ImasAgentTuned imasAgent;
    
    public AgentResponseUpdatesBehaviour( ImasAgentTuned a, MessageTemplate template){
        super(a, template);
        this.imasAgent = a;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        imasAgent.log( "Update REQUEST received from " + request.getSender().getName() );

        imasAgent.log( "Sending AGREE (updates)" );
        ACLMessage agree = request.createReply();
        agree.setPerformative(ACLMessage.AGREE);
        return agree;
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        imasAgent.onUpdateRequest( request );
        
        imasAgent.log( "Sending INFORM (updates)" );
        ACLMessage inform = request.createReply();
        inform.setPerformative( ACLMessage.INFORM );
        return inform;
    }
}
