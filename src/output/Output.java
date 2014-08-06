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
package output;

import java.math.BigInteger;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.linear.FieldMatrix;

import model.LP;

/**
 * The {@code Output} class contains methods that
 * return {@code Strings} that are ready to be
 * output by the command-line interface.
 *  
 * @author  Andreas Halle
 * @see     lightshell.Shell
 */
public final class Output {
    /* Some enums for output format */
    public static enum Format {
        DECIMAL2("Force two decimals."),
        DECIMAL4("Force four decimals."),
        DECIMAL8("Force eight decimals."),
        DECIMAL16("Force 16 decimals."),
        FRACTION("Display numbers as fractions on the format numerator/"+
                 "denominator. (default)")
        ;
        
        String desc;
        
        Format(String desc) {
            this.desc = desc;
        }
        
        public String getDesc() {
            return desc;
        }
    }
    
    
    
    private static String toString(BigFraction bf) {
        String str = null;
        BigInteger denominator = bf.getDenominator();
        BigInteger numerator = bf.getNumerator();
        if (BigInteger.ONE.equals(denominator)) {
            str = numerator.toString();
        } else if (BigInteger.ZERO.equals(numerator)) {
            str = "0";
        } else {
            str = numerator + "/" + denominator;
        }
        return str;
    }
    
    
    
    public static String number(BigFraction bf, Format f) {
        switch(f) {
        case FRACTION: return toString(bf);
        default: return String.format("%.2f", bf.doubleValue());
        }
    }
    
    
    
    /**
     * Return a nicely formatted dual dictionary as a {@code String}.
     *
     * @param  lp
     *         A {@code LP}.
     * @param  f
     *         Format numbers using the given format.
     * @return
     *         A nicely formated {@code String}.
     *         
     */
    public static String dual(LP lp, Format f) {
        FieldMatrix<BigFraction> dict = lp.dictionary().transpose()
                .scalarMultiply(BigFraction.MINUS_ONE);

        String[] basic = OLP.insert(lp.getDualBasic(), "-ξ");
        String[] nb = OLP.insert(lp.getDualNonBasic(), "");

        int max = OLP.longest(basic);
        String format = String.format("%%%ds = ", max);

        String[][] terms = OMatrix.niceTerms(dict, nb, f);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basic.length; i++) {
            sb.append(String.format(format, basic[i]));
            sb.append(OMatrix.join(terms[i], " "));
            if (i < basic.length-1)  sb.append("\n");
        }

        return sb.toString();
    }
    
    
    
    /**
     * Return a {@code String} saying whether the incumbent basic solution is
     * primally and/or dually (in)feasible.
     * 
     * @param  lp {@code LP} to check.
     * @return a {@code String}.
     */
    public static String feasibility(LP lp) {
        StringBuilder sb = new StringBuilder();

        boolean p = lp.feasible(false);
        boolean d = lp.feasible(true);
        if (p) sb.append("Incumbent basic solution is primally feasible");
        else   sb.append("Incumbent basic solution is primally infeasible");
        if (d) sb.append(" and dually feasible.");
        else   sb.append(" and dually infeasible.");
        
        return sb.toString();
    }
    
    
    
    /**
     * Return a {@code String} saying whether the incumbent basic solution is
     * optimal or not.
     * 
     * @param  lp {@code LP} to check.
     * @return a {@code String}.
     */
    public static String optimality(LP lp) {
        if (lp.feasible(false) && lp.feasible(true))
            return "Incumbent basic solution is optimal.";
        return "Incumbent basic solution is not optimal.";
    }



    /*
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
    public static String primal(LP lp, Format f) {
        FieldMatrix<BigFraction> dict = lp.dictionary();
    
        String[] basic = OLP.insert(lp.getBasic(), "ζ");
        String[] nb = OLP.insert(lp.getNonBasic(), "");
        
        int max = OLP.longest(basic);
        String format = String.format("%%%ds = ", max);
    
        String[][] terms = OMatrix.niceTerms(dict, nb, f);
    
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < basic.length; i++) {
            sb.append(String.format(format, basic[i]));
            sb.append(OMatrix.join(terms[i], " "));
            if (i < basic.length-1)  sb.append("\n");
        }
    
        return sb.toString();
    }



    /**
     * Return a nicely formatted {@code String} that represents the
     * matrix-vector product of the given {@code Matrix} and the
     * given vector given as an {@code Array} with a given precision.
     *
     * @param  A
     *         A {@code Matrix}.
     * @param  x
     *         A vector as an {@code array} of {@code Strings}.
     * @param  f
     *         Format numbers using the given format.
     * @return
     *         A nicely formatted {@code String}.
     */
    public static String toString(FieldMatrix<BigFraction> A,
                                  String[] x, Format f) {
        StringBuilder sb = new StringBuilder();
        String[][] terms = OMatrix.niceTerms(A, x, f);
    
        for (int i = 0; i < A.getRowDimension(); i++) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(OMatrix.join(terms[i], " "));
        }
        return sb.toString();
    }
}
