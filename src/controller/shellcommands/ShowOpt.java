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
import java.util.Set;

import output.Output;

import controller.Data;
import lightshell.Command;

public class ShowOpt extends Command {
    protected String getName() { return "optimality"; }
    protected String getUsage() { return "show optimality"; }
    
    protected String getShortHelp() {
        return "show if incumbent basic solution is optimal";
    }
    
    protected String execute(String arg) {
        if (Data.counter == -1) return "show: No LP available.";
        return Output.optimality(Data.getCurrentProgram());
    }
    
    @SuppressWarnings("serial")
    protected Set<String> getAliases() {
        return new HashSet<String>() {
            {
                add("optimal");
                add("opt");
                add("o");
            }
        };
    }
}