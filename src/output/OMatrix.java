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

import model.LP;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.linear.FieldMatrix;

import output.Output.Format;

/**
 * Class containing output-methods specifically
 * for the {@code Matrix} object.
 *
 * @author  Andreas Halle
 * @see     model.Matrix
 */
final class OMatrix {
    /*
     * Return an array containing the length
     * of the longest string in each column.
     */
    private static int[] colSizes(String[][] elements) {
        int[] colSizes = new int[elements[0].length];
        for (int j = 0; j < elements[0].length; j++) {
            int max = 1;
            for (int i = 0; i < elements.length; i++) {
                int len = elements[i][j].length();

                if (len > max) {
                    max = len;
                }
            }
            colSizes[j] = max;
        }
        return colSizes;
    }



    /**
     * Join an array of {@code Strings} into a single 
     * {@code String} separated by the given separator.
     * 
     * @param  arr
     *         {@code Array} of {@code Strings} to join together. 
     * @param  separator
     *         Separator between each {@code String}.
     * @return
     *         A {@code String}.
     */
    static String join(String[] arr, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i != 0) {
                sb.append(separator);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }
    
    
    

    /**
     * Nicely format the spacing in every term in the matrix-vector
     * product based on the longest term in each column.
     *
     * @param  A
     *         The {@code Matrix}
     * @param  x
     *         The vector as a {@code String}-array.
     * @param  precision
     *         Limit each double precision number to this many decimals.
     *         Give a negative value to automatically set precision.
     * @return
     *         A 2D array of nicely formatted terms.
     */
    static String[][] niceTerms(FieldMatrix<BigFraction> A,
                                String[] x, Format f) {
        int m = A.getRowDimension();
        int n = A.getColumnDimension();

        String[][] terms = terms(A, x, f);
        int[] cols = colSizes(terms); // Find longest element in each column.
        /* Is the first column all positive? */
        boolean fcol = 
          LP.getMinValue(A.getColumnVector(0)).compareTo(BigFraction.ZERO) >= 0; 

        String[][] niceTerms = new String[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                char sign = signify(A.getEntry(i, j), j);

                String ff = String.format("%%c %%%ds", cols[j]);
                if (j == 0 && fcol) {
                    ff = String.format("%%%ds", cols[j]);
                    niceTerms[i][j] = String.format(ff, terms[i][j]);
                } else {
                    niceTerms[i][j] = String.format(ff, sign, terms[i][j]);
                }
            }
        }
        return niceTerms;
    }
    
    
    
    /*
     * Return the sign to put in front of a term.
     */
    private static char signify(BigFraction coeff, int column) {
        char sign = ' ';
        if (coeff.compareTo(BigFraction.ZERO) > 0 && column != 0) {
            sign = '+';
        } else if (coeff.compareTo(BigFraction.ZERO) < 0) {
            sign = '-';
        }
        return sign;
    }

    

    /*
     * Format a term as nicely as possible.
     */
    private static String term(BigFraction coeff, String var, Format f) {
        if (coeff.equals(BigFraction.ZERO)) {
            return "";
        } else if (coeff.equals(BigFraction.ONE) && !var.trim().equals("")) {
            return var;
        }
        
        String cstr = Output.number(coeff, f);
        return String.format("%s %s", cstr, var);
    }



    /*
     * Calculate the term of each element
     * in the matrix-vector-product.
     */
    private static String[][] terms(FieldMatrix<BigFraction> A,
                                    String[] x, Format f) {
        int m = A.getRowDimension();
        int n = A.getColumnDimension();
        String[][] elements = new String[m][n];
    
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                BigFraction coeff = A.getEntry(i, j).abs();
                String element = term(coeff, x[j], f);
    
                elements[i][j] = element;
            }
        }
        return elements;
    }



//    /**
//     * Nicely format the spacing in every term in the matrix-vector
//     * product based on the longest term in each column. In LaTeX format.
//     *
//     * @param  A
//     *         The {@code Matrix}
//     * @param  x
//     *         The vector as a {@code String}-array.
//     * @param  precision
//     *         Limit each double precision number to this many decimals.
//     *         Give a negative value to automatically set precision.
//     * @return
//     *         A 2D array of nicely formatted terms in LaTeX.
//     */
//    static String[][] texNiceTerms(Matrix A, String[] x, int precision) {
//        int m = A.rows();
//        int n = A.cols();
//        
//        String[][] terms = terms(A, x, precision);
//        int [] cols = colSizes(terms); // Find longest element in each column.
//        boolean fcol = A.getCol(0).gte(0); // First column all positive?
//        
//        String[][] niceTerms = new String[m][n];
//        for (int i = 0; i < m; i++) {
//            for (int j = 0; j < n; j++) {
//                String sign = texSignify(A.get(i, j), j);
//                
//                String f = String.format("%%s %%%ds", cols[j]);
//                if (j == 0 && fcol) {
//                    f = String.format("%%%ds", cols[j]);
//                    niceTerms[i][j] = String.format(f, terms[i][j]);
//                } else {
//                    niceTerms[i][j] = String.format(f, sign, terms[i][j]);
//                }
//            }
//        }
//        return niceTerms;
//    }
//
//
//
//    /* 
//     * Return the sign (surrounded by &'s) to put in front of a term.
//     */
//    private static String texSignify(double coeff, int column) {
//        return String.format("&%c&", signify(coeff, column));
//    }
}
