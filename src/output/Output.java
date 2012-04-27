/*
 * Copyright (C) 2012 Andreas Halle
 *
 * This file is part of lpped.
 *
 * lpped is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * lpped is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public license
 * along with lpped. If not, see <http://www.gnu.org/licenses/>.
 */
package output;

import model.LP;
import model.Matrix;

/**
 * The {@code Output} class contains methods that
 * return {@code Strings] that are ready to be
 * output by the command-line interface.
 *  
 * @author  Andreas Halle
 * @version 0.1
 * @see     controller.CLI
 */
public final class Output {
    /**
     * Return a nicely formatted {@code String} that represents the
     * matrix-vector product of the given {@code Matrix} and the
     * given vector given as an {@code Array} with a given precision.
     *
     * @param  A
     *         A {@code Matrix}.
     * @param  x
     *         A vector as an {@code array} of {@code Strings}.
     * @param  precision
     *         Limit each double precision number to this many decimals.
     *         Give a negative value to automatically set precision.
     * @return
     *         A nicely formatted {@code String}.
     */
    public static String toString(Matrix A, String[] x, int precision) {
        StringBuilder sb = new StringBuilder();
        String[][] terms = OMatrix.niceTerms(A, x, precision);

        for (int i = 0; i < A.rows(); i++) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(OMatrix.exprToString(terms[i], x, precision));
        }
        return sb.toString();
    }



    /**
     * Return a nicely formatted {$code String} that represents the
     * matrix-vector product of the given {@code Matrix} and the
     * given vector given as an {@code Array} with 2 decimal
     * precision.
     *
     * @param  A
     *         A {@code Matrix}.
     * @param  x
     *         A vector as an {@code array} of {@code Strings}.
     * @return
     *         A nicely formatted {@code String}.
     */
    public static String toString(Matrix A, String[] x) {
        return toString(A, x, 2);
    }



    /**
     * Return a nicely formatted primal dictionary as a {@code String}.
     *
     * @param  lp
     *         A {@code LP}.
     * @param  precision
     *         Limit each double precision number to this many decimals.
     *         Give a negative value to automatically set precision.
     * @return
     *         A nicely formated {@code String}.
     *         
     */
    public static String primal(LP lp, int precision) {
        Matrix dict = lp.dictionary();

        String[] basic = OLP.insert(lp.getBasic(), "ζ");
        String[] nb = OLP.insert(lp.getNonBasic(), "");

        int max = OLP.longest(basic);
        String format = String.format("%%%ds = ", max);

        String[][] terms = OMatrix.niceTerms(dict, nb, precision);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basic.length; i++) {
            sb.append(String.format(format, basic[i]));
            sb.append(OMatrix.exprToString(terms[i], nb, precision));
            sb.append("\n");
        }

        return sb.toString();
    }
    
    
    
    /**
     * Return a nicely formatted primal dictionary as
     * a {@code String} with 2 decimal precision.
     *
     * @param  lp
     *         A {@code LP}.
     * @return
     *         A nicely formated {@code String}.
     *         
     */
    public static String primal(LP lp) {
    	return primal(lp, 2);
    }
    
    
    
    /**
     * Return a nicely formatted dual dictionary as a {@code String}.
     *
     * @param  lp
     *         A {@code LP}.
     * @param  precision
     *         Limit each double precision number to this many decimals.
     *         Give a negative value to automatically set precision.
     * @return
     *         A nicely formated {@code String}.
     *         
     */
    public static String dual(LP lp, int precision) {
    	Matrix dict = lp.dictionary().transpose().scale(-1);
    	
    	String[] basic = OLP.insert(lp.getDualBasic(), "-ξ");
    	String[] nb = OLP.insert(lp.getDualNonBasic(), "");
    	
    	int max = OLP.longest(basic);
    	String format = String.format("%%%ds = ", max);
    	
    	String[][] terms = OMatrix.niceTerms(dict, nb, precision);
    	
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basic.length; i++) {
            sb.append(String.format(format, basic[i]));
            sb.append(OMatrix.exprToString(terms[i], nb, precision));
            sb.append("\n");
        }

        return sb.toString();
    }
    
    
    
    /**
     * Return a nicely formatted dual dictionary as
     * a {@code String} with 2 decimal precision.
     *
     * @param  lp
     *         A {@code LP}.
     * @return
     *         A nicely formated {@code String}.
     *         
     */
    public static String dual(LP lp) {
    	return dual(lp, 2);
    }
}
