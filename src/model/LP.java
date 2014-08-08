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
package model;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.BiMap;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldLUDecomposition;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.FieldVector;

/**
 * An {@code Object} representing a linear program (LP).
 *
 * @author  Andreas Halle
 */
public class LP {
    public static final int UNDER = 0;
    public static final int RIGHT = 1;
    
    private FieldMatrix<BigFraction> B;
    private FieldMatrix<BigFraction> N;
    private FieldVector<BigFraction> b;
    private FieldVector<BigFraction> c;
    
    private FieldMatrix<BigFraction> B_;
    private FieldMatrix<BigFraction> N_;
    private FieldVector<BigFraction> b_; // x_b
    private FieldVector<BigFraction> c_; // z_n which is c negated!

    public BiMap<Integer, String> x;

    private int[] Bi;
    private int[] Ni;
    
    
    
    /**
     * Initializes a linear program.
     * <p>
     * n being the number of variables and m being the number of constraints,
     * this {@code constructor} does the following:
     * <ul>
     * <li>B is set to the identity matrix of dimension m.</li>
     *
     * <li>The indices of the non-basic and basic variables are set to
     *     0..n-1 and n..n+m-1, respectively.</li>
     *
     * <li>The slack variables are called w1..wm.</li>
     * </ul>
     * </p>
     *
     * @param N
     *        A {@code FieldMatrix} with the coefficients of the non-basic
     *        variables.
     * @param b
     *        A {@code FieldVector} with the upper bounds on the constraints
     *        in the original program.
     * @param c
     *        A {@code FieldVector} with the coefficients of the decision
     *        variables in the original program.
     * @param x
     *        A {@code HashMap} mapping the indices of the basic and non-basic
     *        variables to their names.
     */
    public LP(FieldMatrix<BigFraction> N, FieldVector<BigFraction> b,
              FieldVector<BigFraction> c, BiMap<Integer, String> x) {
        this(null, N, b, c, null, N.copy(), b.copy(),
                c.mapMultiply(BigFraction.MINUS_ONE).copy(), x,
                new int[N.getRowDimension()], new int[N.getColumnDimension()]);

        /* Create an identity matrix of BigFraction */
        int m = N.getRowDimension();
        BigFraction[][] Bd = new BigFraction[m][m];
        for (int i = 0; i < m; i++) {
            Arrays.fill(Bd[i], BigFraction.ZERO);
            Bd[i][i] = BigFraction.ONE;
        }
        FieldMatrix<BigFraction> B = new Array2DRowFieldMatrix<BigFraction>(Bd);
        
        this.B = B;
        this.B_ = B.copy();
        
        for (int i = 0; i < Ni.length; i++) Ni[i] = i;
        for (int i = 0; i < Bi.length; i++) {
            Bi[i] = i + Ni.length;
            x.put(Bi[i], "w" + (i+1));
        }
    }
    
    
    
    LP(FieldMatrix<BigFraction> B,  FieldMatrix<BigFraction> N,
       FieldVector<BigFraction> b,  FieldVector<BigFraction> c,
       FieldMatrix<BigFraction> B_, FieldMatrix<BigFraction> N_,
       FieldVector<BigFraction> b_, FieldVector<BigFraction> c_,
       BiMap<Integer, String> x, int[] Bi, int[] Ni) {
        this.B = B;
        this.N = N;
        this.b = b;
        this.c = c;

        this.B_ = B_;
        this.N_ = N_;
        this.b_ = b_;
        this.c_ = c_;

        this.x = x;

        this.Bi = Bi;
        this.Ni = Ni;
    }
    
    
    
