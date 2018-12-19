/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.utils.actions;

/**
 *
 * @author Rafael Mulero
 */
public class ActionBase {
    private final String agent;
    private final String action;
    
    public ActionBase( String agent, String action ){
        this.agent = agent;
        this.action = action;
    }
    
    public String getAgent(){
        return agent;
    }

    public String getAction() {
        return action;
    }
}
