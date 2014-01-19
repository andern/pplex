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

public class Undo extends Command {
    protected String getName() { return "undo"; }
    protected String getShortHelp() { return "undo last change to lp"; }
    protected String getUsage() { return "undo"; }
    
    protected String execute(String arg) {
        if (arg != null) return "undo: Command does not take any"
                              + "arguments.";
        int err = Data.undo();
        if (err == -1) return "undo: Nothing to undo.";
        return null;
    }
    
    @SuppressWarnings("serial")
    protected Set<String> getAliases() {
        return new HashSet<String>() {
            {
                add("u");
            }
        };
    }
}
