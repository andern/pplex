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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import ccs.CCSLine;
import ccs.CCSPoint;
import ccs.CCSPolygon;
import ccs.CCSystem;

import model.LP;
import model.Matrix;

/**
 * The {@code VisLP} class contains functions for visualizing
 * a linear program ({@code LP} class) in two dimensions using
 * the {@code Coordinate} class.
 * 
 * @author  Andreas Halle
 * @version 0.1
 * @see     model.LP
 * @see     ccs.Coordinates
 */
class VisLP {
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
     *    with X component Xmin.
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
        lower.add(x_max);
        
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
     * @param  cons
     *         A constraints-matrix
     * @return
     *         A constraints matrix guaranteed to have lower bounds.
     */
     static Matrix checkForBounds(Matrix cons) {
        boolean lowerx = false;
        boolean lowery = false;
        
        /* Does lower bounds already exist? */
        for (int i = 0; i < cons.rows(); i++) {
            double x = cons.get(i, 0);
            double y = cons.get(i, 1);
            if (x < 0 && y == 0) {
                lowerx = true;
            } else if (x == 0 && y < 0) {
                lowery = true;
            }
        }
        
        Matrix ncons = new Matrix(cons);
        
        /* Add lower bounds if they do not exist */
        if (!lowerx) {
            Matrix c = new Matrix(new double[] {-1, 0, 0});
            ncons = ncons.addBlock(c, Matrix.UNDER);
        }
        if (!lowery) {
            Matrix c = new Matrix(new double[] {0, -1, 0});
            ncons = ncons.addBlock(c, Matrix.UNDER);
        }
        return ncons;
    }
    
    
    
    /* Draw an LP's constraints and color its feasible region. */
    static void drawLP(CCSystem cs, LP lp) {
        cs.clear();
        
        if (lp == null || lp.getNoBasic() != 2) {
            cs.setVisibleAxes(false);
            return;
        }
        cs.setVisibleAxes(true);
        
        CCSLine line;
        Matrix cons = lp.getConstraints();
        cons = checkForBounds(cons);
        
        /* Draw all constraints as lines */
        for (int i = 0; i < cons.rows(); i++) {
            line = new CCSLine(cons.get(i, 0), cons.get(i, 1),
                               cons.get(i, 2), Color.gray);
            cs.addLine(line);
        }
        
        /* Draw all feasible solutions as points */
        Point2D[] pconv = convex(getFeasibleIntersections(cons));
        for (Point2D p2d : pconv) {
            cs.addPoint(new CCSPoint(p2d.getX(), p2d.getY()));
        }
        cs.addPolygon(new CCSPolygon(pconv, Color.pink, true));
        
        /* Draw the current objective function */
        Matrix obj = lp.getObjFunction();
        line = new CCSLine(obj.get(0, 0), obj.get(1, 0),
                           lp.objVal(), Color.red);
        cs.addLine(line);
        
        /* Draw the current basic solution as a point. */
        double[] point = lp.point();
        cs.addPoint(new CCSPoint(point[0], point[1], Color.red, true));
    }
    
    
    
    /* 
     * Return all intersections that are satisfied
     * by ALL inequalities of the LP.
     */
    private static Point2D[] getFeasibleIntersections(Matrix cons) {
        Matrix N = cons.subMatrix(0, cons.rows()-1, 0, cons.cols()-2);
        Matrix b = cons.getCol(cons.cols()-1);
        
        LinkedList<Point2D> points = new LinkedList<Point2D>();
        
        /* Find all intersections */
        for (int i = 0; i < N.rows(); i++) {
            for (int j = 0; j < N.rows(); j++) {
                if (i == j) continue;
                
                Matrix line1 = N.getRow(i);
                Matrix line2 = N.getRow(j);
                
                double[] bval = new double[] {b.get(i, 0), b.get(j, 0)};
                Matrix bsys = new Matrix(bval).transpose();
                Matrix sys = line1.addBlock(line2, Matrix.UNDER);
                
                try {
                    Matrix point = sys.inverse().product(bsys);
                    double x = point.get(0, 0);
                    double y = point.get(1, 0);
                    Point2D p2d = new Point2D.Double(x, y);
                
                    points.add(p2d);
                } catch (IllegalArgumentException e) {
                    /* 
                     * Two lines that don't intersect forms an invertible
                     * matrix. Skip these points.
                     */
                }
            }
        }
        
        
        
        Iterator<Point2D> iter = points.iterator();
        /* Remove intersections that are not satisfied by ALL inequalities. */
        while (iter.hasNext()) {
            Point2D p2d = iter.next();
            
            double x = p2d.getX();
            double y = p2d.getY();
            
            for (int i = 0; i < N.rows(); i++) {
                float val = (float) (N.get(i, 0)*x + N.get(i, 1)*y);
                if (val > b.get(i, 0)) {
                    iter.remove();
                    break;
                }
            }
        }
        return points.toArray(new Point2D[0]);
    }
}
