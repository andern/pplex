/*
 * Copyright (C) 2012, 2013 Andreas Halle
 *
 * This file is part of pplex.
 *
 * pplex is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pplex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public license
 * along with pplex. If not, see <http://www.gnu.org/licenses/>.
 */
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
    public static final String VERSION = "0.5.1";
    public static final String COPY = "Copyright(C) 2012-2014 Andreas Halle";
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
        lps.add(++counter, lp);
        
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
    
    
    
    public static int undo() {
        if (counter < 0) return -1;

        counter--;
        redo++;
        return 0;
    }
    
    
    
    public static int redo() {
        if (redo <= 0) return -1;

        redo--;
        counter++;
        return 0;
    }
}
