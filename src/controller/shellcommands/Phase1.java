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

public class Phase1 extends Command {
	protected String getName() { return "phase1"; }
    protected String getShortHelp() { return "start phase one of the simplex method"; }
    protected String getUsage() { return "phase1"; }
    
    protected String execute(String arg) {
    	if (arg != null) return "phase1: Command does not take any arguments.";
    	
    	LP lp = Data.getCurrentProgram();
    	
    	if (lp == null)
    		return "phase1: No current linear program loaded.";
    	
    	if (lp.feasible(false))
    		return "phase1: Phase one not started. Incumbent basic solution"
                 + " is primal feasible.";
    	
    	if (lp.feasible(true))
    		return "phase1: Phase one not started. Incumbent basic solution"
                 + " is dually feasible.";
    	
    	Data.addLp(lp.phaseOneObj());
    	return "Phase one has started. See command 'phase2' for how"
    		 + " to continue to phase two.";
    }
    
	@SuppressWarnings("serial")
	protected Set<String> getAliases() {
		return new HashSet<String>() {
            {
            	add("p1");
                add("replace");
            }
        };
	}
}