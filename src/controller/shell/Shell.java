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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

import controller.shell.Data.Cmd;

import output.Output.Format;
import parser.LpFileFormatLexer;
import parser.LpFileFormatParser;

import model.LP;

/**
 * An implementation of the shell for pplex.
 * 
 * @author Andreas Halle
 */
public class Shell {
    /* Save history for each linear program */
    private ArrayList<LP> lps = new ArrayList<LP>();

    /* Save command history */
    private ArrayList<String> log = new ArrayList<String>();
    
    /* Index pointer for the current linear program + 1 */
    private int p = 0;
    
    /* Current lp. lps.get(p-1) */
    private LP curLp = null;

    /* Index pointer for undo/redo operations */
    private int redo = 0;
    
    /* Use this output format for numbers */
    private Format format = Format.DECIMAL2;
    
    @SuppressWarnings("serial")
    private final Set<Cmd> REQ_LP = new HashSet<Cmd>() {
        {
            add(Cmd.SHOW);
        }
    };
    
    @SuppressWarnings("serial")
    private final Map<Cmd, LinkedHashSet<Cmd>> SUBCMDS =
                                        new HashMap<Cmd, LinkedHashSet<Cmd>>() {
        {
            put(Cmd.SHOW, new LinkedHashSet<Cmd>() {
                {
                    add(Cmd.SHOWDUAL);
                    add(Cmd.SHOWFEAS);
                    add(Cmd.SHOWOPT);
                    add(Cmd.SHOWPRIMAL);
                    add(Cmd.SHOWSOL);
                }
            });
        }
    };
    
    
    
    private void addLp(LP lp) {
        lps.add(p, lp);
        redo = 0;
        incLpCounter();
    }
    
    
    
    private void decLpCounter() {
        if (p == 0) return;
        if (p-- == 1) curLp = null;
        else curLp = lps.get(p-1);
    }


    
    private void incLpCounter() {
        curLp = lps.get(p++);
    }

    

    /*
     * Parse a message sent to the shell and return an object easy to parse
     * for other functions.
     */
    private ShellMsg parse(String input) throws IllegalArgumentException {
        String msg = input.trim().replaceAll("\\s+", " "); // Remove all extra spaces
        
        ShellMsg smsg = toShellMsg(msg);
        if (smsg == null) {
            String e = (msg.equals("")) ? "" : Data.EHELP;
            throw new IllegalArgumentException(e);
        }
        return smsg;
    }
    
    
    
    /*
     * Return a shellmsg object from a string. Assumes no leading/trailing
     * spaces in input.
     */
    private ShellMsg toShellMsg(String str) {
        int idx1 = str.indexOf(' ');
        int idx2 = str.indexOf(' ', idx1+1); // Will be -1 if idx1 == -1
        
        if (idx1 == -1) { // No subcmd or argument.
            Cmd cmd = Cmd.fromString(str);
            if (cmd == null) return null;
            return new ShellMsg(cmd);
        }
        Cmd cmd = Cmd.fromString(str.substring(0, idx1));
        ShellMsg msg = new ShellMsg(cmd);
        
        if (idx2 == -1) { // subcmd or single argument (no spaces).
            Cmd subCmd = Cmd.fromString(str);
            if (subCmd == null) msg.arg = str.substring(idx1+1);
            else msg.subcmd = msg.subcmd = subCmd;
            return msg;
        }
        
        // subcmd AND argument(s) or several arguments (spaces)
        Cmd subCmd = Cmd.fromString(str.substring(0, idx2));
        if (subCmd != null) {
            msg.subcmd = subCmd;
            msg.arg = str.substring(idx2+1);
            return msg;
        }
        msg.arg = str.substring(idx1+1);
        return msg;
    }
    
    
    
    /*
     * Redirect to correct functions.
     */
    private String parse(ShellMsg msg) {
        if(REQ_LP.contains(msg.cmd) && p == 0)
            return "No current linear program exists.";
        
        switch(msg.cmd) {
        case CONDITIONS: return Data.CONDITIONS;
        case EXIT:
        case Q:
        case QUIT: System.exit(0);
        case READ:       return parseRead(msg);
        case HELP:       return parseHelp(msg);
        case SHOW:       return parseShow(msg);
        case WARRANTY:   return Data.WARRANTY;
        default:         return "";
        }
    }
    
    
    
