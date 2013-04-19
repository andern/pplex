package controller;

import java.util.ArrayList;
import java.util.List;

import model.LP;
import output.Output.Format;

public final class Data {
    /* Don't allow instances of this class. */
    private Data() {}
    
    /* Save history for each linear program. */
    private static List<LP> lps = new ArrayList<LP>();
    
    /* Index pointer for the current linear program. */
    public static int counter = -1;
    
    /* Index pointer for undo/redo operations. */
    private static int redo = 0;

    
    public static final String PNAME = "pplex";
    public static final String VERSION = "0.4.1";
    public static final String COPY = "Copyright(C) 2012, 2013 Andreas Halle";
    public static final String LINE = 
            String.format("%s version %s, %s", PNAME, VERSION, COPY);
    public static final String WELCOME =
            String.format("Welcome to %s. Type 'help' for a list of available"
                        + " commands.", PNAME);
    static final String LICENSE =
            "This program comes with ABSOLUTELY NO WARRANTY; for details\n"
          + "type `warranty'. This is free software, and you are welcome\n"
          + "to redistribute it under certain conditions; type `conditions'\n"
          + "for details.";
    public static final String FWELCOME = String.format("%s\n%s\n\n%s",
            LINE, LICENSE, WELCOME);
    
    public static Format format = Format.FRACTION;
    
    /**
     * Add a {@code LP} to the CLI. The LP is set as the current
     * linear program.
     * 
     * @param lp
     *        a linear program.
     */
    public static void addLp(LP lp) {
    	lps.add(counter, lp);
        
        // TODO: Is there a better solution to this?
        VisLP.readScope = true;
        VisLP.feasScope = true;
    }
    
    
    
    /**
     * @return
     *         The current linear program. Returns null if no current linear
     *         program exists.
     */
    public static LP getCurrentProgram() {
        if (counter == -1) return null;
        return lps.get(counter);
    }
    
    
    
    public static String undo() {
        if (counter < 0) return "Nothing to undo.";

        counter--;
        redo++;
        return "";
    }
    
    
    
    public static String redo() {
        if (redo <= 0) return "Nothing to redo.";

        redo--;
        counter++;
        return "";
    }
}