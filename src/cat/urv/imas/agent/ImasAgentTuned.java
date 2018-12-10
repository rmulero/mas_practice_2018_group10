/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.ontology.GameSettings;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael Mulero
 */
public abstract class ImasAgentTuned extends ImasAgent {
    
    private GameSettings gameSettings;
    
    private final List<AID> previousAgents;
    private final List<AID> nextAgents;
    
    public ImasAgentTuned(AgentType type) {
        super(type);
        this.previousAgents = new ArrayList<>();
        this.nextAgents = new ArrayList<>();
    }
    
    public void setGameSettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public void addPreviousAgent(AID previousAgent) {
        this.previousAgents.add( previousAgent );
    }

    public List<AID> getPreviousAgents() {
        return previousAgents;
    }

    public void addNextAgent(AID nextAgent) {
        this.nextAgents.add( nextAgent );
    }

    public List<AID> getNextAgents() {
        return nextAgents;
    }
    
    ////////////////////////////////////
    
    public void registerDF() {
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType( getType().toString() );
        sd1.setName( getLocalName() );
        sd1.setOwnership( OWNER );

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices( sd1 );
        dfd.setName( getAID() );
        try {
            DFService.register( this, dfd );
            log( "Registered to the DF" );
        } catch (FIPAException e) {
            System.err.println( getLocalName() + " failed registration to DF [ko]. Reason: " + e.getMessage() );
            doDelete();
        }
    }
    
    public void deRegisterDF(){
        try {
            DFService.deregister( this );
            log( "Deregistered from DF" );
        } catch (FIPAException e) {
            System.err.println( getLocalName() + " failed deregistration to DF [ko]. Reason: " + e.getMessage() );
        }
    }
    
    protected AID searchAgent( String agentType ) {
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType( agentType );
        
        AID agentId = UtilsAgents.searchAgent(this, searchCriterion);
        return agentId;
    }
    
    protected AID searchAgent( String agentType, String name ) {
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType( agentType );
        searchCriterion.setName( name );
        
        AID agentId = UtilsAgents.searchAgent(this, searchCriterion);
        return agentId;
    }
    
    public abstract void setupSettings( GameSettings gameSettings );
    
    public void onActionsRequest( ACLMessage request ){
    
    }
    public void onActionsReceived( String actions ){
        
    }
    public void onActionsReceived( ACLMessage request, String actions ){
        
    }
    public void onUpdateRequest( ACLMessage request ) {
        
    }
    public void onUpdateConfirmed(){
        
    }
    public void onUpdateConfirmed( ACLMessage request ){
        
    }
}