    private String generateCommandList() {
        Cmd list[] = new Cmd[] {Cmd.HELP, Cmd.READ, Cmd.SHOW, Cmd.QUIT};
        
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for (Cmd c : list) {
            sb.append(String.format("%-20s %s", c, Data.SHELP.get(c)));
            if (++i < list.length) sb.append("\n");
        }
        return sb.toString();
    }
    
    
    private String getUsage(Cmd cmd) {
        return String.format("Usage: %s", Data.SYNTAX.get(cmd));
    }
    
    
    
    private String getLongHelp(Cmd cmd) {
        String lhelp = Data.LHELP.get(cmd);
        if (lhelp != null) return lhelp;
        return Data.SHELP.get(cmd);
    }
    
    
    
    /* 
     * Return a String with a list of sub commands for a given command. Includes
     * leading newlines. Return a blank String if there are no sub commands.
     */
    private String getSubCmdList(Cmd cmd) {
        Set<Cmd> subCmds = SUBCMDS.get(cmd);
        if (subCmds == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nSub Commands:\n");
        int i = 0;
        for (Cmd c : subCmds) {
            sb.append(String.format("%-20s %s", c, Data.SHELP.get(c)));
            if (++i < subCmds.size()) sb.append("\n");
        }
        
        return sb.toString();
    }
    
    
    
    private String getExampleHelp(Cmd cmd) {
        String examples = Data.EXHELP.get(cmd);
        if (examples == null) return "";
        
        return String.format("%nExamples:%n%s", examples);
    }
    
    
    
    private String parseHelp(ShellMsg msg) {
        if (!msg.hasArg()) return generateCommandList();
        
        ShellMsg helpMsg = toShellMsg(msg.arg);
        if (helpMsg == null) 
            return String.format("help: Unknown command '%s'", msg.arg);
        
        Cmd cmd = (helpMsg.isSubCmd()) ? helpMsg.subcmd : helpMsg.cmd;
        
        StringBuffer sb = new StringBuffer();
        sb.append(getUsage(cmd));
        sb.append("\n");
        sb.append(getLongHelp(cmd));
        sb.append(getSubCmdList(cmd));
        sb.append(getExampleHelp(cmd));
        return sb.toString();
    }
    
    
    
    private String parseRead(ShellMsg msg) {
        if (!msg.hasArg()) return getUsage(msg.cmd);
        try {
            CharStream stream = new ANTLRFileStream(msg.arg);
            
            LpFileFormatLexer lexer = new LpFileFormatLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            LpFileFormatParser parser = new LpFileFormatParser(tokenStream);
            
            LP lp = parser.lpfromfile();
            addLp(lp);
            return "Read " + msg.arg + " OK.";
        } catch (Exception e) {
            return "Error reading file: " + e.getLocalizedMessage();
        }
    }
    
    
    
    private String parseShow(ShellMsg msg) {
        if (!msg.isSubCmd()) {
            if (msg.hasArg()) return Data.EHELP;
            return output.Output.primal(curLp, format);
        }
        
        switch (msg.subcmd) {
        case SHOWDUAL:   return output.Output.dual(curLp, format);
        case SHOWFEAS:   return output.Output.feasibility(curLp);
        case SHOWOPT:    return output.Output.optimality(curLp);
        case SHOWPRIMAL: return output.Output.primal(curLp, format);
        default: return Data.EHELP;
        }
    }
        
    
    
    public static void main(String[] args) {
        Shell shell = new Shell();
        shell.run();
    }
    
    
    
    void run() {
        Scanner s = new Scanner(System.in);
        System.out.println(Data.FWELCOME);
        for (;;) {
            System.out.print("pplex> ");
            
            String strcmd = s.nextLine();
            
            ShellMsg cmd = null;
            try {
                cmd = parse(strcmd);
                System.out.println(parse(cmd));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }
    
    
    
    private class ShellMsg {
        private Cmd cmd;
        private Cmd subcmd;
        private String arg;
        
        ShellMsg(Cmd cmd) {
            this.cmd = cmd;
            this.subcmd = null;
            this.arg = null;
        }
        
        boolean hasArg() { return (arg != null); }
        boolean isSubCmd() { return (subcmd != null); }
    }
}
