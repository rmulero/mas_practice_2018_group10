/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.ontology;

import static cat.urv.imas.ontology.InitialGameSettings.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

/**
 * Helper for updating the game settings. To do so, just update the content of
 * the <code>defineSettings()</code> method.
 */
public class GenerateGameSettings {

    private static final int STEPS = 100;
    
    /**
     * the goal of this class is to generate a file with 
     * all requiered settings of the IMAS practical work
     */
    //private static final String FILENAME = "game.settings";
    static String FILENAME = "game.evaluation.firstdate.settings";

    public static String getFilename() {
          return FILENAME;
    }
    
    public static void setFilename(String fileName) {
        FILENAME = fileName;
    }
    
    /*
     * ********************* JUST SET YOUR SETTINGS ****************************
     */
    /**
     * Override the default settings to what you need.
     *
     * @param settings GameSettings instance.
     */
    public static void defineSettings(InitialGameSettings settings) {
        //add here whatever settings.set* to define your new settings.
        settings.setSeed(1234567890);
        
        settings.setNewWasteProbability(40);
        settings.setMaxAmountOfWastes(3);
        settings.setCleanerCapacity(12);

        settings.setSimulationSteps(STEPS);
        settings.setTitle("Practical IMAS");
        settings.setNumberInitialElements(10);
        settings.setNumberVisibleInitialElements(10);
        // settings for first date
        int[][] map = {
                {F, F,  F,  RPC, F,  F, F,  F,  F,   F,  F,  F,  F, F, F, F,   F, F,  F,  F},
                {F, P,  P,  P,   P,  P, P,  P,  P,   P,  P,  CC, P, P, P, P,   P, P,  P,  F},
                {F, P,  SC, P,   P,  P, P,  P,  SC,  P,  P,  P,  P, P, P, P,   P, P,  CC, F},
                {F, P,  P,  F,   F,  F, F,  F,  F,   P,  P,  F,  F, F, F, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  F, F,  F,  BCC, P,  P,  F,  F, F, F, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  P,  P,   P,  P,  F,  F, P, P, P,   P, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  P,  P,   P,  P,  F,  F, P, P, P,   P, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  SC, F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, BCC, F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  CC, F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  RPC, F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  SC, P,   P,  P, P,  F,  F,   P,  P,  P,  P, P, P, F,   F, P,  P,  F},
                {F, P,  P,  P,   P,  P, P,  F,  F,   P,  P,  P,  P, P, P, F,   F, P,  P,  F},
                {F, F,  F,  F,   F,  F, F,  F,  F,   F,  F,  F,  F, F, F, F,   F, F,  F,  F},
            };
        settings.setInitialMap(map);
    }

    /**
     * Override the default settings to what you need.
     *
     * @param settings GameSettings instance.
     */
    public static void defineSettings_1(InitialGameSettings settings) {
        //add here whatever settings.set* to define your new settings.
        settings.setSeed(1234567890);
        
        settings.setNewWasteProbability(10);
        settings.setMaxAmountOfWastes(3);

        settings.setCleanerCapacity(20);

        settings.setSimulationSteps(STEPS);
        settings.setTitle("Practical IMAS");
        settings.setNumberInitialElements(10);
        settings.setNumberVisibleInitialElements(10);
        // settings for first date
        int[][] map = {
                {F, F,  F,  RPC, F,  F, F,  F,  F,   F,  F,  F,  F, F, F, F,   F, F,  F,  F},
                {F, P,  P,  P,   P,  P, P,  P,  P,   P,  P,  CC, P, P, P, P,   P, P,  P,  F},
                {F, P,  SC, P,   P,  P, P,  CC, P,   P,  P,  P,  P, P, P, P,   P, P,  CC, F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, F,  F,  F},
                {F, P,  P,  F,   F,  P, P,  F,  RPC, P,  P,  F,  F, P, P, F,   F, F,  F,  F},
                {F, SC, P,  P,   P,  P, P,  P,  P,   P,  P,  P,  P, P, P, P,   P, P,  P,  F},
                {F, P,  P,  P,   P,  P, P,  P,  P,   P,  P,  P,  P, P, P, P,   P, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, CC, F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  P,   P,  P, P,  P,  P,   P,  SC, P,  P, P, P, F,   F, P,  P,  F},
                {F, P,  P,  P,   P,  P, P,  P,  P,   P,  P,  P,  P, P, P, RPC, F, P,  P,  F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  CC, F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  CC, F},
                {F, P,  P,  F,   F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  P,  BCC, F,  P, P,  F,  F,   P,  P,  F,  F, P, P, F,   F, P,  P,  F},
                {F, P,  SC, P,   CC, P, P,  P,  P,   P,  P,  P,  P, P, P, P,   P, P,  P,  F},
                {F, P,  P,  P,   P,  P, P,  P,  P,   P,  P,  P,  P, P, P, P,   P, CC, P,  F},
                {F, F,  F,  F,   F,  F, F,  F,  F,   F,  F,  F,  F, F, F, F,   F, F,  F,  F},
            };
        settings.setInitialMap(map);
    }

