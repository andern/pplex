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

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.fraction.BigFraction;

import output.Output;
import parser.LpFileFormatLexer;
import parser.LpFileFormatParser;

import model.LP;

/**
 * An implementation of a command-line
 * interface (CLI) for pplex.
 * 
 * @author  Andreas Halle
 */
class CLI {
    /* Save history for each linear program. */
    private ArrayList<LP> lps = new ArrayList<LP>();

    /* Save command history. */
    private ArrayList<String> log = new ArrayList<String>();

    /* Index pointer for the current linear program. */
    private int p = 0;

    /* Index pointer for undo/redo operations. */
    private int redo = 0;

    /* These commands do operations on an LP. */
    private String[] reqProg = new String[] {Data.pivot,
                                             Data.replace,
                                             Data.reinstate,
                                             Data.show};
    
    /* Standard number of decimals when printing double precision numbers. */
    private int stdPrec = 2;
    
    
    
    /**
     * Add a {@code LP} to the CLI. The LP is set as the current
     * linear program.
     * 
     * @param lp
     *        a linear program.
     */
    protected void addLp(LP lp) {
        lps.add(p++, lp);
        redo = 0;
        // TODO: Fix this in another way?
        VisLP.readScope = true;
        VisLP.feasScope = true;
    }



    /**
     * 
     * @return
     *         The current linear program. Returns null if no current linear
     *         program exists.
     */
    protected LP getCurrentProgram() {
        if (p == 0) return null;
        return lps.get(p-1);
    }
    
    
    
    /* Return whether a command needs a current linear program to function. */
    private boolean requireProgram(String cmd) {
        for (String s : reqProg)
            if (cmd.equals(s))
                return true;
        return false;
    }



    /**
     * Parse a command and return it's output as a {@code String}.
     * 
     * @param  cmd
     *         a command to parse.
     * @return 
     *         the output of the command.
     */
    String parseCmd(String cmd) {
        if (cmd.equals("q") || cmd.equals("exit") || cmd.equals("quit"))
            System.exit(0);

        if (cmd.trim().equals(""))
            return "";

        String[] args = cmd.split(" ");
        String s = "";

        /* Valid command for the current state? */
        if (p == 0 && requireProgram(args[0])) {
            String f = "Cannot execute command '%s' without a current linear "
                    +  "program. See '%s %s'.%n";
            return String.format(f, args[0], Data.help, Data.read);
        }

        if      (args[0].equals(Data.help))      { s = parseHelp(args); }
        else if (args[0].equals(Data.log))       { s = parseLog(args); }
        else if (args[0].equals(Data.pivot))     { s = parsePivot(args); }
        else if (args[0].equals(Data.read))      { s = parseRead(args); }
        else if (args[0].equals(Data.redo))      { s = parseRedo(args); }
        else if (args[0].equals(Data.replace))   { s = parseReplace(args); }
        else if (args[0].equals(Data.show))      { s = parseShow(args); }
        else if (args[0].equals(Data.undo))      { s = parseUndo(args); }
        else if (args[0].equals(Data.reinstate)) { s = parseReinstate(args); }

        else return String.format("Invalid command %s. See '%s'.%n",
                                  args[0], Data.help);

        /* Only log valid commands. */
        log.add(cmd);
        return s;
    }



    private String parseHelp(String[] args) {
        StringBuffer sb = new StringBuffer();

        if (args.length == 1) {
            Set<String> set = Data.SHELP.keySet();
            for (String s : set) {
                sb.append(String.format("%-20s %s%n", s, Data.SHELP.get(s)));
            }
            sb.append("\n");
            sb.append(Data.EHELP);
            sb.append("\n");
        }
        if (args.length == 2) {
            String h = Data.LHELP.get(args[1]);
            if (h != null) sb.append(h);
            else sb.append("No such command.");
        }
        return sb.toString();
    }



    private String parseLog(String[] args) {
        if (log.size() == 0) return "No command history available.";

        StringBuilder sb = new StringBuilder();

        sb.append("Command history:\n");
        String f = "%d: %s%n";

        /* Show the last 10 commands. */
        int k = log.size()-10;
        if (k < 0) k = 0;
        for (; k < log.size(); k++) {
            sb.append(String.format(f, k, log.get(k)));
        }
        return sb.toString();
    }



