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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import output.Output;

import controller.Data;
import lightshell.Command;
import controller.shellcommands.ShowDual;
import controller.shellcommands.ShowFeas;
import controller.shellcommands.ShowOpt;
import controller.shellcommands.ShowPrimal;

public class Show extends Command {
    protected String getName() { return "show"; }
    protected String getUsage() { return "show (<subcommand>)"; }
    
    protected String getLongHelp() { 
        return "show has several subcommands that print out various"
             + " information about the current linear program.";
    }
    protected String getShortHelp() {
        return "show info about the current linear program";
    }
    
    protected String execute(String arg) {
        if (Data.counter == -1) return "show: No LP available.";
        return Output.primal(Data.lps.get(Data.counter), Data.format);
    }
    
    @SuppressWarnings("serial")
    protected Set<String> getAliases() {
        return new HashSet<String>() {
            {
                add("s");
            }
        };
    }
    
    @SuppressWarnings("serial")
    protected Set<Command> getSubCommands() {
        return new LinkedHashSet<Command>() {
            {
                add(new ShowDual());
                add(new ShowFeas());
                add(new ShowOpt());
                add(new ShowPrimal());
            }
        };
    }
}