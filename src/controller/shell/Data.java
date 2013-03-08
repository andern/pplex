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
package controller.shell;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code Data}-class contains data needed by the shell.
 * 
 * @author  Andreas Halle
 * @see     controller.shell.Shell
 */
final class Data {
    static final String PNAME = "pplex";
    static final String VERSION = "0.4.1";
    static final String COPY = "Copyright(C) 2012, 2013 Andreas Halle";
    static final String LINE = String.format("%s version %s, %s", PNAME, VERSION, COPY);
    static final String WELCOME =
        String.format("Welcome to %s. Type 'help' for a list of available commands.", PNAME);
    
    static final String WARRANTY = "This program is distributed in the hope that it will be useful,\n"
                                 + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                                 + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
                                 + "GNU General Public License for more details.";
    static final String CONDITIONS = "This program is free software: you can redistribute it and/or modify\n"
                                   + "it under the terms of the GNU General Public License as published by\n"
                                   + "the Free Software Foundation, either version 3 of the License, or\n"
                                   + "(at your option) any later version.";
    static final String LICENSE = "This program comes with ABSOLUTELY NO WARRANTY; for details type `warranty'.\n"
                                + "This is free software, and you are welcome to redistribute it\n"
                                + "under certain conditions; type `conditions' for details.";

    static final String FWELCOME = String.format("%s\n%s\n\n%s", LINE, LICENSE, WELCOME);

    static final String EHELP = "Unknown command. Type 'help' for a list of available commands.";
    
    /* Some enums representing shell commands */
    static enum Cmd {
        CONDITIONS("conditions"),
        EXIT("exit"),
        HELP("help"),
        Q("q"),
        QUIT("quit"),
        READ("read"),
        SHOW("show"),
        SHOWDUAL("show dual"),
        SHOWFEAS("show feasibility"),
        SHOWOPT("show optimality"),
        SHOWPRIMAL("show primal"),
        SHOWSOL("show solution"),
        WARRANTY("warranty");
        
        private final String text;
        
        Cmd(String text) {
            this.text = text;
        }
        
        public static Cmd fromString(String text) {
            if (text != null) {
                for (Cmd c : Cmd.values()) {
                    if (text.equalsIgnoreCase(c.text)) {
                        return c;
                    }
                }
            }
            return null;
        }
        
        public String toString() {
            return text;
        }
    }



    @SuppressWarnings("serial")
    static final Map<Cmd, String> SYNTAX = new HashMap<Cmd, String>(){
        {
            put(Cmd.CONDITIONS, "conditions");
            put(Cmd.EXIT,       "exit");
            put(Cmd.HELP,       "help (command)");
            put(Cmd.Q,          "q");
            put(Cmd.QUIT,       "quit");
            put(Cmd.READ,       "read <file>");
            put(Cmd.SHOW,       "show (<subcommand>)");
            put(Cmd.SHOWDUAL,   "show dual");
            put(Cmd.SHOWFEAS,   "show feasibility");
            put(Cmd.SHOWOPT,    "show optimality");
            put(Cmd.SHOWPRIMAL, "show primal");
            put(Cmd.SHOWSOL,    "show solution");
            put(Cmd.WARRANTY,   "warranty");
        }
    };


    
    /* 
     * The short help lines should not be longer than 47 characters. This is to
     * make every command fit on one line on a 80 character wide terminal.
     * These sentences should NOT end with a dot.
     */
    @SuppressWarnings("serial")
    static final Map<Cmd, String> SHELP = new HashMap<Cmd, String>() {
        {
            put(Cmd.CONDITIONS, "show license conditions");
            put(Cmd.EXIT,       "quit the program (q and quit do the same)");
            put(Cmd.HELP,       "show this list");
            put(Cmd.Q,          "quit the program (exit and quit do the same)");
            put(Cmd.QUIT,       "quit the program (exit and q do the same)");
            put(Cmd.READ,       "read a .lp file and create a model from it");
            put(Cmd.SHOW,       "show info about the current linear program");
            put(Cmd.SHOWDUAL,   "show the dual dictionary");
            put(Cmd.SHOWFEAS,   "show if incumbent basic solution is feasible");
            put(Cmd.SHOWOPT,    "show if incumbent basic solution is optimal");
            put(Cmd.SHOWPRIMAL, "show the primal dictionary");
            put(Cmd.SHOWSOL,    "show the incumbent basic solution");
            put(Cmd.WARRANTY,   "show license warranty");
        }
    };
    
    /*
     * Long help lines to be shown to the user when help <command> is written.
     * These sentences should be properly punctuated and end with a dot.
     */
    @SuppressWarnings("serial")
    static final Map<Cmd, String> LHELP = new HashMap<Cmd, String>() {
        {
            put(Cmd.CONDITIONS,
                "show a short summary of the terms and conditions of pplex' license."
                );
            
            put(Cmd.HELP,
                "show a list of available commands."
                );
            
            put(Cmd.READ,
                "read a file of format .lp into the program. read does not override\n"
              + "the current progress, so it is possible to undo a read.\n"
              + "\n"
              + "Currently, the .lp file format is only partially supported. That is,\n"
              + "only the objective section and the constraints section is supported."
                );
            
            put(Cmd.SHOW,
                "show has several subcommands that prints out various information\n"
              + "about the current linear program."
                );
            
            put(Cmd.SHOWDUAL,
                "print out a dual dictionary of the current linear program. For the\n"
              + "primal, see the 'show primal' command."
                );
            
            put(Cmd.SHOWPRIMAL,
                "print out a primal dictionary of the current linear program. For the\n"
              + "dual, see the 'show dual' command."
                );
            
            put(Cmd.CONDITIONS,
                "show a short summary of what sort of warranty pplex' license offer."
                );
        }
    };
    
    @SuppressWarnings("serial")
    static final Map<Cmd, String> EXHELP = new HashMap<Cmd, String>() {
        {
        }
    };
}
