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
package lpped;

/**
 * The {@code Matrix} class represents a rectangular array of double precision
 * numbers. The {@code Matrix} class is immutable.
 *
 * @author  Andreas Halle
 * @version 0.1
 */
public class Matrix {
    private double[][] data;
    private int m;
    private int n;



    /**
     * Initialize a newly created {@code Matrix} object so that it represents
     * the same rectangular array of numbers as the argument. The newly
     * created {@code Matrix} is a copy of the argument @{code Matrix}.
     *
     * @param matrix
     *        A {@code Matrix}.
     */
    Matrix(Matrix matrix) {
        this(matrix.data);
    }


    /**
     * Initialize a newly created {@code Matrix object from a given one-
     * dimensional array of double precision numbers. This matrix will
     * have one row.
     *
     * @param data
     *        An {@code array} of {@code double} precision numbers.
     */
    Matrix(double[] data) {
        this(1, data.length);
        this.data[0] = data.clone();
    }



    /**
     * Initialize a newly created {@code Matrix} object from a given two-
     * dimensional array of double precision numbers.
     *
     * @param data
     *        An {@code array} of {@code double} precision numbers.
     */
    Matrix(double[][] data) {
        m = data.length;
        n = data[0].length;
        this.data = copyData(data);
    }



    /**
     * Initialize a newly created {@code Matrix} object with m rows and
     * n columns. All elements in the {@code Matrix} are set to {@code 0.0}.
     *
     * @param m
     *        Number of rows in the {@code Matrix}·
     * @param n
     *        Number of columns in the {@code Matrix}·
     */
    Matrix(int m, int n) {
        this.m = m;
        this.n = n;
        data = new double[m][n];
    }



