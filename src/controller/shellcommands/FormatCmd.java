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
package controller.shellcommands;

import output.Output.Format;
import controller.Data;
import lightshell.Command;
import lightshell.Shell;

public class FormatCmd extends Command {
    protected String getLongHelp() { 
        return "Specify an output format for numbers throughout pplex. This"
             + " only affects the display of numbers, and not how they are"
             + " stored or how calculations are done."
             + System.getProperty("line.separator")
             + "Not specifying any format will output the format currently in"
             + " use."
             + System.getProperty("line.separator")
             + prettyFormat();
    }
    
    
    protected String getName() { return "format"; }
    protected String getShortHelp() {
        return "choose between a number of output formats";
    }
    
    protected String getUsage() { return "format (<format>)"; }
    
    protected String execute(String arg) {
        if (arg == null || arg.length() == 0) 
            return String.format("Current format: %s.", arg);
        
        try  {
            Format f = Format.valueOf(arg);
            Data.format = f;
            return String.format("Changed format to %s.", f);
        } catch (Exception e) {
            return String.format("Invalid format '%s'.", arg);
        }
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
            sb.append(Shell.indent(str, f.getDesc(), 20, 47));
            delim = endl;
        }
        return sb.toString();
    }
}