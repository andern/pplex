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
package controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldLUDecomposition;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.FieldVector;

import cartesian.coordinate.CCLine;
import cartesian.coordinate.CCPoint;
import cartesian.coordinate.CCPolygon;
import cartesian.coordinate.CCSystem;

import model.LP;

/**
 * The {@code VisLP} class contains functions for visualizing
 * a linear program ({@code LP} class) in two dimensions using
 * the {@code Coordinate} class.
 * 
 * @author  Andreas Halle
 * @see     model.LP
 * @see     ccs.Coordinates
 */
class VisLP {
    private static ArrayList<Point2D> unb;
    protected static boolean readScope = true;
    protected static boolean feasScope = true;
    
    
    
    /*
     * Input: Unordered list of points that can form a
     *        convex polygon, but in the given order
     *        does not necessarily form a convex
     *        polygon if edges are drawn between the
     *        points in the given order.
     * 
     * Output: An ordered list of points that when edges
     *         are drawn in this order is guaranteed to
     *         form a convex polygon.
     * 
     * Assuming your points are all on the convex hull
     * of your polygon, you can use the following:
     * 
     * 1. Pick the two extreme points with the min and
     *    max X value, (call them Xmin and Xmax) and
     *    draw the line between them. In the case where
     *    you have multiple points with the same X value
     *    at the extremes, pick Xmin with the minimum Y
     *    value and Xmax with the maximum Y value.
     *    
     * 2. Split the list of points into two sub lists
     *    where all of the points below the line
     *    connecting Xmin and Xmax are in one list and
     *    all those above that line are in another.
     *    Include Xmin in the first list and Xmax in
     *    the second.
     *    
     * 3. Sort the first list in ascending order of X
     *    value. If you have multiple points with the
     *    same X value, sort them in ascending Y value.
     *    This should only happen for points with the
     *    same X component as Xmax since the polygon is
     *    convex.
     *    
     * 4. Sort the second list in descending order of X
     *    value. Again, sort in descending Y value in
     *    the event of multiple points with the same X
     *    value (which should only happen for points
     *    with X component Xmin).
     *    
     * 5. Append the two lists together (it doesn't
     *    matter which is first).
     * 
     * From:
     * http://stackoverflow.com/questions/7369710/sorting-polygons-points
     */
    /**
     * @param  points
     *         Unordered list of points that can form a
     *         convex polygon, but in the given order
     *         does not necessarily form a convex
     *         polygon if edges are drawn between the
     *         points in the given order.
     * @return 
     *         An ordered list of points that when edges
     *         are drawn in this order is guaranteed to
     *         form a convex polygon.
     */
    static Point2D[] convex(Point2D[] points) {
        /* 
         * Sort the points first on x-value then
         * on y-value, both in ascending order.
         */
        Arrays.sort(points, new Comparator<Point2D>() {
            @Override public int compare(Point2D o1, Point2D o2) {
                double s = o1.getX() - o2.getX();
                if (s > 0) return 1;
                if (s < 0) return -1;
                
                s = o1.getY() - o2.getY();
                if (s > 0) return 1;
                if (s < 0) return -1;
                
                return 0;
            }
        });
        
        Point2D x_min = points[0];
        Point2D x_max = points[points.length-1];
        
        ArrayList<Point2D> upper = new ArrayList<Point2D>();
        ArrayList<Point2D> lower = new ArrayList<Point2D>();
        
        upper.add(x_min); 
        
        /* Find the slope of the line L connecting x_min and x_max */
        double mx = x_max.getX() - x_min.getX();
        double my = x_max.getY() - x_min.getY();
        double m = my / mx;
        
        /* Intersection of y-axis */
        double b = x_max.getY() - (m * x_max.getX());
        
        /* Add points above/below L to upper/lower, respectively */
        for (int i = 1; i < points.length-1; i++) {
            Point2D p2d = points[i];
            double y = p2d.getX()*m + b;
            
            if (p2d.getY() >= y) upper.add(p2d);
            else lower.add(p2d);
        }
        
        /* Sort the lower list in descending order */
        lower.add(x_max);
        Collections.reverse(lower);
        
        upper.addAll(lower);
        return upper.toArray(new Point2D[0]);
    }
    
    
    
