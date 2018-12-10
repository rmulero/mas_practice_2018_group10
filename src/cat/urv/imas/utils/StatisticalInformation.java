/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rafael Mulero
 */
public class StatisticalInformation {
    
    private final List<List<String>> generatedWastes;
    private final List<String> lastWastes;
    private final List<List<String>> discoveredWastes;
    private final List<List<String>> collectedWastes;
    private final Map<String, Integer> cleanerUnits;
    private final Map<String, Integer> recycledUnits;
    
    public StatisticalInformation(){
        this.generatedWastes = new ArrayList<>();
        this.lastWastes = new ArrayList<>();
        this.discoveredWastes = new ArrayList<>();
        this.collectedWastes = new ArrayList<>();
        this.cleanerUnits = new HashMap<>();
        this.recycledUnits = new HashMap<>();
    }
    
    public void addGeneratedWastes( List<String> wastes ){
        
        if ( generatedWastes.isEmpty() ){
            generatedWastes.add( wastes );
            
        } else {
            List<String> changes = new ArrayList<>();
            
            changes.addAll( wastes );
            changes.removeAll( lastWastes );
            
            generatedWastes.add( changes );
        }
        
        lastWastes.clear();
        lastWastes.addAll( wastes );
    }
    
    public void addDiscoveredWastes( List<String> wastes ){
        discoveredWastes.add( wastes );
    }
    
    public void addCollectedWastes( List<String> wastes ){
        collectedWastes.add( wastes );
    }
    
    public void updateCleanerStats( String agentName, int units ){
        Integer currentUnits = cleanerUnits.get( agentName );
        if ( currentUnits == null ){
            cleanerUnits.put( agentName, units );
        } else {
            cleanerUnits.put( agentName, currentUnits + units );
        }
    }
    
    public void updateRecycledWasteUnits( String typeName, int units ){
        Integer currentUnits = recycledUnits.get( typeName );
        if ( currentUnits == null ){
            recycledUnits.put( typeName, units );
        } else {
            recycledUnits.put( typeName, currentUnits + units );
        }
    }
    
    /**
     * Get the recycled waste units of the given type
     * @param typeName Name of the type of unit
     * @return Number of recycled units for the given type
     */
    public int getRecycledUnitsOfType( String typeName ){
        Integer units = recycledUnits.get( typeName );
        if ( units == null ){
            return 0;
        }
        return units;
    }
    
    /**
     * Get the average of collected units per cleaner
     * @return Average of collected units per cleaner
     */
    public float getAverageCollectedUnits(){
        List<Integer> unitsPerCleaner = new ArrayList();
        for ( String cleaner : cleanerUnits.keySet() ){
            Integer units = cleanerUnits.get( cleaner );
            if (units == null){
                unitsPerCleaner.add( 0 );
            } else {
                unitsPerCleaner.add( units );
            }
        }
        
        float sum = 0;
        for ( int val : unitsPerCleaner ){
            sum += val;
        }
        
        float result = sum / (float)unitsPerCleaner.size();
        return result;
    }
    
    /**
     * Get the average time between waste generation and discovery
     * @return Average steps between waste generation and discovery
     */
    public float getAverageDiscoveryTime(){
        
        List<Integer> steps = new ArrayList<>();
        
        for ( int genStep = 0; genStep < generatedWastes.size(); ++genStep ){
            
            List<String> genWastes = generatedWastes.get(genStep );
            for ( String waste : genWastes ){
                
                boolean found = false;
                for ( int disStep = genStep; disStep < discoveredWastes.size() && !found; ++disStep ){
                    List<String> disWastes = discoveredWastes.get( disStep );
                    if ( disWastes.contains( waste ) ){
                        found = true;
                        steps.add( disStep - genStep );
                    }
                }
            }
        }
        
        float sum = 0;
        for (int val : steps ){
            sum += val;
        }
        
        float result = sum / (float)steps.size();
        return result;
    }
    
    /**
     * Get the average time between waste discovery and collection
     * @return Average steps between waste discovery and collection
     */
    public float getAverageCollectionTime(){
        
        List<Integer> steps = new ArrayList<>();
        
        for ( int disStep = 0; disStep < discoveredWastes.size(); ++disStep ){
            
            List<String> disWastes = discoveredWastes.get( disStep );
            for ( String waste : disWastes ){
                
                boolean found = false;
                for ( int colStep = disStep; colStep < collectedWastes.size() && !found; ++colStep ){
                    List<String> colWastes = collectedWastes.get(colStep );
                    if ( colWastes.contains( waste ) ){
                        found = true;
                        steps.add( colStep - disStep );
                    }
                }
            }
        }
        
        float sum = 0;
        for (int val : steps ){
            sum += val;
        }
        
        float result = sum / (float)steps.size();
        return result;
    }
    
    /**
     * Get the ratio between the number of discovered wastes over the generated ones 
     * @return Ratio between discovered and generated wastes
     */
    public float discoveredWastesRatio(){
        float discovered = 0;
        for ( List<String> wastes : discoveredWastes ){
            discovered += wastes.size();
        }
        
        float total = 0;
        for ( List<String> wastes : generatedWastes ){
            total += wastes.size();
        }
        
        return discovered / total;
    }
    
}
