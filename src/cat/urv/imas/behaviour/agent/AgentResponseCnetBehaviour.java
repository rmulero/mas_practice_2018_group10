/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.agent;

import cat.urv.imas.agent.ImasAgentTuned;
import cat.urv.imas.utils.ActionUtils;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

/**
 *
 * @author Rafael Mulero
 */
public class AgentResponseCnetBehaviour extends ContractNetResponder {
    
    private final ImasAgentTuned agent;
    
    public AgentResponseCnetBehaviour( ImasAgentTuned a, MessageTemplate mt ) {
        super( a, mt );
        this.agent = a;
    }

    @Override
    protected ACLMessage handleCfp( ACLMessage cfp ) throws RefuseException, FailureException, NotUnderstoodException {
        
        printHandleCfp( cfp );
        
        ACLMessage reply = agent.onCfpReceived( cfp );
        
        return reply;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        agent.log( "CNet REJECT received from " + reject.getSender().getLocalName() );
    }
    
    @Override
    protected ACLMessage handleAcceptProposal( ACLMessage cfp, ACLMessage propose, ACLMessage accept ) throws FailureException {
        
        agent.log( "CNet ACCEPT received from " + accept.getSender().getLocalName() + " ( " + accept.getContent() + " )");
        
        agent.onProposalAccepted( accept );
        
        ACLMessage inform = accept.createReply();
        inform.setPerformative( ACLMessage.INFORM );
        return inform;
    }
    
    private void printHandleCfp( ACLMessage cfp ){
        
        StringBuilder sb = new StringBuilder();
        sb.append( "CNet CFP received from " );
        sb.append( cfp.getSender().getLocalName() );
        sb.append( " ( " );
        
        String[] content = cfp.getContent().split( ActionUtils.DELIMITER_ACTION );
        for ( String action : content ){
            sb.append( "\n\t" ).append( action );
        }
        sb.append( " )" );
        
        agent.log( sb.toString() );
    }
}
