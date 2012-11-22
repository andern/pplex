/*
 * Copyright (C) 2012 Andreas Halle
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

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * The {@code Data} class contains data needed by the
 * command-line interface.
 * 
 * @author  Andreas Halle
 * @see     controller.CLI
 */
final class Data {
    static final String PNAME = "pplex";
    static final String VERSION = "0.4.1";
    static final String COPY = "Copyright(C) 2012, Andreas Halle";
    static final String LINE = String.format("%s %s, %s", PNAME, VERSION, COPY);
    static final String WELCOME =
        String.format("Welcome to %s. Type 'help' for a list of commands.", PNAME);
    static final String LICENSE = String.format(
            ("%s is free software; you can redistribute and/or\n"
           + "modify it under the terms of the GPL as published by\n"
           + "the Free Software Foundation (version >=3)."), PNAME);

    static final String FWELCOME = String.format("%s\n\n%s\n\n%s", LINE, LICENSE, WELCOME);

    static final String EHELP = "Type 'help <command>' for further\n"
                              + "information about a specific command.";

    static final String help      = "help";
    static final String log       = "log";
    static final String pivot     = "pivot";
    static final String read      = "read";
    static final String redo      = "redo";
    static final String reinstate = "reinstate";
    static final String replace   = "replace";
    static final String show      = "show";
    static final String undo      = "undo";
    static final String quit      = "quit";

    /* Sub commands of show */
    static final String showDual        = "dual";
    static final String showFeasibility = "feasibility";
    static final String showLatex       = "latex";
    static final String showOptimality  = "optimality";
    static final String showPrimal      = "primal";
    static final String showSolution    = "solution";



    @SuppressWarnings("serial")
    static final HashMap<String, String> SYNTAX = new HashMap<String, String>(){
        {
            put(help,      "help");
            put(log,       "log");
            put(pivot,     "pivot ((dual/primal) <entering index> <leaving index>)");
            put(read,      "read <filename>");
            put(redo,      "redo");
            put(reinstate, "reinstate");
            put(replace,   "replace (coeffs..)");
            put(show,      "show <subcommand>");
            put(undo,      "undo");
            put(quit,      "quit or q or exit");

            /* Sub commands of show */
            put(showDual,         "show dual (<precision>)");
            put(showFeasibility,  "show feasibility");
            put(showLatex,        "show latex <dual/primal> (<precision>)");
            put(showOptimality,   "show optimality");
            put(showPrimal,       "show primal (<precision>)");
            put(showSolution,     "show solution (<precision>)");
        }
    };


    
    /* 
     * The short help lines should not be longer than 47 characters. This is to
     * make every command fit on one line on a 80 character wide terminal.
     */
    @SuppressWarnings("serial")
    static final LinkedHashMap<String, String> SHELP = new LinkedHashMap<String, String>() {
        {
            put(help,      "show this list");
            put(log,       "show command history");
            put(pivot,     "run one iteration of the simplex method");
            put(read,      "read a file of format .lp");
            put(redo,      "reverse last undo");
            put(replace,   "replace the current objective function");
            put(show,      "show information about the current dictionary");
            put(undo,      "erase last change");
            put(reinstate, "reinstate the current objective function");
            put(quit,      "quit the program (exit and q do the same)");
        }
    };

    

