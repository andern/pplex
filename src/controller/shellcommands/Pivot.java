/*
 * Copyright (C) 2012-2014 Andreas Halle
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

import java.util.*;

import org.apache.commons.math3.exception.MathArithmeticException;

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

                put("pivot dual",
                    "Same as above, using the dual simplex method."
                );

                put("pivot",
                    "Shorthand for 'pivot primal'."
                    );
                
                put("pivot var1 var2",
                    "Run one iteration of the simplex method with "
                  + " 'var1' as the entering variable and 'var2' as the"
                  + " leaving variable."
                    );
                
                put("pivot dual var1 var2",
                    "Same as above, but print the dual dictionary after"
                  + " the iteration."
                    );
                put("pivot var1",
                    "Run one iteration of the primal simplex method with"
                  + " 'var' as the entering variable. The leaving"
                  + " variable is calculated according to the largest"
                  + " coefficient rule."
                    );
            }};
    }
    
    
    protected String getLongHelp() { 
        return "Run one iteration of the simplex method on the current linear"
             + " program and print out its dictionary."
             + System.getProperty("line.separator")
             + "The arguments refer to basic or non-basic variables in the"
             + " primal or dual dictionary. If the dictionary argument is"
             + " given, pplex will choose a pivot based on the largest"
             + " coefficient rule.";
    }
    
    
    protected String getName() { return "pivot"; }
    protected String getShortHelp() {
        return "run one iteration of the simplex method";
    }
    
    protected String getUsage() {
        return "pivot ((dictionary) (<variable> (<variable>)))\n" +
               "DEPRECATED: pivot ((<dictionary>) <colindex> (<rowindex>))";
    }
    
    protected String execute(String arg) {
        if (arg == null) return execute("primal");
        LP lp = Data.getCurrentProgram();
        if (lp == null) return "pivot: No current linear program loaded.";

        String[] args = arg.split(" ");
        
        boolean primal = args[0].equals("primal") || args[0].equals("p");
        boolean dual = args[0].equals("dual") || args[0].equals("d");
        boolean any = primal || dual;
        
        int idx = (any) ? 1 : 0;
        int idx2 = (any) ? 2 : 1;

        /* Get size of lp */
        int ro = lp.getNoBasic();
        int co = lp.getNoNonBasic();

        String colhi = String.format("Column index must be less than %d.", co);
        String dcolhi = String.format("Column index must be less than %d.", ro);
        String collo = "Column index must be greater than 0.";
        String rowhi = String.format("Row index must be less than %d.", ro);
        String drowhi = String.format("Row index must be less than %d.", co);
        String rowlo = "Row index must be greater than 0.";
        
        try {
            if (any && args.length == 1) return pivot(lp, dual);

            /* Find entering variable and check that it's legal. */
            int entering = Integer.parseInt(args[idx]);
            if (entering >= co&&!dual)return String.format("pivot: %s", colhi);
            if (entering >= ro&&dual) return String.format("pivot: %s", dcolhi);
            if (entering < 0) return String.format("pivot: %s", collo);

            if (args.length == idx+1) return pivot(lp, dual, entering);
            
            /* Find leaving variable and check that it's legal. */
            int leaving = Integer.parseInt(args[idx2]);
            if (leaving >= ro&&!dual) return String.format("pivot: %s", rowhi);
            if (leaving >= co&&dual) return String.format("pivot: %s", drowhi);
            if (leaving < 0) return String.format("pivot: %s", rowlo);

            if (args.length == idx2+1) return pivot(lp,dual,entering,leaving);
        } catch (MathArithmeticException e) {
            return "pivot: Illegal pivot. Would cause division by zero.";
        } catch (NumberFormatException e) {
            return parse(lp, args, dual, idx, idx2);
        } catch (RuntimeException e) {
            return String.format("pivot: %s", e.getLocalizedMessage());
        }
        String err = 
                "Unknown parameters. See 'help pivot' for more information.";
        return String.format("pivot: %s", err);
    }



    private String parse(LP lp, String[] args, boolean dual, int idx, int idx2){
        try {
            if (args.length == idx+1)
                return pivot(lp, dual, args[idx]);
            if (args.length == idx2+1)
                return pivot(lp, dual, args[idx], args[idx2]);
        } catch (Exception e) {
            return String.format("pivot: %s", e.getLocalizedMessage());
        }

        String err =
                "Unknown parameters. See 'help pivot' for more information.";
        return String.format("pivot: %s", err);
    }

    private String output(LP curLp, boolean dual) {
        if (dual) return Output.dual(curLp, Data.format);
        return Output.primal(curLp, Data.format);
    }
    
    private String pivot(LP lp, boolean dual) {
        LP curLp = lp.pivot(dual);
        Data.addLp(curLp);
        
        return output(curLp, dual);
    }

    private String pivot(LP lp, boolean dual, int e) {
        LP curLp = lp.pivot(dual, e);
        Data.addLp(curLp);
        
        return output(curLp, dual);
    }

    private String pivot(LP lp, boolean dual, String var) {
        LP curLp = lp.pivot(var);
        Data.addLp(curLp);

        return output(curLp, dual);
    }

    private String pivot(LP lp, boolean dual, String var, String var2) {
        LP curLp = lp.pivot(var, var2);
        Data.addLp(curLp);

        return output(curLp, dual);
    }

    private String pivot(LP lp, boolean dual, int e, int l) {
        LP curLp;
        if (dual) curLp = lp.pivot(l, e);
        else curLp = lp.pivot(e, l);
        Data.addLp(curLp);
        
        return output(curLp, dual);
    }
}
