package controller;

import java.util.ArrayList;
import java.util.List;

import model.LP;
import output.Output.Format;

public final class Data {
    /* Don't allow instances of this class. */
    private Data() {}
    
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
    public static List<LP> lps = new ArrayList<LP>();
    public static int counter = -1;
}