/*
 * Copyright (C) 2012 Andreas Halle
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
package output;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.linear.FieldMatrix;
import model.LP;

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
//    /**
//     * Return a nicely formatted LaTeX-formatted
//     * dual dictionary as a {@code String}.
//     *
//     * @param  lp
//     *         A {@code LP}.
//     * @param  precision
//     *         Limit each double precision number to this many decimals.
//     *         Give a negative value to automatically set precision.
//     * @return
//     *         A nicely formated {@code String}.
//     *         
//     */
//    public static String texDual(LP lp, int precision) {
//        FieldMatrix<BigFraction> dict = lp.dictionary().transpose()
//                .scalarMultiply(BigFraction.MINUS_ONE);
//
//        String[] basic = OLP.insert(lp.getDualBasic(), "-\\xi");
//        String[] nb = OLP.insert(lp.getDualNonBasic(), "");
//
//        int max = OLP.longest(basic);
//        String format = String.format("%%%ds &=& ", max);
//
//        String[][] terms = OMatrix.texNiceTerms(dict, nb, precision);
//
//        StringBuilder sb = new StringBuilder();
//        
//        sb.append("\\begin{array}{l");
//        for (int i = 0; i < nb.length-1; i++) {
//            sb.append("cr");
//        }
//        sb.append("}\n");
//        
//        for (int i = 0; i < basic.length; i++) {
//            sb.append(" ");
//            sb.append(String.format(format, basic[i]));
//            sb.append(OMatrix.join(terms[i], " "));
//            sb.append("\\\\");
//            if (i == 0) {
//                sb.append("\\hline");
//            }
//            sb.append("\n");
//        }
//        
//        sb.append("\\end{array}\n");
//
//        return sb.toString();
//    }
//    
//    
//    
//    /**
//     * Return a nicely formatted LaTeX-formatted
//     * primal dictionary as a {@code String}.
//     * 
//     * 
//     * @param  lp
//     *         A {@code LP}.
//     * @param  precision
//     *         Limit each double precision number to this many decimals.
//     *         Give a negative value to automatically set precision.
//     * @return
//     *         A nicely formated {@code String}.
//     */
//    public static String texPrimal(LP lp, int precision) {
//        Matrix dict = lp.dictionary();
//        
//        String[] basic = OLP.insert(lp.getBasic(), "\\zeta");
//        String[] nb = OLP.insert(lp.getNonBasic(), "");
//        
//        int max = OLP.longest(basic);
//        String format = String.format("%%%ds &=& ", max);
//        
//        String[][] terms = OMatrix.texNiceTerms(dict, nb, precision);
//        
//        StringBuilder sb = new StringBuilder();
//        sb.append("\\begin{array}{l");
//        for (int i = 0; i < nb.length-1; i++) {
//            sb.append("cr");
//        }
//        sb.append("}\n");
//        
//        for (int i = 0; i < basic.length; i++) {
//            sb.append(" ");
//            sb.append(String.format(format, basic[i]));
//            sb.append(OMatrix.join(terms[i], " "));
//            sb.append("\\\\");
//            if (i == 0) {
//                sb.append("\\hline");
//            }
//            sb.append("\n");
//        }
//        
//        sb.append("\\end{array}\n");
//        
//        return sb.toString();
//    }



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
    public static String toString(FieldMatrix<BigFraction> A,
                                  String[] x, int precision) {
        StringBuilder sb = new StringBuilder();
        String[][] terms = OMatrix.niceTerms(A, x, precision);

        for (int i = 0; i < A.getRowDimension(); i++) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(OMatrix.join(terms[i], " "));
        }
        return sb.toString();
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
        FieldMatrix<BigFraction> dict = lp.dictionary();

        String[] basic = OLP.insert(lp.getBasic(), "ζ");
        String[] nb = OLP.insert(lp.getNonBasic(), "");
        
        int max = OLP.longest(basic);
        String format = String.format("%%%ds = ", max);

        String[][] terms = OMatrix.niceTerms(dict, nb, precision);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basic.length; i++) {
            sb.append(String.format(format, basic[i]));
            sb.append(OMatrix.join(terms[i], " "));
            sb.append("\n");
        }

        return sb.toString();
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
        FieldMatrix<BigFraction> dict = lp.dictionary().transpose()
                .scalarMultiply(BigFraction.MINUS_ONE);

        String[] basic = OLP.insert(lp.getDualBasic(), "-ξ");
        String[] nb = OLP.insert(lp.getDualNonBasic(), "");

        int max = OLP.longest(basic);
        String format = String.format("%%%ds = ", max);

        String[][] terms = OMatrix.niceTerms(dict, nb, precision);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basic.length; i++) {
            sb.append(String.format(format, basic[i]));
            sb.append(OMatrix.join(terms[i], " "));
            sb.append("\n");
        }

        return sb.toString();
    }
}