    /**
     * Dictionary form assumes all x-values >= 0. For a geometric
     * representation we need these constraints explicitly stated
     * in order to draw the feasible region.
     * 
     * This method adds the constraints x >= 0 (-x <= 0) 
     * and y >= 0 (-y <= 0) unless more bounding constraints
     * on the x and y-values already exists.
     * 
     * It also always adds a line with a negative slope with
     * a <<high enough>> positive x- and y-intercept needed
     * to color unbounded feasible regions.
     * 
     * @param  cons
     *         A constraints-matrix
     * @return
     *         A constraints matrix guaranteed to have lower bounds.
     */
    static FieldMatrix<BigFraction> checkForBounds(
            FieldMatrix<BigFraction> cons) {
        boolean lowerx = false;
        boolean lowery = false;

        BigFraction valsum = BigFraction.ZERO;

        /* Does lower bounds already exist? */
        for (int i = 0; i < cons.getRowDimension(); i++) {
            BigFraction x = cons.getEntry(i, 0);
            BigFraction y = cons.getEntry(i, 1);
            if (x.compareTo(BigFraction.ZERO) < 0
                    && y.equals(BigFraction.ZERO)) {
                lowerx = true;
            } else if (x.equals(BigFraction.ZERO)
                    && y.compareTo(BigFraction.ZERO) < 0) {
                lowery = true;
            }

            valsum = valsum.add(cons.getEntry(i, 2).abs());
        }

        FieldMatrix<BigFraction> ncons = cons.copy();

        BigFraction[] cxdata = new BigFraction[] {BigFraction.MINUS_ONE,
                BigFraction.ZERO,
                BigFraction.ZERO};
        BigFraction[] cydata = new BigFraction[] {BigFraction.ZERO,
                BigFraction.MINUS_ONE,
                BigFraction.ZERO};
        /* Add lower bounds if they do not exist */
        if (!lowerx) {
            FieldMatrix<BigFraction> c =
                    new Array2DRowFieldMatrix<BigFraction>(cxdata).transpose();
            ncons = LP.addBlock(ncons, c, LP.UNDER);
        }
        if (!lowery) {
            FieldMatrix<BigFraction> c =
                    new Array2DRowFieldMatrix<BigFraction>(cydata).transpose();
            ncons = LP.addBlock(ncons, c, LP.UNDER);
        }

        valsum = valsum.add(BigFraction.TWO).multiply(valsum);
        BigFraction[] uc = new BigFraction[] {BigFraction.ONE,
                BigFraction.ONE,
                valsum};

        FieldMatrix<BigFraction> c = new Array2DRowFieldMatrix<BigFraction>(uc)
                .transpose();
        ncons = LP.addBlock(ncons, c, LP.UNDER);

        return ncons;
    }
    
    
    /**
     * Draw the linear constraints of an {@code LP} and color
     * it's feasible region in a given {@code CCSystem}.
     * 
     * @param cs
     *        a {@code CCSystem}.
     * @param lp
     *        a {@code LP}.
     */
    static void drawLP(CCSystem cs, LP lp) {
        cs.clear();
        
        /* Don't draw the LP if it is not in two variables */
        if (lp == null || lp.getNoNonBasic() != 2) {
            cs.setAxesVisible(false);
            cs.setGridVisible(false);
            return;
        }
        cs.setAxesVisible(true);
        cs.setGridVisible(true);
        cs.setAxisXPaint(Color.black);
        cs.setAxisYPaint(Color.black);
        
        FieldMatrix<BigFraction> cons = lp.getConstraints();
        cons = checkForBounds(cons);
        
        FieldVector<BigFraction> b = lp.getBasis();
        int[] Bi = lp.getBasicIndices();
        
        boolean[] degLines = new boolean[lp.getNoBasic()];
        
        /* Find degenerate lines and color axes if they are degenerate */
        for (int i = 0; i < Bi.length; i++) {
            if (b.getEntry(i).equals(BigFraction.ZERO)) {
                if (Bi[i] >= lp.getNoNonBasic()) {
                    degLines[Bi[i]-lp.getNoNonBasic()] = true;
                }
                else if (Bi[i] == 0) cs.setAxisXPaint(Color.green);
                else if (Bi[i] == 1) cs.setAxisYPaint(Color.green);
            }
        }
        
        CCLine line;
        /* Draw all constraints as lines, except hidden bounded constraint */
        for (int i = 0; i < lp.getNoBasic(); i++) {
            Color color = Color.gray;
            
            /* Color degenerate lines differently */
            if (i < lp.getNoBasic() && degLines[i]) color = Color.green;
            
            line = new CCLine(cons.getEntry(i, 0).doubleValue(),
                              cons.getEntry(i, 1).doubleValue(),
                              cons.getEntry(i, 2).doubleValue(), color);
            cs.add(line);
        }
        
        Point2D[] fpoints = getFeasibleIntersections(cons);
        
        /* 
         * Move the center of the coordinate system
         * to the center of the feasible region.
         */
        if (readScope) {
            scopeArea(cs, fpoints, true);
            readScope = false;
        }
        if (feasScope && lp.feasible(false)) {
            scopeArea(cs, fpoints, false);
            feasScope = false;
        }
        
        
        /* If there is no feasible region there is no need to try to color it */
        if (fpoints.length == 0) return;
        
        /* Draw all feasible solutions as points */
        Point2D[] pconv = convex(fpoints);
        for (Point2D p2d : pconv) {
            CCPoint ccp = new CCPoint(p2d.getX(), p2d.getY());
            if (!unb.contains(p2d)) cs.add(ccp);
        }
        
        /* Color the region depending on whether it is unbounded or not. */
        if (unb.size() == 0) {
            cs.add(new CCPolygon(pconv, null, Color.pink, null));
        } else if (unb.size() == 1) {
            GradientPaint gp = new GradientPaint(pconv[0], Color.pink,
                    unb.get(0), cs.getBackground());
            cs.add(new CCPolygon(pconv, null, gp, null));
        } else {
            Point2D p1 = unb.get(0);
            Point2D p2 = unb.get(1);
            double xavg = (p1.getX() + p2.getX()) / 2.0;
            double yavg = (p1.getY() + p2.getY()) / 2.0;
            /* 
             * Move the end point of the gradient further away from the
             * polygon edge to make the end of the gradient look less sudden.
             */
            xavg *= 0.9;
            yavg *= 0.9;
            
            Point2D pavg = new Point2D.Double(xavg, yavg);
            
            /* Fade into the background color */
            GradientPaint gp = new GradientPaint(pconv[0], Color.pink,
                    pavg, cs.getBackground());

            cs.add(new CCPolygon(pconv, null, gp, null));
        }
        
        /* Draw the current objective function */
        FieldVector<BigFraction> obj = lp.getObjFunction();
        line = new CCLine(obj.getEntry(0).doubleValue(),
                           obj.getEntry(1).doubleValue(),
                           lp.objVal().doubleValue(), Color.red);
        cs.add(line);
        
        /* Draw the current basic solution as a point. */
        BigFraction[] point = lp.point();
        cs.add(new CCPoint(point[0].doubleValue(), point[1].doubleValue(),
                Color.red, new BasicStroke(1f)));
    }
    
    
    
