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

import java.util.Map;
import java.util.Set;

import controller.shell.Command;

public abstract class EasyCommand extends Command {
    protected Set<String> getAliases() { return null; }
    protected Map<String, String> getExamples() { return null; }
    protected Set<Command> getSubCommands() { return null; }
    protected String getLongHelp() { return null; }
}
