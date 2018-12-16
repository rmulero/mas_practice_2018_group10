/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.utils;

/**
 *
 * @author Rafael Mulero
 */
public class AStarNode implements Comparable<AStarNode>{
    
    private final int row;
    private final int col;
    private AStarNode parent;
    
    private int gValue;
    private int hValue;
    
    public AStarNode( int row, int col ){
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setParent(AStarNode parent) {
        this.parent = parent;
    }
    
    public AStarNode getParent(){
        return parent;
    }
    
    public void setgValue(int gValue) {
        this.gValue = gValue;
    }

    public int getgValue() {
        return gValue;
    }
    
    public void sethValue(int hValue) {
        this.hValue = hValue;
    }
    
    public int gethValue() {
        return hValue;
    }
    
    public int getfValue() {
        return getgValue() + gethValue();
    }

    @Override
    public int compareTo(AStarNode o) {
        int fDifference = this.getfValue() - o.getfValue();
        return fDifference;
    }

    @Override
    public boolean equals(Object obj) {
        AStarNode o = (AStarNode) obj;
        
        boolean sameRow = this.getRow() == o.getRow();
        boolean sameCol = this.getCol() == o.getCol();
        
        return sameRow && sameCol;
    }
}