    /* 
     * Return all intersections that are satisfied
     * by ALL inequalities of the LP.
     */
    private static
    Point2D[]
    getFeasibleIntersections(FieldMatrix<BigFraction> cons) {
        FieldMatrix<BigFraction> N =
                cons.getSubMatrix(0, cons.getRowDimension()-1, 0,
                                  cons.getColumnDimension()-2);
        FieldVector<BigFraction> b =
                cons.getColumnVector(cons.getColumnDimension()-1);
        
        HashSet<Point2D> points = new HashSet<Point2D>();
        unb = new ArrayList<Point2D>();
        
        /* Find all intersections */
        for (int i = 0; i < N.getRowDimension(); i++) {
            for (int j = 0; j < N.getRowDimension(); j++) {
                if (i == j) continue;
                
                FieldMatrix<BigFraction> line1 = N.getRowMatrix(i);
                FieldMatrix<BigFraction> line2 = N.getRowMatrix(j);
                
                BigFraction[] bval = new BigFraction[] {b.getEntry(i),
                                                        b.getEntry(j)};
                FieldVector<BigFraction> bsys =
                        new ArrayFieldVector<BigFraction>(bval);
                FieldMatrix<BigFraction> sys =
                        LP.addBlock(line1, line2, LP.UNDER);
                
                try {
                    FieldVector<BigFraction> point =
                            new FieldLUDecomposition<BigFraction>(sys)
                                    .getSolver().getInverse().operate(bsys);
                    double x = point.getEntry(0).doubleValue();
                    double y = point.getEntry(1).doubleValue();
                    Point2D p2d = new Point2D.Double(x, y);
                
                    /* Only add feasible points */
                    if (feasible(p2d, N, b)) {
                        if (i >= N.getRowDimension()-1) unb.add(p2d);
                        points.add(p2d);
                    }
                } catch (IllegalArgumentException e) {
                    /* 
                     * Two lines that don't intersect forms an invertible
                     * matrix. Skip these points.
                     */
                }
            }
        }
        return points.toArray(new Point2D[0]);
    }
    
    
    /*
     * Find the lowest and highest x and y values among all the
     * given points. Set the are to be displayed in the cartesian
     * coordinate system to these values with a 10% padding.
     */
    private static void scopeArea(CCSystem cs,Point2D[] points, boolean origo) {
        // No feasible points. Don't do anything.
        if (points.length == 0) return;
        if (points.length == 1) origo = true;
        
        double loX = origo ? 0 : Double.MAX_VALUE;
        double hiX = Double.MIN_VALUE;
        double loY = origo? 0 : Double.MAX_VALUE;
        double hiY = Double.MIN_VALUE;
        
        for (Point2D p : points) {
            double x = p.getX();
            double y = p.getY();
            if (x < loX) loX = x;
            if (x > hiX) hiX = x;
            if (y < loY) loY = y;
            if (y > hiY) hiY = y;
        }
        
        if (loX == hiX) hiX = loX + 0.001;
        if (loY == hiY) hiY = loY + 0.001;
        double distX = hiX - loX;
        double distY = hiY - loY;
        cs.move(loX-distX*0.1, hiX+distX*0.1, loY-distY*0.1, hiY+distY*0.1);
    }
    
    
    
    /* Return whether a point is feasible according to the given constraints. */
    private static boolean feasible(Point2D p2d, FieldMatrix<BigFraction> N,
                                    FieldVector<BigFraction> b) {
        double x = p2d.getX();
        double y = p2d.getY();
        
        for (int j = 0; j < N.getRowDimension(); j++) {
            float nx = N.getEntry(j, 0).floatValue();
            float ny = N.getEntry(j, 1).floatValue();
            float val = (float) (nx*x + ny*y);
            if (val > b.getEntry(j).floatValue()) return false;
        }
        
        return true;
    }
}