    private String parsePivot(String[] args) {
        int argc = args.length;

        if (argc == 1) return pivot(false);
        if (argc == 2) {
            boolean dual = args[1].equals(Data.showDual);
            return pivot(dual);
        }
        if (argc == 3 || argc == 4) {
            boolean dual = args[1].equals(Data.showDual);
            try {
                int e = Integer.parseInt(args[argc - 2]) - 1;
                int l = Integer.parseInt(args[argc - 1]) - 1;

                int eSize = lps.get(p-1).getNoNonBasic();
                int lSize = lps.get(p-1).getNoBasic();
                
                if (e < 0 || l < 0) 
                    return "Pivot index must be positive.";
                if (dual && (e >= lSize || l >= eSize)) 
                    return "Invalid dual index";
                if (!dual && (e >= eSize || l >= lSize))
                    return "Invalid primal index";
                
                return pivot(e, l, dual);
            }
            catch (NumberFormatException err) {
                return Data.SYNTAX.get(Data.pivot);
            }
        }
        return Data.SYNTAX.get(Data.pivot);
    }
    
    
    
    private String parseRead(String[] args) {
        if (args.length == 2) {
            CharStream stream;
            try {
            	stream = new ANTLRFileStream(args[1]);

            	LpFileFormatLexer lexer = new LpFileFormatLexer(stream);
            	TokenStream tokenStream = new CommonTokenStream(lexer);
            	LpFileFormatParser parser = new LpFileFormatParser(tokenStream);

            	LP lp = parser.lpfromfile();
            	addLp(lp);
            	return "Read " + args[1] + " OK.\n";
            } catch (Exception e2) {
            	return "Read not OK. " + e2.getLocalizedMessage() + "\n";
            }
        } else {
            return Data.SYNTAX.get(Data.read);
        }
    }



//    private String parseRead(String[] args) {
//        if (args.length == 2) {
//            File file = new File(args[1]);
//
//            try {
//                LP lp = Parser.parse(file);
//                addLp(lp);
//                return "Read " + file + " OK.\n";
//            } catch (Exception e1) {
//            	CharStream stream;
//    			try {
//    				stream = new ANTLRFileStream(args[1]);
//    				
//    	    		LpFileFormatLexer lexer = new LpFileFormatLexer(stream);
//    	    		TokenStream tokenStream = new CommonTokenStream(lexer);
//    	    		LpFileFormatParser parser = new LpFileFormatParser(tokenStream);
//    	    		
//    	    		LP lp = parser.lpfromfile();
//    	    		addLp(lp);
//    	    		return "Read " + args[1] + " OK.\n";
//    			} catch (Exception e2) {
//    				return "Read not OK. " + e2.getLocalizedMessage() + "\n";
//    			}
//            }
//        }
//        else {
//            return Data.SYNTAX.get(Data.read);
//        }
//    }



    private String parseRedo(String[] args) {
        if (redo == 0) return "Nothing to redo.\n";

        redo--;
        p++;
        return "";
    }



    private String parseReplace(String[] args) {
        String syntax = Data.SYNTAX.get(Data.replace);

        LP lp;
        int size = lps.get(p-1).getNoNonBasic();

        if (args.length == 1){
            lp = lps.get(p-1).phaseOneObj();
        }
        else if (args.length == (size + 1)) {
            BigFraction[] coeff = new BigFraction[size];

            for (int i = 0; i < size; i++) {
                try {
                    coeff[i] = new BigFraction(Double.parseDouble(args[i+1]));
                } catch (NumberFormatException err) {
                    return Data.SYNTAX.get(Data.replace);
                }
            }
            lp = lps.get(p-1).replaceObj(coeff);
        }
        else {
            return syntax;
        }
        lps.add(p++, lp);
        return Output.primal(lp, stdPrec);
    }



    private String parseShow(String[] args) {
        if (args.length == 1)                     return parsePrimal(args);

        if (args[1].equals(Data.showDual))        return parseDual(args);
        if (args[1].equals(Data.showFeasibility)) return parseFeasibility(args);
//        if (args[1].equals(Data.showLatex))       return parseLatex(args);
        if (args[1].equals(Data.showOptimality))  return parseOptimality(args);
        if (args[1].equals(Data.showPrimal))      return parsePrimal(args);
        if (args[1].equals(Data.showSolution))    return parseSolution(args);

        return Data.LHELP.get(Data.show);
    }



    private String parseUndo(String[] args) {
        if (p == 0) return "Nothing to undo.";

        p--;
        redo++;
        return "";
    }       



