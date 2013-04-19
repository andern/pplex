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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import output.Output;

import model.LP;

import controller.Data;
import lightshell.Command;

public class Pivot extends Command {
    @SuppressWarnings("serial")
    protected Set<String> getAliases() {
        return new HashSet<String>() {
            {
                add("p");
            }
        };
    }
    
    @SuppressWarnings("serial")
    protected Map<String, String> getExamples() {
        return new LinkedHashMap<String, String>() {
            {
                put("pivot primal",
                    "Run one iteration of the primal simplex method with"
                  + " entering and leaving variables according to the"
                  + " largest coefficient rule."
                    );
                
                put("pivot",
                    "Shorthand for 'pivot primal'."
                    );
                
                put("pivot dual",
                    "Same as above, using the dual simplex method."
                    );
                
                put("pivot primal 2 3",
                    "Run one iteration of the primal simplex method with"
                  + " column 2 being the entering variable and row 3 being"
                  + " the leaving variable."
                    );
                
                put("pivot 2 3",
                    "Shorthand for the command directly above."
                    );
                
                put("pivot dual 2 3",
                    "Same as the command directly above, using the dual"
                  + " simplex method."
                    );
            }};
    }
    
    
    protected String getLongHelp() { 
        return "Run one iteration of the simplex method on the current linear"
             + " program and print out its dictionary."
             + System.getProperty("line.separator")
             + "The argument indices refer to rows and columns in the primal or"
             + " dual dictionary. The dictionary argument tells pplex which"
             + " dictionary the indices refer to. If no dictionary argument is"
             + " given, pplex will automatically assume the primal dictionary."
             + " If no rowindex is given, pplex will automatically compute the"
             + " leaving variable according to the largest coefficient rule.";
    }
    
    
    protected String getName() { return "pivot"; }
    protected String getShortHelp() {
        return "run one iteration of the simplex method";
    }
    
    protected String getUsage() {
        return "pivot ((<dictionary>) <colindex> (<rowindex>))";
    }
    
    protected String execute(String arg) {
        if (arg == null) return pivot(false);
        String[] args = arg.split(" ");
        String err="Unknown parameters. See 'help pivot' for more information.";
        
        boolean primal = args[0].equals("primal") || args[0].equals("p");
        boolean dual = args[0].equals("dual") || args[0].equals("d");
        boolean any = primal || dual;
        
        int idx = (any) ? 1 : 0;
        int idx2 = (any) ? 2 : 1;
        
        try {
            if (any && args.length == 1) return pivot(dual);
            
            int entering = Integer.parseInt(args[idx]);
            if (args.length == idx+1) return pivot(dual, entering);
            
            int leaving = Integer.parseInt(args[idx2]);
            if (args.length == idx2+1) return pivot(dual, entering, leaving);
        } catch (NumberFormatException e) {
        } catch (RuntimeException e) {
            System.out.println("runtimeexception");
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
        return err;
    }
    
    private String output(LP curLp, boolean dual) {
        if (dual) return Output.dual(curLp, Data.format);
        return Output.primal(curLp, Data.format);
    }
    
    private String pivot(boolean dual) {
        LP curLp = Data.lps.get(Data.counter++).pivot(dual);
        Data.lps.add(curLp);
        
        return output(curLp, dual);
    }

    private String pivot(boolean dual, int e) {
        LP curLp = Data.lps.get(Data.counter++).pivot(dual, e);
        Data.lps.add(curLp);
        
        return output(curLp, dual);
    }

    private String pivot(boolean dual, int e, int l) {
        LP curLp = Data.lps.get(Data.counter++);
        if (dual) curLp = curLp.pivot(l, e);
        else curLp = curLp.pivot(e, l);
        Data.lps.add(curLp);
        
        return output(curLp, dual);
    }
}