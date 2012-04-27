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

import lpped.Matrix;

public final class Output {
    /**
     * Return a nicely formatted {@code String} that represents the
     * matrix-vector product of this {@code Matrix} and the given
     * vector given as an {@code Array}.
     *
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
}
