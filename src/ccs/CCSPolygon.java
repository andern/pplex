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
package ccs;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;

/**
 * {@code CCSPolygon} represent a polygon formed by
 * drawing straight lines between a number of
 * coordinates in a Cartesian coordinate system.
 *
 * @author Andreas Halle
 * @see    ccs.CCSystem
 */
public class CCSPolygon {
    protected double[] xpoints;
    protected double[] ypoints;
    protected int npoints;
    protected Paint paint;
    protected boolean fill;
    
    
    
    /**
     * Create a polygon formed by a number of points.
     * 
     * @param xpoints
     *        x-value for each points
     * @param ypoints
     *        y-value for each point
     * @param npoints
     *        the number of points
     * @param paint
     *        Draw the polygon with this paint
     * @param fill
     *        If true, fill the polygon. Otherwise outline it.
     */
    public CCSPolygon(double[] xpoints, double[] ypoints, int npoints,
                                                Paint paint, boolean fill) {
        this.xpoints = xpoints;
        this.ypoints = ypoints;
        this.npoints = npoints;
        this.paint = paint;
        this.fill = fill;
    }
    
    
    
    /**
     * Create a polygon formed by a number of points.
     * <p>
     * The polygon is filled in pink by default.
     * 
     * @param xpoints
     *        x-value for each points
     * @param ypoints
     *        y-value for each point
     * @param npoints
     *        the number of points
     */
    public CCSPolygon(double[] xpoints, double[] ypoints, int npoints) {
        this(xpoints, ypoints, npoints, Color.pink, true);
    }
    
    
    
    /**
     * Create a polygon formed by the given points.
     * 
     * @param points
     *        Array of points
     * @param paint
     *        Draw the polygon with this paint
     * @param fill
     *        If true, fill the polygon. Otherwise outline it.
     */
    public CCSPolygon(Point2D[] points, Paint paint, boolean fill) {
        this(new double[points.length], new double[points.length],
                                      points.length, paint, fill);
        
        for (int i = 0; i < points.length; i++) {
            xpoints[i] = points[i].getX();
            ypoints[i] = points[i].getY();
        }
    }
}