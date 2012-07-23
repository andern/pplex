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
package ccs;

import java.awt.Color;

import javax.swing.JPanel;

/**
 * {@code CCSPoint} represent a point in a
 * Cartesian coordinate system.
 * 
 * @author Andreas Halle
 */
public class CCSPoint extends JPanel {
    private static final long serialVersionUID = 1L;

    protected double x;
    protected double y;
    protected Color color;
    protected boolean fill;
    
    
    
    /**
     * Create a new point at the coordinate (x, y).
     * 
     * @param x
     *        x coordinate
     * @param y
     *        y coordinate
     * @param color
     *        Draw the point in this color
     * @param fill
     *        If true, fill the point. Otherwise outline it.
     */
    public CCSPoint(double x, double y, Color color, boolean fill) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.fill = fill;
    }
    
    
    
    /**
     * Create a new point at the coordinate (x, y).
     * <p>
     * The point is filled in black by default.
     * 
     * @param x
     *        x coordinate
     * @param y
     *        y coordinate
     */
    public CCSPoint(double x, double y) {
        this(x, y, Color.black, true);
    }
}