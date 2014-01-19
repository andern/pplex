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

import controller.Data;

import lightshell.Command;
import model.LP;

public class Phase2 extends Command {
    protected String getName() { return "phase2"; }
    protected String getShortHelp() { return "start phase two of the simplex method"; }
    protected String getUsage() { return "phase2"; }
    
    protected String execute(String arg) {
        if (arg != null) return "phase2: Command does not take any arguments.";
        
        LP lp = Data.getCurrentProgram();
        
        if (lp == null)
            return "phase2: No current linear program loaded.";
        
        if (!lp.feasible(false))
            return "phase2: Phase two not started. Incumbent basic solution"
                 + " is primal infeasible.";
        
        if (!lp.feasible(true))
            return "phase2: Phase two not started. Incumbent basic solution"
                 + " is dually infeasible.";
        
        Data.addLp(lp.reinstate());
        return "phase2: Phase two has started.";
    }
    
    @SuppressWarnings("serial")
    protected Set<String> getAliases() {
        return new HashSet<String>() {
            {
                add("p2");
                add("reinstate");
            }
        };
    }
}
