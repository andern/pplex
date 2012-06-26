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
package model;

import java.util.Arrays;
import java.util.HashMap;

/**
 * An {@code Object} representing a linear program (LP).
 *
 * @author  Andreas Halle
 * @version 0.2
 * @see     controller.Parser
 */
public class LP {
    private Matrix B;
    private Matrix N;
    
    public Matrix B_;
    public Matrix N_;

    public Matrix b;
    public final Matrix c;
    private Matrix x_b;
    private Matrix z_n;

    private int[] Bi;
    private int[] Ni;
    private HashMap<Integer, String> x;



    /**
     * Initializes a linear program.
     * <p>
     * n being the number of variables and m being the number of constraints,
     * this {@code constructor} does the following:
     * <p><blockquote><pre>
     *     B is set to the identity matrix of dimension m.
     *
     *     The indices of the basic and non-basic variables are set to
     *     0..n-1 and n-1..n+m-1, respectively.
     *
     *     The slack variables are called w1..wm.
     * </pre></blockquote<p>
     *
     * @param N
     *        A {@code Matrix} with the coefficients
     *        of the non-basic variables.
     * @param b
     *        A {@code Matrix} with the upper bounds on
     *        the constraints in the original program.
     * @param c
     *        A {@code Matrix} with the coefficients of the
     *        decision variables in the original program.
     * @param x
     *        A {@code HashMap} mapping the indices of the
     *        basic and non-basic variables to their names.
     */
    public LP(Matrix N, Matrix b, Matrix c, HashMap<Integer, String> x) {
        this(Matrix.identity(N.rows()), N, Matrix.identity(N.rows()), N, b, c,
                                              b, c.scale(-1),
                                              new int[N.rows()],
                                              new int[N.cols()], x);

        for (int i = 0; i < Ni.length; i++) Ni[i] = i;
        for (int i = 0; i < Bi.length; i++) {
            Bi[i] = i + Ni.length;
            x.put(Bi[i], "w" + (i+1));
        }
    }



    /*
     * Initializes a linear program.
     *
     * @param B
     *        A {@code Matrix} with the coefficients of the basic variables.
     * @param N
     *        A {@code Matrix} with the coefficients
     *        of the non-basic variables.
     * @param B_
     *        A {@code Matrix} with the coefficients of the
     *        basic variables in the original dictionary.
     * @param N_
     *        A {@code Matrix} with the coefficients of the
     *        non-basic variables in the original dictionary.
     * @param b
     *        A {@code Matrix} with the upper bounds on
     *        the constraints in the original program.
     * @param c
     *        A {@code Matrix} with the coefficients of the
     *        decision variables in the original program.
     * @param x_b
     *        A {@code Matrix} with the upper bounds on the constraints
     *        in the current iteration of the simplex method.
     * @param z_n
     *        A {@code Matrix} with the coefficients of the decision variables
     *        in the current iteration of the simplex method.
     * @param Bi
     *        An {@code array} with the indices of the basic variables.
     * @param Ni
     *        An {@code array} with the indices of the non-basic variables.
     * @param x
     *        A {@code HashMap} mapping the indices of the
     *        basic and non-basic variables to their names.
     */
    private LP(Matrix B, Matrix N, Matrix B_, Matrix N_, Matrix b, Matrix c,
                                            Matrix x_b, Matrix z_n,
                                            int[] Bi, int[] Ni,
                                            HashMap<Integer, String> x) {
        this.B = B;
        this.N = N;
        
        this.B_ = B_;
        this.N_ = N_;
    
        this.b = b;
        this.c = c;
        this.x_b = x_b;
        this.z_n = z_n;
    
        this.Bi = Bi;
        this.Ni = Ni;
        this.x = x;
    }



    /**
     * Find an entering variable index according
     * to the largest coefficients rule.
     *
     * @param  dual
     *         If true, find an entering variable index for the dual problem.
     *         Otherwise, find one for the primal problem.
     * @return
     *         An entering variable index.
     */
    private int entering(boolean dual) {
        Matrix check = dual ? x_b : z_n;

        double min = 0.0;
        int index = -1;
        for (int i = 0; i < check.rows(); i++) {
            double val = check.get(i, 0);
            if (val < min) {
                min = val;
                index = i;
            }
        }

        if (index == -1) {
            String e = "Problem is optimal.";
            String e2 = String.format("Problem is %s infeasible",
                                       dual ? "dual" : "primal");

            if (optimal(dual)) throw new RuntimeException(e);
            if (!feasible(dual)) throw new RuntimeException(e2);
        }
        return index;
    }