    /**
     * Return a newly created {@code FieldMatrix} with a new block
     * {@code FieldMatrix} added either horizontally or vertically to the
     * original {@code FieldMatrix}.
     *
     * @param  B
     *         {@code FieldMatrix} to append to the parent {@code FieldMatrix}.
     * @param  modifier
     *         RIGHT or UNDER
     * @return
     *         The original {@code FieldMatrix} with a new block.
     */
    public static FieldMatrix<BigFraction> addBlock(
                        FieldMatrix<BigFraction> A, FieldMatrix<BigFraction> B,
                        int modifier) {
        int Am = A.getRowDimension();
        int An = A.getColumnDimension();
        int Bm = B.getRowDimension();
        int Bn = B.getColumnDimension();
        
        String e = String.format("Illegal operation: Cannot add a matrix block"
                               + " of size %d x %d to a matrix of size %d x %d."
                               , Am, An, Bm, Bn);

        if  ((modifier == RIGHT && Am != Bm || modifier == UNDER && An != Bn)) { 
            throw new IllegalArgumentException(e);
        }

        int newm = Am;
        int newn = An;

        int ci = 0;
        int cj = 0;

        switch (modifier) {
        case RIGHT:
            newn += Bn;
            cj = An;
            break;
        case UNDER:
        /* Fall through */
        default:
            newm += Bm;
            ci = Am;
        }
        
        BigFraction cdata[][] = new BigFraction[newm][newn];
        
        /* Copy A's data into cdata */
        for (int i = 0; i < Am; i++) {
            for (int j = 0; j < An; j++) {
                cdata[i][j] = A.getEntry(i, j);
            }
        }

        /* Add the new block of data */
        for (int i = 0; i < Bm; i++) {
            for (int j = 0; j < Bn; j++) {
                cdata[i+ci][j+cj] = B.getEntry(i, j);
            }
        }

        return new Array2DRowFieldMatrix<BigFraction>(cdata);
    }
    
    
    
    public static BigFraction getMinValue(FieldVector<BigFraction> bf) {
        BigFraction min = bf.getEntry(0);
        
        for (int i = 1; i < bf.getDimension(); i++) {
            BigFraction val = bf.getEntry(i);
            if (val.compareTo(min) < 0) {
                min = val;
            }
        }
        return min;
    }
    
    