    /**
     * Return a newly created {@code Matrix} augmented of the parent
     * {@code Matrix} and the given {@code Matrix}.
     *
     * @param  B
     *         {@code Matrix} to append to the parent {@code Matrix}.
     * @return
     *         An augmented {@code Matrix}.
     */
    Matrix augment(Matrix B) {
        if (m != B.m) {
            throw new IllegalArgumentException(String.format("Illegal"
                    + " operation: Cannot augment a %d x %d matrix with a"
                    + " %d x %d matrix", m, n, B.m, B.n));
        }

        Matrix C = new Matrix(m, n+B.n);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C.data[i][j] = data[i][j];
            }
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < B.n; j++) {
                C.data[i][j+n] = B.data[i][j];
            }
        }

        return C;
    }



    /**
     * Return a newly created {@code Matrix} where each element is the
     * corresponding element of the parent {@code Matrix} added to
     * the corresponding element in the given {@code Matrix}.
     *
     * @param  B
     *         {@code Matrix} to add to the parent {@code Matrix}.
     * @return
     *         A {@code Matrix}.
     */
    Matrix add(Matrix B) {
        if (m != B.m || n != B.n) {
            String e = String.format("Illegal operation: cannot add a %d x %d "
                    + "matrix to a %d x %d matrix.", B.m, B.n, m, n);
            throw new IllegalArgumentException(e);
        }
        Matrix C = new Matrix(m, n);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C.data[i][j] = data[i][j] + B.data[i][j];
            }
        }
        return C;
    }



    /**
     *
     * @return
     *         the number of columns in the {@code Matrix}.
     */
    int cols() { return n; }



    /**
     *
     * @return
     *         copy of the {@code array} of numbers in the {@code Matrix}.
     */
    double[][] data() {
        double[][] data = new double[m][n];
        for (int i = 0; i < data.length; i++) this.data[i] = data[i].clone();
        return data;
    }



    /**
     * Return the number in the ith row and jth column.
     *
     * @param  i
     *         Row i
     * @param  j
     *         Column j
     * @return
     *         A double precision number.
     */
    double get(int i, int j) { return data[i][j]; }



    /**
     * Return the jth column vector in the {@code Matrix}.
     *
     * @param  j
     *         Column j
     * @return
     *         A column vector.
     */
    Matrix getCol(int j) {
        double[] col = new double[m];
        for (int i = 0; i < m; i++) {
            col[i] = data[i][j];
        }
        return new Matrix(col).transpose();
    }



    /**
     * Return the ith row vector in the {@code Matrix}.
     *
     * @param  i
     *         Row i
     * @return
     *         A row vector.
     */
    Matrix getRow(int i) {
        return new Matrix(data[i]);
    }



    /**
     * Check if each element is greater than a given value.
     *
     * @param  value
     *         A {@code double} precision number.
     * @return
     *         {@code true} if every element is greater than the given value.
     *         {@code false} otherwise.
     */
    boolean gt(double value) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j] <= value) return false;
            }
        }
        return true;
    }



    /**
     * Check if each element is greater than or equal to a given value.
     *
     * @param  value
     *         A {@code double} precision number.
     * @return
     *         {@code true} if every element is greater than or equal to
     *         the given value. {@code false} otherwise.
     */
    boolean gte(double value) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j] < value) return false;
            }
        }
        return true;
    }



    /**
     * Return the inverse of the {@code original}.
     * <p>
     * This method uses Gauss-Jordan elimination
     * and has a time complexity of O(n<sup>3</sup>).
     *
     * @return
     *         The inverse of the {@code original}.
     */
    Matrix inverse() {
        if (m != n) {
            String e = "Cannot invert a non-square matrix.";
            throw new IllegalArgumentException(e);
        }
        Matrix C = this.augment(Matrix.identity(m));

        for (int i = 0; i < C.m; i++) {

            if (C.data[i][i] == 0.0) {
                String e = "Cannot invert a singular matrix";
                throw new IllegalArgumentException(e);
            }

            C.rowScale(i, (double)1/C.data[i][i]);

            for (int k = 0; k < C.m; k++) {
                if (k != i) {
                    C.rowSubtract(k, i, (double)C.data[k][i] / C.data[i][i]);
                }
            }
        }
        return C.subMatrix(0, m-1, m, C.n-1);
    }



    /**
     * Check if each element is less than a given value.
     *
     * @param  value
     *         A {@code double} precision number.
     * @return
     *         {@code true} if every element is less than the given value.
     *         {@code false} otherwise.
     */
    boolean lt(double value) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j] >= value) return false;
            }
        }
        return true;
    }



    /**
     * Check if each element is less than or equal to a given value.
     *
     * @param  value
     *         A {@code double} precision number.
     * @return
     *         {@code true} if every element is less than or equal to
     *         the given value. {@code false} otherwise.
     */
    boolean lte(double value) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j] > value) return false;
            }
        }
        return true;
    }



    /**
     * Return a newly created {@code Matrix} that is the cross product between
     * the {@code original} and the given {@code Matrix}·
     *
     * @param  B
     *         A {code Matrix}.
     * @return
     *         A cross product between two matrices.
     */
    Matrix product(Matrix B) {
        if (n != B.m) {
            String e = String.format("Illegal matrix operation: Cannot compute"
                    + " the cross product of two matrices whose dimensions are"
                    + " %d x %d and %d x %d.", m, n, B.m, B.n);
            throw new IllegalArgumentException(e);
        }

        double[][] cdata = new double[m][B.n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < B.n; j++) {
                for (int k = 0; k < n; k++) {
                    cdata[i][j] += data[i][k] * B.data[k][j];
                }
            }
        }
        return new Matrix(cdata);
    }



    /**
     *
     * @return
     *         Number of rows in the {@code Matrix}.
     */
    int rows() { return m; }



    /**
     * Return a newly created {@code Matrix} similar
     * to the {@code original}, but with the element
     * in the ith row and jth column set to the given value.
     *
     * @param  i
     *         ith row.
     * @param  j
     *         jth column.
     * @param  value
     *         A {@code double} precision number.
     * @return
     *         A {@code Matrix}.
     */
    Matrix set(int i, int j, double value) {
        Matrix C = new Matrix(this);
        C.data[i][j] = value;
        return C;
    }



    /**
     * Return a newly created {@code Matrix} similar
     * to the {code original}, but with the jth
     * column replaced with the given column.
     *
     * @param  j
     *         jth column.
     * @param  col
     *         A column vector.
     * @return
     *         A {@code Matrix}.
     */
    Matrix setCol(int j, Matrix col) {
        double[][] cdata = copyData(data);
        for (int i = 0; i < m; i++) {
            cdata[i][j] = col.data[i][0];
        }
        return new Matrix(cdata);
    }



    /**
     * Return a newly created {@code Matrix} similar to the {code original},
     * but with the ith row replaced with the given row.
     *
     * @param  i
     *         ith row.
     * @param  row
     *         A row vector.
     * @return
     *         A {@code Matrix}.
     */
    Matrix setRow(int i, Matrix row) {
        double[][] cdata = copyData(data);
        for (int j = 0; j < n; j++) {
            cdata[i][j] = row.data[i][j];
        }
        return new Matrix(cdata);
    }



    /**
     * Return a newly created {@code Matrix} equal to the {@code original} but
     * with each element in the matrix multiplied by a scalar.
     *
     * @param  scalar
     *         number to multiply to each element.
     * @return
     *         A {@code Matrix}.
     */
    Matrix scale(double scalar) {
        double[][] cdata = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                cdata[i][j] = scalar*data[i][j];
            }
        }
        return new Matrix(cdata);
    }



    /**
     * Return a sub matrix of the parent @{code Matrix} beginning and ending
     * at given row and column indices.
     *
     * @param  rs
     *         Begin sub matrix at this row index.
     * @param  re
     *         End sub matrix at this row index.
     * @param  cs
     *         Begin sub matrix at this column index.
     * @param  ce
     *         End sub matrix at this column index.
     * @return
     *         A sub matrix of the parent @{code matrix} beginning at row
     *         index {@code rs} and column index {@code cs} and ending at
     *         row index {@code re} and column index {@code ce}.
     *
     */
    Matrix subMatrix(int rs, int re, int cs, int ce) {
        if (re-rs < 0 || ce-cs < 0 || rs >= m || re >= m || cs >= n
                                                                  || ce >= n) {
            String e = String.format("Cannot create a sub matrix from rows"
                    + " %d through %d and columns %d through %d of a"
                    + " %d x %d matrix.", rs+1, re+1, cs+1, ce+1, m, n);
            throw new IllegalArgumentException(e);
        }

        double cdata[][] = new double[re-rs+1][ce-cs+1];
        for (int i = rs; i <= re; i++) {
            for (int j = cs; j <= ce; j++) {
                cdata[i-rs][j-cs] = data[i][j];
            }
        }
        return new Matrix(cdata);
    }



    /**
     * Return a newly created {@code Matrix} where each element is the
     * corresponding element of the parent {@code Matrix} subtracted by
     * the corresponding element in the given {@code Matrix}.
     *
     * @param  b
     *         {@code Matrix} to subtract from parent {@code Matrix}.
     * @return
     *         A {@code Matrix}.
     */
    Matrix subtract(Matrix B) {
        if (m != B.m || n != B.n) {
            String e = String.format("Illegal matrix operation: Cannot"
                    + " subtract a %d x %d matrix from a %d x %d matrix.",
                    m, n, B.m, B.n);
            throw new IllegalArgumentException(e);
        }

        double[][] cdata = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                cdata[i][j] = data[i][j] - B.data[i][j];
            }
        }
        return new Matrix(cdata);
    }



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
    protected String toString(String[] x, int precision) {
        if (x.length != n) {
            String e = String.format("Illegal matrix operation: Cannot"
                    + " multiply a %d x %d matrix with a vector whose"
                    + " dimension is %d.", m, n, x.length);
            throw new IllegalArgumentException(e);
        }

        String[][] elements = new String[m][n];
        int[] lCol = new int[n];
        boolean posfcol = true;

        String f;
        if (precision < 0) f = "%s%s"; // Automatically set a precision.
        else f = String.format("%%.%df%%s", precision);

        /* Calculate the product of each element in the matrix. */
        for (int i = 0; i < n; i++) {
            int max = 0;
            for (int j = 0; j < m; j++) {
                double coeff = Math.abs(data[j][i]);

                if (coeff == 0.0)
                    elements[j][i] = "";
                else if (coeff == 1.0 && !x[i].trim().equals(""))
                    elements[j][i] = x[i];
                else
                    elements[j][i] = String.format(f, coeff, x[i]);

                /* 
                 * Check if the coefficients in the
                 * first column are all positive.
                 */
                if (data[j][0] < 0) posfcol = false;

                /*
                 * Find the space needed to print out
                 * the largest variable in each column.
                 */
                if (elements[j][i].length() > max) {
                    max = elements[j][i].length();
                }
            }
            lCol[i] = (max > 0) ? max : 1;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < m; i++) {
            if (i != 0) sb.append("\n");

            for (int j = 0; j < n; j++) {
                if (j != 0) sb.append(" ");

                char sign = ' ';
                if (data[i][j] > 0 && j != 0) sign = '+';
                else if (data[i][j] < 0) sign = '-';

                String fele = String.format("%%c %%%ds", lCol[j]);
                if (j == 0 && posfcol) {
                    fele = String.format("%%%ds", lCol[j]);
                    sb.append(String.format(fele, elements[i][j]));
                }
                else sb.append(String.format(fele, sign, elements[i][j]));
            }
        }

        return sb.toString();
    }



    /**
     * Return the transpose of the {@code original}.
     *
     * @return
     *         The transpose of the {@code original}.
     */
    Matrix transpose() {
        Matrix C = new Matrix(n, m);
        for (int i = 0; i < C.m; i++) {
            for (int j = 0; j < C.n; j++) {
                C.data[i][j] = data[j][i];
            }
        }
        return C;
    }



    public boolean equals(Matrix o) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j] != o.data[i][j]) return false;
            }
        }
        return true;
    }



    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%9.4f ", data[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }


    /**
     * Return the nth unit vector in the given dimension.
     *
     * @param  dim
     *         The dimension of the vector.
     * @param  n
     *         nth unit vector.
     * @return
     *         A unit vector.
     */
    public static Matrix unitVector(int dim, int n) {
        double[][] e = new double[dim][1];
        e[n-1][0] = 1;
        return new Matrix(e);
    }



    /**
     * Initialize an identity matrix of size n x n.
     *
     * @param  n
     *         Number of rows and columns in the matrix.
     * @return
     *         A n x n identity matrix.
     */
    public static Matrix identity(int n) {
        Matrix identity = new Matrix(n, n);

        for (int i = 0; i < n; i++) {
            identity.data[i][i] = 1;
        }
        return identity;
    }



    /*
     * Multiply each element in the given row by the given scalar.
     */
    private void rowScale(int i, double scalar) {
        for (int k = 0; k < n; k++) {
            data[i][k] = data[i][k]*scalar;
        }
    }



    /*
     * Set row i equal to row i minus row j times a scalar.
     */
    private void rowSubtract(int i, int j, double scalar) {
        for (int k = 0; k < n; k++) {
            data[i][k] = data[i][k] - scalar*data[j][k];
        }
     }



    private double[][] copyData(double[][] data) {
        double[][] ndata = new double[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            ndata[i] = data[i].clone();
        }
        return ndata;
    }
}
