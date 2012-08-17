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

/**
 * {@code CCSLine} represents a straight line in a
 * Cartesian coordinate system.
 * <p>
 * Lines are stored as a linear equation on the format:
 *     ax + by = c
 *
 * @author Andreas Halle
 */
public class CCSLine {
    protected double a;
    protected double b;
    protected double c;
    protected Color color;
    
    
    
    /**
     * Create a new line from a linear equation:
     *     ax + by = c 
     * 
     * @param a
     *        Coefficient of the x-variable.
     * @param b
     *        Coefficient of the y-variable.
     * @param c
     *        A constant.
     * @param color
     *        The line will have this color when drawn.
     */
    public CCSLine(double a, double b, double c, Color color) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.color = color;
    }



    /**
     * Create a new line from a linear equation:
     *     ax + by = c 
     * Will hav
     * 
     * @param a
     *        Coefficient of the x-variable.
     * @param b
     *        Coefficient of the y-variable.
     * @param c
     *        A constant.
     */
    public CCSLine(double a, double b, double c) {
        this(a, b, c, Color.black);
    }
}
