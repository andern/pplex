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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

import controller.VisLP;
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
    private Format format = Format.FRACTION;
    
    /* All commands that require a linear program to function. */
    @SuppressWarnings("serial")
    private final Set<Cmd> REQ_LP = new HashSet<Cmd>() {
        {
            add(Cmd.PIVOT);
            add(Cmd.SHOW);
        }
    };
    
    /* All sub commands of all commands that have sub commands. */
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
                }
            });
        }
    };
    
    
    
    /**
     * Add an {@code LP} object at the end of the list of LPs. Also update the
     * LP counter such that curLp points to the new LP.
     * 
     * @param lp
     */
    public void addLp(LP lp) {
        lps.add(p, lp);
        redo = 0;
        incLpCounter();
        
        // TODO: Solve this in another way?
        VisLP.readScope = true;
        VisLP.feasScope = true;
    }
    
    
    
    /**
     * 
     * @return
     *         The current linear program. Returns null if no current linear
     *         program exists.
     */
    public LP getCurrentProgram() {
        if (p == 0) return null;
        return lps.get(p-1);
    }



    /**
     * Parse a string (should come from console or gui-console), do what the
     * input says the user wants to do and output some result.
     * 
     * @param  str
     *         {@code String} to parse. Hopefully contains some command.
     * @return
     *         some (hopefully) informative {@code String}.
     */
    public String parse(String str) {
        try {
            return parse(parseStr(str));
        } catch (IllegalArgumentException e) {
            return e.getLocalizedMessage();
        }
    }



    public void run() {
        Scanner s = new Scanner(System.in);
        System.out.println(Data.FWELCOME);
        for (;;) {
            System.out.print("pplex> ");
            
            String strcmd = s.nextLine();
            
            ShellMsg cmd = null;
            try {
                cmd = parseStr(strcmd);
                System.out.println(parse(cmd));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }



    /*
     * Cut a string into several strings. Each of the new strings will never
     * be longer than maxlen. The output will look nice even if the original
     * string contains newlines.
     */
    private String[] cut(String s, int maxlen) {
        String endl = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        
        String delim = "";
        String[] lines = s.split(endl);
        for (String line : lines) {
            sb.append(delim).append(cutLine(line, maxlen));
            delim = endl;
        }
        
        return sb.toString().split(endl);
    }



    /*
     * Cut a string into several lines that are never longer than maxlen. Each
     * line is separated with the OS's line separator. 
     */
    private String cutLine(String s, int maxlen) {
        String endl = System.getProperty("line.separator");
        int len = 0;
        
        StringBuilder sb = new StringBuilder();
        String delim = "";
        String[] words = s.split(" ");
        for (String word : words) {
            int wlen = word.length() + delim.length();
            len += wlen;
            
            if (len > maxlen) {
                len = wlen;
                sb.append(endl);
                delim = "";
            }
            
            sb.append(delim).append(word);
            delim = " ";
        }
        
        return sb.toString();
    }



    private void decLpCounter() {
        if (p == 0) return;
        if (p-- == 1) curLp = null;
        else curLp = lps.get(p-1);
    }


    
    /* Output a list of available commands with its short help. */
    private String generateCommandList() {
        Cmd list[] = new Cmd[] {Cmd.FORMAT, Cmd.HELP, Cmd.PIVOT, Cmd.READ,
                                Cmd.SHOW, Cmd.QUIT};
        
        StringBuffer sb = new StringBuffer();
        String delim = "";
        for (Cmd c : list) {
            sb.append(delim);
            sb.append(indentTitleText(c.toString(), Data.SHELP.get(c), 20, 47));
            delim = System.getProperty("line.separator");
        }
        return sb.toString();
    }



    /*
     * Return a String with a list of examples for a given command. Includes
     * leading newline. Return a blank String if there are no examples.
     */
    private String getExampleHelp(Cmd cmd) {
        LinkedHashMap<String, String> ex = Data.EXHELP.get(cmd);
        if (ex == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("\nEXAMPLE USAGE\n");
        Set<String> keys= ex.keySet();
        String delim = "";
        for (String key : keys) {
            String str = ex.get(key);
            sb.append(delim).append(indentTitleText(" " + key, str, 20, 47));
            delim = System.getProperty("line.separator");
        }
        return sb.toString();
    }



    /* 
     * Return a string containing a long text with help for the specified
     * command. No lines in the output is longer than the gived limit.
     */
    private String getLongHelp(Cmd cmd, int lim) {
        String lhelp = Data.LHELP.get(cmd);
        if (lhelp == null) return Data.SHELP.get(cmd);
        
        StringBuilder sb = new StringBuilder();
        
        String delim = "";
        String[] lines = cut(lhelp, lim);
        for (String s : lines) {
            sb.append(delim).append(" ").append(s);
            delim = System.getProperty("line.separator");
        }
        return sb.toString();
    }



    /* 
     * Return a String with a list of sub commands for a given command. Includes
     * leading newline. Return a blank String if there are no sub commands.
     */
    private String getSubCmdList(Cmd cmd) {
        Set<Cmd> subCmds = SUBCMDS.get(cmd);
        if (subCmds == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("\nSUB COMMANDS\n");
        
        String delim = "";
        for (Cmd c : subCmds) {
            String str = " " + c.toString();
            sb.append(delim);
            sb.append(indentTitleText(str, Data.SHELP.get(c), 20, 47));
            delim = System.getProperty("line.separator");
        }
        
        return sb.toString();
    }



    private String getUsage(Cmd cmd) {
        return String.format("Usage: %s", Data.SYNTAX.get(cmd));
    }



    private void incLpCounter() {
        curLp = lps.get(p++);
    }

    

    /*
     * Indent a help string like this:
     * 
     * cmd subcmd arg    This is a long text describing what
     *                   the command does. This line is split
     *                   into several lines and indented
     *                   "properly" in such a way that makes
     *                   it readable.
     */
    private String indentTitleText(String title, String[] lines, int indentLen) {
        StringBuilder sb = new StringBuilder();
        String format = String.format("%%-%ds %%s", indentLen);
        
        String s = title;
        String delim = "";
        for (String line : lines) {
            sb.append(delim);
            sb.append(String.format(format, s, line));
            s = "";
            delim = System.getProperty("line.separator");
        }
        return sb.toString();
    }



    private String indentTitleText(String title, String longStr, int indentLen,
            int maxlen) {
        return indentTitleText(title, cut(longStr, maxlen), indentLen);
    }



    /* Redirect to correct function. */
    private String parse(ShellMsg msg) {
        if(REQ_LP.contains(msg.cmd) && p == 0)
            return "No current linear program exists.";
        
        switch(msg.cmd) {
        case CONDITIONS: return Data.CONDITIONS;
        case EXIT:
        case Q:
        case QUIT:       System.exit(0);
        case FORMAT:     return parseFormat(msg);
        case HELP:       return parseHelp(msg);
        case READ:       return parseRead(msg);
        case SHOW:       return parseShow(msg);
        case WARRANTY:   return Data.WARRANTY;
        default:         return "";
        }
    }



    /* Set or show the current format. */
    private String parseFormat(ShellMsg msg) {
        if (!msg.hasArg()) return String.format("Current format: %s.", format);
        
        try  {
            Format f = Format.valueOf(msg.arg);
            format = f;
            return String.format("Changed format to %s.", f);
        } catch (Exception e) {
            return String.format("Invalid format '%s'.", msg.arg);
        }
    }



    /* 
     * Return a string with all the help available for a command. Sections
     * without information about the specified command is omitted.
     */
    private String parseHelp(ShellMsg msg) {
        if (!msg.hasArg()) return generateCommandList();
        
        
        ShellMsg helpMsg = toShellMsg(msg.arg);
        if (helpMsg == null) 
            return String.format("help: Unknown command '%s'", msg.arg);
        
        /* Some exceptional commands */
        if (helpMsg.cmd == Cmd.FORMAT) return prettyFormat();
        
        Cmd cmd = (helpMsg.isSubCmd()) ? helpMsg.subcmd : helpMsg.cmd;
        
        StringBuffer sb = new StringBuffer();
        sb.append(getUsage(cmd));
        sb.append("\n");
        sb.append(getLongHelp(cmd, 67));
        sb.append(getSubCmdList(cmd));
        sb.append(getExampleHelp(cmd));
        return sb.toString();
    }



    /*
     * Parse a file and turn it into an LP object. Set the new LP as the current
     * LP.
     * 
     * NOTE: Please ignore any errors complaining about that the following
     * classes are missing:
     *  CharStream
     *  ANTLRFileStream
     *  LpFileFormatLexer
     *  TokenStream
     *  CommonTokenStream
     *  LpFileFormatParser
     *  
     *  These classes are built at compile time (see ant target 'antlr' in
     *  build.xml).
     */
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



    /* Run the correct sub commands of show */
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



    /*
     * Parse a message sent to the shell and return an object easy to parse
     * for other functions.
     */
    private ShellMsg parseStr(String input) throws IllegalArgumentException {
        String msg = input.trim().replaceAll("\\s+", " ");// Remove extra spaces
        
        ShellMsg smsg = toShellMsg(msg);
        if (smsg == null) {
            String e = (msg.equals("")) ? "" : Data.EHELP;
            throw new IllegalArgumentException(e);
        }
        return smsg;
    }
    
    
    
    /* Print out a pretty list of available output formats. */
    private String prettyFormat() {
        String endl = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder("AVAILABLE FORMATS");
        sb.append(endl);
        String delim = "";
        for (Format f : Format.values()) {
            String str = " " + f.toString();
            sb.append(delim);
            sb.append(indentTitleText(str, f.getDesc(), 20, 47));
            delim = endl;
        }
        return sb.toString();
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