    /**
     * Return whether the program is feasible or not.
     *
     * @param  dual
     *         If true, check for dual feasibility.
     *         Otherwise, check for primal feasibility.
     * @return
     *         True if the program is feasible. False otherwise.
     */
    public boolean feasible(boolean dual) {
        if (dual) return z_n.gte(0);
        return x_b.gte(0);
    }



    /**
     * Find a leaving variable index that is the most
     * bounding on the given entering variable index.
     *
     * @param  entering
     *         an entering variable index.
     * @param  dual
     *         If true, find a leaving variable index for the dual problem.
     *         Otherwise, find one for the primal problem.
     * @return
     *         A leaving variable index.
     */
    private int leaving(int entering, boolean dual) {
        Matrix check, sd;
        Matrix bin = B.inverse().product(N);

        String e = "Problem is unbounded.";
        if (dual) {
            check = z_n;
            Matrix unit = Matrix.unitVector(bin.rows(), entering+1);
            sd = bin.transpose().scale(-1).product(unit);
        }
        else {
            check = x_b;
            Matrix unit = Matrix.unitVector(bin.cols(), entering+1);
            sd = bin.product(unit);
        }

        double max = Double.MIN_VALUE;
        int index = -1;

        for (int i = 0; i < sd.rows(); i++) {
            double num = sd.get(i, 0);
            double denom = check.get(i, 0);

            if (denom != 0) {
                double val = num / denom;
                if (val > max) {
                    max = val;
                    index = i;
                }
            } else {
               if (num > 0) return i;
            }
        }
        if (index == -1) throw new RuntimeException(e);
        return index;
    }



    /**
     * Return the objective value of the current dictionary.
     *
     * @return
     *         the objective value.
     */
    public double objVal() {
        double sum = 0;
        for (int i = 0; i < Bi.length; i++) {
            int j = Bi[i];
            if (j < c.rows()) sum += c.get(j, 0)*x_b.get(i, 0);
        }
        return sum;
    }


    /**
     * Return whether the program is optimal or not.
     *
     * @param  dual
     *         If true, check for dual optimality.
     *         Otherwise, check for primal optimality.
     * @return
     *         True if the program is optimal. False otherwise.
     */
    public boolean optimal(boolean dual) {
        if (dual) return feasible(true) && x_b.gte(0);
        return feasible(false) && z_n.gte(0);
    }



    /**
     * Return a new linear program with a new objective function
     * making the program dually feasible.
     *
     * @return A linear program.
     */
    public LP phaseOneObj() {
        double zdata[] = new double[z_n.rows()];
        Arrays.fill(zdata, 1);

        Matrix z_n = new Matrix(zdata).transpose();
        return new LP(B, N, B_, N_, b, c, x_b, z_n, Bi, Ni, x);
    }



    /**
     * Return a new linear program with a new objective function
     * made of the given coefficients.
     *
     * @param  coeff
     *         Coefficients of the decision variables.
     * @return
     *         A linear program with the new objective function.
     */
    public LP replaceObj(double[] coeff) {
        Matrix z_n = new Matrix(coeff).transpose().scale(-1);
        return new LP(B, N, B_, N_, b, c, x_b, z_n, Bi, Ni, x);
    }



    /**
     * Do one iteration of the simplex method.
     *
     * @param  entering
     *         Index of variable to enter the basis.
     * @param  leaving
     *         Index of variable to leave the basis.
     * @return
     *         A linear program after one iteration.
     */
    public LP pivot(int entering, int leaving) {
        Matrix bin = B.inverse().product(N);
        // Step 1: Check for optimality
        // Step 2: Select entering variable.
        // Naive method. Does not check for optimality. Assumes feasibility.
        // Entering variable is given.

        // Step 3: Compute primal step direction.
        Matrix ej = Matrix.unitVector(bin.cols(), entering+1);
        Matrix psd = bin.product(ej);

        // Step 4: Compute primal step length.
        // Step 5: Select leaving variable.
        // Leaving variable is given.
        double t = x_b.get(leaving, 0) / psd.get(leaving, 0);

        // Step 6: Compute dual step direction.
        Matrix ei = Matrix.unitVector(bin.rows(), leaving+1);
        Matrix dsd = bin.transpose().scale(-1).product(ei);

        // Step 7: Compute dual step length.
        double s = z_n.get(entering, 0) / dsd.get(entering, 0);

        // Step 8: Update current primal and dual solutions.
        Matrix nx_b = x_b.subtract(psd.scale(t)).set(leaving, 0, t);
        Matrix nz_n = z_n.subtract(dsd.scale(s)).set(entering, 0, s);

        // Step 9: Update basis.
        Matrix temp = B.getCol(leaving);
        Matrix nB = B.setCol(leaving, this.N.getCol(entering));
        Matrix nN = N.setCol(entering, temp);

        int[] nBi = Bi.clone();
        int[] nNi = Ni.clone();
        nBi[leaving] = Ni[entering];
        nNi[entering] = Bi[leaving];

        return new LP(nB, nN, B_, N_, b, c, nx_b, nz_n, nBi, nNi, x);
    }