    /**
     * Find an entering variable index according to the largest coefficient
     * rule.
     *
     * @param  dual
     *         If true, find an entering variable index for the dual dictionary.
     *         Otherwise, find one for the primal dictionary.
     * @return
     *         An entering variable index.
     */
    private int entering(boolean dual) {
        String e = "Incumbent basic solution is optimal.";
        String e2 = String.format("Incumbent basic solution is %s infeasible",
                                   dual ? "dually" : "primally");

        if (optimal(dual)) throw new RuntimeException(e);
        if (!feasible(dual)) throw new RuntimeException(e2);
        
        FieldVector<BigFraction> check = dual ? b_ : c_;
        
        BigFraction min = BigFraction.ZERO;
        int index = -1;
        
        for (int i = 0; i < check.getDimension(); i++) {
            BigFraction val = check.getEntry(i);
            if (val.compareTo(min) < 0) {
                min = val;
                index = i;
            }
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
        if (dual) return getMinValue(c_).compareTo(BigFraction.ZERO) >= 0.0;
        return getMinValue(b_).compareTo(BigFraction.ZERO) >= 0.0;
    }



    /**
     * Find a leaving variable index that is the most bounding on the given
     * entering variable index.
     * <p>
     * If there are multiple leaving variables that are 'the most bounding', the
     * variable listed first in the incumbent dictionary will be chosen.
     * </p>
     *
     * @param  entering
     *         an entering variable index.
     * @param  dual
     *         If true, find a leaving variable index for the dual dictionary.
     *         Otherwise, find one for the primal dictionary.
     * @return
     *         A leaving variable index.
     */
    private int leaving(int entering, boolean dual) {
        FieldVector<BigFraction> check;
        FieldVector<BigFraction> sd;
        
        FieldMatrix<BigFraction> bin = new FieldLUDecomposition<BigFraction>(B_)
                .getSolver().getInverse().multiply(N_);
        
        if (dual) {
            check = c_;
            FieldVector<BigFraction> unit = new ArrayFieldVector<BigFraction>(
                    bin.getRowDimension(), BigFraction.ZERO);
            unit.setEntry(entering, BigFraction.ONE);
            sd = bin.transpose().scalarMultiply(BigFraction.MINUS_ONE)
                    .operate(unit);
        }
        else {
            check = b_;
            FieldVector<BigFraction> unit = new ArrayFieldVector<BigFraction>(
                    bin.getColumnDimension(), BigFraction.ZERO);
            unit.setEntry(entering, BigFraction.ONE);
            sd = bin.operate(unit);
        }

        boolean unbounded = true;
        int index = -1;
        
        /* Check for unboundedness and find first non-zero element in check */
        for (int i = 0; i < sd.getDimension(); i++) {
            if (!check.getEntry(i).equals(BigFraction.ZERO) && index == -1) {
                index = i;
            }
            if (sd.getEntry(i).compareTo(BigFraction.ZERO) > 0) {
                unbounded = false;
            }
        }
        if (unbounded) throw new RuntimeException("Program is unbounded");
        
        BigFraction max;
        if (index == -1) { // All boundaries are 0. (All values of check are 0).
            max = BigFraction.ZERO; // Set temporary max to zero.
        } else {
            /* Set temporarily max value as ratio of the first divisible pair. */
            max = sd.getEntry(index).divide(check.getEntry(index));
        }
        
        for (int i = 0; i < sd.getDimension(); i++) {
            BigFraction num = sd.getEntry(i);
            BigFraction denom = check.getEntry(i);
            
            if (!denom.equals(BigFraction.ZERO)) {
                BigFraction val = num.divide(denom);
                if (val.compareTo(max) > 0) {
                    max = val;
                    index = i;
                }
            } else {
                if (num.compareTo(BigFraction.ZERO) > 0) return i;
            }
        }

        return index;
    }



    /**
     * Return the objective value of the incumbent dictionary.
     *
     * @return
     *         the objective value.
     */
    public BigFraction objVal() {
        BigFraction sum = BigFraction.ZERO;
        for (int i = 0; i < Bi.length; i++) {
            int j = Bi[i];
            if (j < c.getDimension()) {
                sum = sum.add(c.getEntry(j).multiply(b_.getEntry(i)));
            }
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
        if (dual) {
            return feasible(true)
                    && getMinValue(b_).compareTo(BigFraction.ZERO) >= 0;
        }
        return feasible(false)
                && getMinValue(c_).compareTo(BigFraction.ZERO) >= 0;
    }



    /**
     * Return a new linear program with a new objective function, keeping the
     * dictionary, making the program dually feasible.
     *
     * @return
     *         A linear program.
     */
    public LP phaseOneObj() {
        FieldVector<BigFraction> nc_ = new ArrayFieldVector<BigFraction>(
                c_.getDimension(), BigFraction.ONE);
        return new LP(B, N, b, c, B_, N_, b_, nc_, x, Bi, Ni);
    }



    /*
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
        FieldMatrix<BigFraction> bin = new FieldLUDecomposition<BigFraction>(B_)
                .getSolver().getInverse().multiply(N_);
        
        // Step 1: Check for optimality
        // Step 2: Select entering variable.
        // Naive method. Does not check for optimality. Assumes feasibility.
        // Entering variable is given.

        // Step 3: Compute primal step direction.
        FieldVector<BigFraction> ej = new ArrayFieldVector<BigFraction>(
                bin.getColumnDimension(), BigFraction.ZERO);
        ej.setEntry(entering, BigFraction.ONE);
        FieldVector<BigFraction> psd = bin.operate(ej);
        
        // Step 4: Compute primal step length.
        // Step 5: Select leaving variable.
        // Leaving variable is given.
        BigFraction t = b_.getEntry(leaving).divide(psd.getEntry(leaving));

        // Step 6: Compute dual step direction.
        FieldVector<BigFraction> ei = new ArrayFieldVector<BigFraction>(
                bin.getRowDimension(), BigFraction.ZERO);
        ei.setEntry(leaving, BigFraction.ONE);
        FieldVector<BigFraction> dsd = bin.transpose()
                .scalarMultiply(BigFraction.MINUS_ONE).operate(ei);
        
        // Step 7: Compute dual step length.
        BigFraction s = c_.getEntry(entering).divide(dsd.getEntry(entering));

        // Step 8: Update current primal and dual solutions.
        FieldVector<BigFraction> nb_ = b_.subtract(psd.mapMultiply(t));
        nb_.setEntry(leaving,  t);
        
        FieldVector<BigFraction> nc_ = c_.subtract(dsd.mapMultiply(s));
        nc_.setEntry(entering, s);
        
        // Step 9: Update basis.
        FieldVector<BigFraction> temp = B_.getColumnVector(leaving);
        FieldMatrix<BigFraction> nB_ = B_.copy();
        nB_.setColumn(leaving, N_.getColumn(entering));
        
        FieldMatrix<BigFraction> nN_ = N_.copy();
        nN_.setColumnVector(entering, temp);

        int[] nBi = Bi.clone();
        int[] nNi = Ni.clone();
        nBi[leaving] = Ni[entering];
        nNi[entering] = Bi[leaving];
        
        return new LP(B, N, b, c, nB_, nN_, nb_, nc_, x, nBi, nNi);
    }



    /*
     * Do one iteration of the simplex method. Calculate leaving variable
     * according to the largest coefficient rule.
     *
     * @param  dual
     *         If true, run the dual simplex method.
     *         Otherwise, run the primal simplex method.
     * @param  entering
     *         Index of variable to enter the basis.
     * @return
     *         A linear program after one iteration.
     */
    public LP pivot(boolean dual, int entering) {
        int leaving = leaving(entering, dual);
        if (dual) return pivot(leaving, entering);
        return pivot(entering, leaving);
    }



    /**
     * Do one iteration of the simplex method. Calculate leaving variable
     * according to the largest coefficient rule.
     *
     * @param  var
     *         Variable to enter/leave the basis.
     * @param  var2
     *         Variable to enter/leave the basis.
     * @return
     *         A linear program after one iteration.
     */
    public LP pivot(String var, String var2) {
        String format = "Unknown variable '%s'.";
        Integer varIdx = x.inverse().get(var);
        Integer var2Idx = x.inverse().get(var2);

        int idx = getDualIndex(var2);
        int idx2 = getDualIndex(var);
        if (idx == -1 && idx2 == -1) {
            idx = (varIdx != null) ? varIdx : -1;
            idx2 = (var2Idx != null) ? var2Idx : -1;
        }

        if (idx == -1)
                throw new IllegalArgumentException(String.format(format, var));
        if (idx2 == -1)
                throw new IllegalArgumentException(String.format(format, var2));

        System.out.println(idx);
        System.out.println(idx2);

        if (isNonBasic(idx) == isNonBasic(idx2))
            throw new IllegalArgumentException("The entering and leaving" +
                    " variables cannot both be basic variables or both be" +
                    " non-basic variables.");

        if (isNonBasic(idx)) return pivot(getPivotIndex(var), getPivotIndex(var2));
        return pivot(getPivotIndex(var2), getPivotIndex(var));

    }



    private boolean isNonBasic(int varIndex) {
        for (int i : Ni) if (i == varIndex) return true;
        return false;
    }



    private boolean isBasic(int varIndex) {
        for (int i : Bi) if (i == varIndex) return true;
        return false;
    }


    /**
     * Do one iteration of the simplex method. Calculate leaving variable
     * according to the largest coefficient rule.
     *
     * @param  dual
     *         If true, run the dual simplex method.
     *         Otherwise, run the primal simplex method.
     * @param  entering
     *         Variable to enter the basis.
     * @return
     *         A linear program after one iteration.
     */
    public LP pivot(boolean dual, String entering) {
        String format = "Illegal variable '%s'.";
        Integer idx = x.inverse().get(entering);
        if (idx == null)
            throw new IllegalArgumentException(String.format(format, entering));

        if (idx > getNoNonBasic()) {
            dual = true;
            idx -= getNoNonBasic();
        }

        return pivot(dual, idx);
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


    /**
     * Transition from phase 1 to phase 2 of the simplex method by reinstating
     * an updated objective function based on the original objective function
     * according to the incumbent dictionary.
     *
     * @return
     *         A linear program.
     */
    public LP reinstate() {
        FieldVector<BigFraction> nc_ = new ArrayFieldVector<BigFraction>(
                c_.getDimension(), BigFraction.ZERO);
        FieldMatrix<BigFraction> bin = new FieldLUDecomposition<BigFraction>(B_)
                .getSolver().getInverse().multiply(N_);
        
        for (int i = 0; i < Bi.length; i++) {
            int k = Bi[i];
            if (k < Ni.length) {
                for (int j = 0; j < Ni.length; j++) {
                    BigFraction bf = c.getEntry(k).multiply(
                            bin.getEntry(i, j));
                    nc_.setEntry(j, nc_.getEntry(j).add(bf));
                }
            }
        }
        
        for (int i = 0; i < Ni.length; i++) {
            int k = Ni[i];
            if (k < Ni.length) {
                nc_.setEntry(i, nc_.getEntry(i).subtract(c.getEntry(i)));
            }
        }
        
        return new LP(B, N, b, c, B_, N_, b_, nc_, x,Bi, Ni);
    }



    public BigFraction[] point() {
        BigFraction[] point = new BigFraction[Ni.length];
        Arrays.fill(point, BigFraction.ZERO);
        
        for (int i = 0; i < Bi.length; i++) {
            int j = Bi[i];
            if (j < Ni.length) point[j] = b_.getEntry(i);
        }
        return point;
    }



    public int getNoNonBasic() {
        return Ni.length;
    }



    public int getNoBasic() {
        return Bi.length;
    }
    
    
    
    public int[] getNonBasicIndices() {
        return Ni;
    }
    
    
    
    public int[] getBasicIndices() {
        return Bi;
    }



    public String[] getBasic() {
        String[] basic = new String[Bi.length];
        for (int i = 0; i < Bi.length; i++) {
            basic[i] = x.get(Bi[i]);
        }
        return basic;
    }
    
    
    
    public FieldVector<BigFraction> getBasis() {
        return b_;
    }
    
    
    
    // TODO: Hopefully find a method in apache commons math that supports
    //       augmenting matrices.
    public FieldMatrix<BigFraction> getConstraints() {
        return LP.addBlock(N, new Array2DRowFieldMatrix<BigFraction>(
                b.toArray()), RIGHT);
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


    private int getPivotIndex(String var) {
        Integer idx = x.inverse().get(var);
        if (idx == null) return getDualPivotIndex(var);

        int[] indices = (idx >= Ni.length) ? Bi : Ni;
        for (int i = 0; i < indices.length; i++) if(indices[i] == idx) return i;
        return -1;
    }



    private int getDualPivotIndex(String var) {
        int idx = Arrays.asList(getDualNonBasic()).indexOf(var);
        if (idx != -1) return idx;

        return Arrays.asList(getDualBasic()).indexOf(var);
    }



    private int getDualIndex(String var) {
        int idx = Arrays.asList(getDualNonBasic()).indexOf(var);
        if (idx != -1) return idx;

        return Arrays.asList(getDualBasic()).indexOf(var) + Ni.length;
    }



    /**
     * @return
     *         A {@code FieldMatrix} of numbers representing the dictionary of
     *         the incumbent Linear Program.
     */
    public FieldMatrix<BigFraction> dictionary() {
        BigFraction[][] data = new BigFraction[Bi.length+1][Ni.length+1];
        for (int i = 0; i < Ni.length; i++) {
            data[0][i+1] = c_.getEntry(i).negate();
        }
        for (int i = 0; i < Bi.length; i++) { 
            data[i+1][0] = b_.getEntry(i);
        }

        data[0][0] = objVal();

        FieldMatrix<BigFraction> values = new FieldLUDecomposition<BigFraction>(B_)
                .getSolver().getInverse().multiply(N_);
        
        for (int i = 0; i < Bi.length; i++) {
            for (int j = 0; j < Ni.length; j++) {
                data[i+1][j+1] = values.getEntry(i, j).negate();
            }
        }

        return new Array2DRowFieldMatrix<BigFraction>(data);
    }
    
    
    
    /**
     * Constraints being on the form:
     * <pre><blockquote>
     * c11x1 + c12x2 + ... + c1nxn <= b1
     * c21x1 + c22x2 + ... + c2nxn <= b2
     *                 ...
     * cm1x1 + cm2x2 + ... + cmnxn <= bm
     * </blockquote></pre>
     *
     * Cx <= b. This method returns the matrix C.
     * 
     * @return
     *         A {@code FieldMatrix} of numbers representing the coefficients
     *         for the variables in the constraints.
     */
    public FieldMatrix<BigFraction> getConsCoeffs() {
        return N_;
    }
    
    
    
    /**
     * Constraints being on the form:
     * <pre><blockquote>
     * c11x1 + c12x2 + ... + c1nxn <= b1
     * c21x1 + c22x2 + ... + c2nxn <= b2
     *                 ...
     * cm1x1 + cm2x2 + ... + cmnxn <= bm
     * </blockquote></pre>
     * 
     * Cx <= b. This method returns the vector b.
     * 
     * @return
     *         A {@code FieldVector} of numbers representing the values in the
     *         constraints. 
     */
    public FieldVector<BigFraction> getConsValues() {
        return b;
    }
    
    
    
    /**
     * @return
     *         A row {@code FieldVector} of numbers representing the
     *         coefficients in the objective function.
     */
    public FieldVector<BigFraction> getObjFunction() {
        return c;
    }
}
