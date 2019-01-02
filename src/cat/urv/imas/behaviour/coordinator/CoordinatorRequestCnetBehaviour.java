/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.ImasAgentTuned;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Vector;

/**
 *
 * @author Rafael Mulero
 */
public class CoordinatorRequestCnetBehaviour extends ContractNetInitiator {
    
    private final ImasAgentTuned agent;
    
    public CoordinatorRequestCnetBehaviour( ImasAgentTuned a, ACLMessage cfp ) {
        super( a, cfp );
        this.agent = a;
    }

    @Override
    protected void handlePropose( ACLMessage propose, Vector acceptances ) {
        agent.log( "CNet PROPOSE received from " + propose.getSender().getLocalName() + " ( " + propose.getContent() + " )" );
    }

    @Override
    protected void handleRefuse( ACLMessage refuse ) {
        agent.log( "CNet REFUSE received from " + refuse.getSender().getLocalName() );
    }

    @Override
    protected void handleAllResponses( Vector responses, Vector acceptances ) {
        agent.evaluateProposals( responses, acceptances );
    }
    
    @Override
    protected void handleInform( ACLMessage inform ) {
        agent.log( "CNet INFORM received from " + inform.getSender().getLocalName() );
    }

    @Override
    protected void handleFailure( ACLMessage failure ) {
        agent.log( "CNet FAILURE received from " + failure.getSender().getLocalName() );
    }
}