    private String parseReinstate(String[] args) {
        LP lp = lps.get(p-1).reinstate();
        lps.add(p, lp);
        p++;
        return Output.primal(lp, stdPrec);
    }
    
    
    
//    private String parseLatex(String[] args) {
//        int prec = stdPrec;
//        if (args.length == 4) {
//            try {
//                prec = Integer.parseInt(args[3]);
//            } catch (NumberFormatException e) {
//                return Data.SYNTAX.get(Data.showPrimal);
//            }
//        }
//        
//        try {
//            if (args[2].equals("dual")) {
//                return Output.texDual(lps.get(p-1), prec);
//            }
//            return Output.texPrimal(lps.get(p-1), prec);
//        } catch (IllegalArgumentException e) {
//            return e.getLocalizedMessage();
//        } catch (IndexOutOfBoundsException e) {
//            return Data.SYNTAX.get(Data.showLatex);
//        }
//    }



    private String parseSolution(String[] args) {
        LP lp = lps.get(p-1);
        BigFraction[] point = lp.point();
        BigFraction val = lp.objVal();

        int prec = stdPrec;

        if (args.length == 3) {
            try {
                prec = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                return Data.SYNTAX.get(Data.showSolution);
            }
        }

        StringBuilder sb = new StringBuilder("(");

        String f = String.format("%%.%df", prec);
        for (int i = 0; i < point.length-1; i++) {
            sb.append(String.format((f + ", "), point[i].doubleValue()));
        }
        sb.append(String.format(f + ")", point[point.length-1].doubleValue()));

        return String.format(f + " at point %s%n",
                             val.doubleValue(), sb.toString());
    }



    private String parseOptimality(String[] args) {
        LP lp = lps.get(p-1);

        if (lp.feasible(false) && lp.feasible(true))
            return "Incumbent basic solution is optimal\n";
        return "Incumbent basic solution is not optimal\n";
    }



    private String parseFeasibility(String[] args) {
        StringBuilder sb = new StringBuilder();
        LP lp = lps.get(p-1);

        if (lp.feasible(false))
            sb.append("Incumbent basic solution is primal feasible\n");
        else
            sb.append("Incumbent basic solution is primal infeasible.\n");

        if (lp.feasible(true))
            sb.append("Incumbent basic solution is dually feasible.\n");
        else
            sb.append("Incumbent basic solution is dually infeasible.\n");

        return sb.toString();
    }



    private String parsePrimal(String[] args) {
        int prec = stdPrec;
        if (args.length == 3) {
            try {
                prec = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                return Data.SYNTAX.get(Data.showPrimal);
            }
        }
        
        try {
            return Output.primal(lps.get(p-1), Math.max(0,Math.min(15,prec)));
        } catch (IllegalArgumentException e) {
            return e.getLocalizedMessage();
        }
    }



    private String parseDual(String[] args) {
        int prec = stdPrec;
        if (args.length == 3) {
            try {
                prec = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                return Data.SYNTAX.get(Data.showDual);
            }
        }
        
        try {
            return Output.dual(lps.get(p-1), Math.max(0,Math.min(15,prec)));
        } catch (IllegalArgumentException e) {
            return e.getLocalizedMessage();
        }
    } 



    private String pivot(int e, int l, boolean dual) {
        try {
            LP lp;
            if (e == -1 && l == -1) lp = lps.get(p-1).pivot(dual);
            else if (dual) lp = lps.get(p-1).pivot(l, e);
            else lp = lps.get(p-1).pivot(e, l);

            lps.add(p++, lp);
            redo = 0;
            
            if (dual) return Output.dual(lp, stdPrec);
            return Output.primal(lp, stdPrec);
        } catch (MathArithmeticException err) {
            return "Invalid pivot";
        } catch (RuntimeException err) {
            return err.getLocalizedMessage();
        }
    }



    private String pivot(boolean dual) {
        try {
            LP lp = lps.get(p-1).pivot(dual);

            lps.add(p++, lp);
            redo = 0;

            if (dual) return Output.dual(lp, stdPrec);
            return Output.primal(lp, stdPrec);
        } catch (RuntimeException err) {
            return err.getLocalizedMessage();
        }
    }



    void run() {
        Scanner s = new Scanner(System.in);
        System.out.println(Data.FWELCOME);
        for (;;) {
            System.out.print("pplex> ");
            String cmd = s.nextLine();
            System.out.println(parseCmd(cmd));
        }
    }
}