    /**
     * Do one iteration of the simplex method.
     *
     * @param  dual
     *         If true, run the dual simplex method.
     *         Otherwise, run the primal simplex method.
     * @return
     *         A linear program after one iteration.
     */
    public LP pivot(boolean dual) {
        int e = entering(dual);
        int l = leaving(e, dual);
        if (dual) return pivot(l, e);
        return pivot(e, l);
    }



    public LP updateObj() {
        double zdata[] = new double[z_n.rows()];
        Matrix bin = B.inverse().product(N);

        for (int i = 0; i < Bi.length; i++) {
            int k = Bi[i];
            if (k < Ni.length) {
                for (int j = 0; j < Ni.length; j++) {
                    zdata[j] += c.get(k, 0)*bin.get(i, j);
                }
            }
        }

        for (int i = 0; i < Ni.length; i++) {
            int k = Ni[i];
            if (k < Ni.length) zdata[i] += -c.get(i, 0);
        }

        Matrix z_n = new Matrix(zdata).transpose();
        return new LP(B, N, B_, N_, b, c, x_b, z_n, Bi, Ni, x);
    }



    public double[] point() {
        double[] point = new double[Ni.length];
        for (int i = 0; i < Bi.length; i++) {
            int j = Bi[i];
            if (j < Ni.length) point[j] = x_b.get(i, 0);
        }
        return point;
    }



    public int getNoNonBasic() {
        return Bi.length;
    }



    public int getNoBasic() {
        return Ni.length;
    }



    public String[] getBasic() {
        String[] basic = new String[Bi.length];
        for (int i = 0; i < Bi.length; i++) {
            basic[i] = x.get(Bi[i]);
        }
        return basic;
    }
    
    
    
    public Matrix getConstraints() {
        return N_.augment(b);
    }



    public String[] getNonBasic() {
        String[] nb = new String[Ni.length];
        for (int i = 0; i < Ni.length; i++) {
            nb[i] = x.get(Ni[i]);
        }
        return nb;
    }
    
    
    
    public String[] getDualNonBasic() {
        String[] vars = new String[Bi.length];
        
        for (int i = 0; i < Bi.length; i++) {
            String var;
            if (Bi[i] >= Ni.length) {
                var = "y" + (Bi[i] - Ni.length + 1);
            }
            else {
                var = "z" + (Bi[i] + 1);
            }
            
            vars[i] = var;
        }
        return vars;
    }
    
    
    
    public String[] getDualBasic() {
        String[] vars = new String[Ni.length];
        
        for (int i = 0; i < Ni.length; i++) {
            String var;
            if (Ni[i] >= Ni.length) {
                var = "y" + (Ni[i] - Ni.length + 1);
            }
            else {
                var = "z" + (Ni[i] + 1);
            }
            vars[i] = var;
        }
        return vars;
    }



    /**
     * @return
     *         A {@code Matrix} of double precision numbers representing
     *         the dictionary of the current Linear Program.
     */
    public  Matrix dictionary() {
        double[][] data = new double[Bi.length+1][Ni.length+1];
        for (int i = 0; i < Ni.length; i++) { data[0][i+1] = -z_n.get(i, 0); }
        for (int i = 0; i < Bi.length; i++) { data[i+1][0] = x_b.get(i, 0); }

        data[0][0] = objVal();

        Matrix values = B.inverse().product(N);
        for (int i = 0; i < Bi.length; i++) {
            for (int j = 0; j < Ni.length; j++) {
                data[i+1][j+1] = -values.get(i, j);
            }
        }

        return new Matrix(data);
    }
}