    @SuppressWarnings("serial")
    static final HashMap<String, String> LHELP = new HashMap<String, String>() {
        {
            put(help,
                    "help shows a list of available commands.\n"
                  + "\n"
                  + "Syntax:\n"
                  + SYNTAX.get(help) + "\n"
                );

            put(log,
                    "log prints out a command history list.\n"
                  + "\n"
                  + "Syntax:\n"
                  + Data.SYNTAX.get(log) + "\n"
                );

            put(pivot,
                    "The pivot command runs one iteration of the simplex method\n"
                  + "on the current linear program and then prints out its\n"
                  + "dictionary.\n"
                  + "\n"
                  + "Syntax:\n"
                  + SYNTAX.get(pivot) + "\n"
                  + "\n"
                  + "A 'dual' argument will tell the program to do one iteration\n"
                  + "of the dual simplex method using the given entering and\n"
                  + "leaving variables. If no 'dual' argument is given, the\n"
                  + "entering and leaving index arguments become the first and\n"
                  + "second argument.\n"
                  + "\n"
                  + "Examples:\n"
                  + "pivot            Run one iteration of the primal simplex\n"
                  + "                 method with entering and leaving variables\n"
                  + "                 calculated using the largest coefficient rule.\n"
                  + "pivot primal     Same as above.\n"
                  + "pivot dual       Same as above with the dual simplex method.\n"
                  + "pivot 2 3        Run one iteration of the primal simplex\n"
                  + "                 method with column 2 being the entering\n"
                  + "                 variable and row 3 being the leaving\n"
                  + "                 variable.\n"
                  + "pivot primal 2 3 Same as above.\n"
                  + "pivot dual 2 3   Same as above with the dual simplex method.\n"
                );

            put(read,
                    "read is used to read a file of format lps into the\n"
                  + "program. read does not override the current progress,\n"
                  + "so it is possible to undo a read.\n"
                  + "\n"
                  + "Syntax:\n"
                  + SYNTAX.get(read) + "\n"
                  + "\n"
                  + "lps format:\n"
                  + "The lps format input syntax is similar to that of\n"
                  + "standard form of linear programs. An lps file should\n"
                  + "have the following format:\n"
                  + "max        c_1  x_1 + c_2  x_2 + ... + c_n  x_n\n"
                  + "subject to c_11 x_1 + c_12 x_2 + ... + c_1n x_n <= b_1\n"
                  + "           c_21 x_1 + c_22 x_2 + ... + c_2n x_n <= b_2\n"
                  + "                         ...                          \n"
                  + "           c_m1 x_1 + c_m2 x_2 + ... + c_mn x_n <= b_m\n"
                  + "where c and b are double precision numbers in decimal form.\n"
                );

            put(redo,
                    "redo reverses the last undo.\n"
                  + "\n"
                  + "Syntax:\n"
                  + Data.SYNTAX.get(redo) + "\n"
                );

            put(reinstate,
                    "reinstate replaces the objective function of the current linear\n"
                  + "program with the original objective function fitting the current\n"
                  + "dictionary.\n"
                  + "\n"
                  + "Syntax:\n"
                  + Data.SYNTAX.get(reinstate) + "\n"
                  + "\n"
                );

            put(replace,
                    "replace replaces the objective function of\n"
                  + "the current linear program.\n"
                  + "\n"
                  + "Syntax:\n"
                  + Data.SYNTAX.get(replace) + "\n"
                  + "\n"
                  + "coeffs represent the coefficients in the primal objective function\n"
                  + "of the current linear program. The number of coeffs (arguments) must\n"
                  + "match the number of decision variables. This command prints out the\n"
                  + "dictionary of the program after the objective has been replaced.\n"
                  + "\n"
                  + "Examples:\n"
                  + "replace          Replace the coefficients of the decision variables\n"
                  + "                 with only -1's ready to run phase one of the\n"
                  + "                 simplex method.\n"
                  + "replace 1 2 -3   Replace the coefficients of the decision variables\n"
                  + "                 with 1, 2 and -3, respectively.\n"
                );

            put(show,
                    "show has several subcommands that prints out various information\n"
                  + "about the current dictionary of the linear program.\n"
                  + "\n"
                  + "Syntax:\n"
                  + Data.SYNTAX.get(show) + "\n"
                  + "\n"
                  + "Subcommands:\n"
                  + "dual             show the dual dictionary\n"
                  + "feasibility      show whether the dictionary is feasible or not\n"
                  + "optimality       show whether the dictionary is optimal or not\n"
                  + "primal           show the primal dictionary\n"
                  + "solution         show the basic (current) solution\n"
                  + "\n"
                  + "To get help for any of the subcommands, type 'help <subcommand>'.\n"
                );

            put(undo,
                    "undo erases the last change done to the current linear program.\n"
                  + "\n"
                  + "Syntax:\n"
                  + Data.SYNTAX.get(undo) + "\n"
                  + "\n"
                );

            put(quit,
                    "quit the program (exit and q do the same)\n"
                  + "\n"
                  + "Syntax:\n"
                  + Data.SYNTAX.get(quit) + "\n"
                  + "\n"
                );


            /* Sub commands of show */
            put(showDual,
                    "dual prints out a dual dictionary of the current\n"
                  + "linear program. For the primal, see the primal command\n"
                  + "\n"
                  + "Syntax:\n"
                  + SYNTAX.get(showDual) + "\n"
                  + "\n"
                  + "precision tells the program how many decimals to use\n"
                  + "for each number in the output. A negative number will\n"
                  + "tell the program to calculate precision automatically.\n"
                  + "Standard precision is two decimals.\n"
                );
            
            put(showFeasibility,
                    "feasibility shows whether the primal or dual dictionary\n"
                  + "of the current linear program is feasible or not.\n"
                  + "\n"
                  + "Syntax:\n"
                  + SYNTAX.get(showFeasibility) + "\n"
                );
            
            put(showLatex,
                    "latex prints out a dictionary of the current linear\n"
                  + "program in LaTeX format.\n"
                  + "\n"
                  + "Syntax:\n"
                  + SYNTAX.get(showLatex) + "\n"
                  + "\n"
                  + "precision tells the program how many decimals to use\n"
                  + "for each number in the output. A negative number will\n"
                  + "tell the program to calculate precision automatically.\n"
                  + "Standard precision is two decimals.\n"
                );
            
            put(showOptimality,
                    "optimality shows whether the dictionary of the current\n"
                  + "linear program is optimal or not.\n"
                  + "\n"
                  + "Syntax:\n"
                  + SYNTAX.get(showOptimality) + "\n"
                );

            put(showPrimal,
                    "primal prints out a primal dictionary of the current\n"
                  + "linear program. For the dual, see the dual command.\n"
                  + "\n"
                  + "Syntax:\n"
                  + SYNTAX.get(showPrimal) + "\n"
                  + "\n"
                  + "precision tells the program how many decimals to use\n"
                  + "for each number in the output. A negative number will\n"
                  + "tell the program to calculate precision automatically.\n"
                  + "Standard precision is two decimals.\n"
                );

            put(showSolution,
                    "solution prints out a solution to the current linear program.\n"
                  + "\n"
                  + "Syntax:\n"
                  + Data.SYNTAX.get(showSolution) + "\n"
                  + "\n"
                  + "precision tells the program how many decimals to use\n"
                  + "for each number in the output. A negative number will\n"
                  + "tell the program to calculate precision automatically.\n"
                  + "Standard precision is two decimals.\n"
                );
        }
    };
}