        /**
     * Override the default settings to what you need.
     *
     * @param settings GameSettings instance.
     */
    public static void defineSettings_2(InitialGameSettings settings) {
        //add here whatever settings.set* to define your new settings.
        settings.setSeed(1234567890);
        
        settings.setNewWasteProbability(20);
        settings.setMaxAmountOfWastes(5);
        settings.setCleanerCapacity(15);

        settings.setSimulationSteps(STEPS);
        settings.setTitle("Practical IMAS");
        settings.setNumberInitialElements(10);
        settings.setNumberVisibleInitialElements(10);
        // settings for first date
        int[][] map = {
                {F,  F,  F,  RPC, F,  F,  F,  F,  F,   F,  F,  F,  F,  F,  F,  F,   F, F,  F,  F},
                {F,  P,  P,  P,   P,  P,  P,  P,  P,   P,  P,  CC, P,  P,  P,  P,   P, P,  P,  F},
                {F,  P,  SC, P,   P,  P,  P,  P,  P,   P,  P,  P,  P,  P,  P,  P,   P, P,  CC, F},
                {F,  P,  P,  F,   F,  F,  F,  F,  F,   F,  F,  F,  F,  P,  P,  F,   F, F,  F,  F},
                {F,  P,  P,  F,   F,  F,  F,  F,  RPC, F,  F,  F,  F,  P,  P,  F,   F, F,  F,  F},
                {F,  P,  SC, P,   P,  P,  P,  P,  P,   P,  P,  P,  P,  P,  P,  P,   P, P,  P,  F},
                {F,  P,  P,  P,   P,  P,  P,  P,  P,   P,  P,  P,  P,  P,  P,  P,   P, P,  P,  F},
                {F,  P,  P,  F,   F,  F,  F,  P,  P,   F,  F,  F,  F,  P,  P,  F,   F, P,  P,  F},
                {F,  P,  P,  F,   F,  F,  F,  P,  P,   F,  F,  F,  F,  P,  P,  F,   F, P,  P,  F},
                {F,  P,  P,  F,   F,  P,  P,  P,  P,   P,  P,  F,  F,  P,  P,  F,   F, P,  P,  F},
                {F,  P,  P,  F,   F,  P,  P,  P,  P,   P,  P,  F,  F,  P,  P,  F,   F, P,  P,  F},
                {F,  P,  P,  F,   F,  P,  P,  F,  F,   P,  SC, F,  F,  P,  P,  F,   F, P,  P,  F},
                {F,  P,  CC, F,   F,  P,  P,  F,  F,   P,  P,  F,  F,  P,  P,  RPC, F, P,  P,  F},
                {F,  P,  P,  F,   F,  P,  P,  F,  F,   P,  P,  F,  F,  P,  P,  F,   F, CC, P,  F},
                {F,  P,  P,  F,   F,  P,  P,  F,  F,   P,  P,  F,  F,  P,  P,  F,   F, P,  P,  F},
                {F,  P,  P,  F,   F,  P,  P,  F,  F,   P,  P,  F,  F,  F,  F,  F,   F, P,  P,  F},
                {F,  P,  P,  BCC, F,  P,  P,  F,  F,   P,  P,  F,  F,  F,  F,  F,   F, P,  P,  F},
                {F,  P,  SC, P,   P,  P,  P,  P,  P,   P,  P,  P,  P,  P,  P,  P,   P, P,  P,  F},
                {F,  P,  P,  P,   P,  P,  P,  P,  P,   P,  P,  P,  P,  P,  P,  P,   P, P,  P,  F},
                {F,  F,  F,  F,   F,  F,  F,  F,  F,   F,  F,  F,  F,  F,  F,  F,   F, F,  F,  F},
            };
        settings.setInitialMap(map);
    }

    
    /*
     * ********************* DO NOT MODIFY BELOW *******************************
     */
   

    /**
     * Produces an XML file with the whole set of settings from the given
     * GameSettings.
     *
     * @param settings GameSettings to store in a file.
     */
    private static void storeSettings(InitialGameSettings settings) {
        try {

            //create JAXBElement of type GameSettings
            //Pass it the GameSettings object
            JAXBElement<InitialGameSettings> jaxbElement = new JAXBElement(
                    new QName(InitialGameSettings.class.getSimpleName()), InitialGameSettings.class, settings);

            //Create a String writer object which will be
            //used to write jaxbElment XML to string
            StringWriter writer = new StringWriter();

            // create JAXBContext which will be used to update writer
            JAXBContext context = JAXBContext.newInstance(InitialGameSettings.class);

            // marshall or convert jaxbElement containing GameSettings to xml format
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            marshaller.marshal(jaxbElement, writer);

            //print XML string representation of GameSettings
            try {
                PrintWriter out = new PrintWriter(getFilename(), "UTF-8");
                out.println(writer.toString());
                out.close();
            } catch (Exception e) {
                System.err.println("Could not create file '" + getFilename() + "'.");
                System.out.println(writer.toString());
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks that settings file was created and it is readable again.
     */
    private static void testSettings() {
        try {
            GameSettings settings = InitialGameSettings.load(getFilename());
            if (settings.getSimulationSteps() != STEPS) {
                throw new Exception("Something went wrong, we loaded some different to what we stored on '" + getFilename() + "'.");
            }
            System.out.println("Settings loaded again from '" + getFilename() + "'. Ok!");
        } catch (Exception e) {
            System.err.println("Settings could not be loaded from '" + getFilename() + "'!");
            e.printStackTrace();
        }
    }
    
     /**
     * Produces a new settings file to be loaded into the game.
     *
     * @param args nothing expected.
     */
    public static final void main(String[] args) {
        InitialGameSettings settings = new InitialGameSettings();
        
        setFilename("game.settings");
        defineSettings(settings);
        //defineSettings_1(settings);
        //defineSettings_2(settings);
        storeSettings(settings);
        testSettings();
        
        setFilename("game.evaluation.firstdate.settings");
        defineSettings_1(settings);
        storeSettings(settings);
        testSettings();
        
        setFilename("game.evaluation.seconddate.settings");
        defineSettings_2(settings);
        storeSettings(settings);
        testSettings();
    }

}
