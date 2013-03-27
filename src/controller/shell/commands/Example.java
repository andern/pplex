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
package controller.shell.commands;

import java.util.Map;
import java.util.Set;

import model.LP;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

import parser.LpFileFormatLexer;
import parser.LpFileFormatParser;

import controller.Data;
import controller.shell.Command;

public class Example extends Command {
    protected Set<String> getAliases() { return null; }
    protected Map<String, String> getExamples() { return null; }
    protected Set<Command> getSubCommands() { return null; }
    
    
    protected String getLongHelp() { 
        return "read a file of format .lp into the program. read does not"
             + " override the current progress, so it is possible to undo"
             + " a read."
             + System.getProperty("line.separator")
             + "Currently, the .lp file format is only partially supported."
             + " That is, only the objective section and the constraints"
             + " section is supported.";
    }
    
    
    protected String getName() { return "read"; }
    protected String getShortHelp() {
        return "read a .lp file and create a model from it";
    }
    
    protected String getUsage() { return "read <file>"; }
    
    protected String execute(String arg) {
        try {
            CharStream stream = new ANTLRFileStream(arg);
            
            LpFileFormatLexer lexer = new LpFileFormatLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            LpFileFormatParser parser = new LpFileFormatParser(tokenStream);
            
            LP lp = parser.lpfromfile();
            Data.lps.add(lp);
            Data.counter++;
            return "Read " + arg + " OK.";
        } catch (Exception e) {
            return "Error reading file: " + e.getLocalizedMessage();
        }
    }
}