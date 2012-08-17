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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

import javax.swing.JPanel;

/*
 * In this class there are two coordinate systems:
 * 
 * 1. A two-dimensional coordinate system for Java2D
 *    where x lies in the interval [0, window width]
 *    and y lies in the interval [0, window height]
 *    where the units of both x and y are pixels.
 *    
 * 2. An emulated coordinate system where x and y can
 *    lie in any range definable by double precision
 *    numbers.
 * 
 * Throughout this class, Point is used to represent
 * a point in system 1 while Point2D is used to
 * represent a point in system 2.
 * 
 * The method translate(Point) and translate(Point2D)
 * is used to translate between the two systems.
 */
public class CCSystem extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private boolean drawXAxis;
    private boolean drawYAxis;
    private boolean drawXUnits;
    private boolean drawYUnits;
    
    private ArrayList<Shape> shapes;
    private ArrayList<CCSLine> lines;
    private ArrayList<CCSPoint> points;
    private ArrayList<CCSPolygon> polygons;
    
    /* Define the range of the visible xy-plane */
    private double loX;
    private double hiX;
    private double loY;
    private double hiY;
    
    /* The length of each range */
    private double distX;
    private double distY;
    
    private Point2D.Double origo;
    
    /* Use this precision for BigDecimal */
    private final MathContext MC = MathContext.DECIMAL128;



    /**
     * Initialize a new empty coordinate system.
     */
    public CCSystem() {
        /* Setting some default values */
        drawXAxis = false;
        drawYAxis = false;
        drawXUnits = true;
        drawYUnits = true;
        loX = -10;
        hiX = 10;
        loY = -10;
        hiY = 10;
        
        shapes = new ArrayList<Shape>();
        lines = new ArrayList<CCSLine>();
        points = new ArrayList<CCSPoint>();
        polygons = new ArrayList<CCSPolygon>();
        
        updatePosition();
        
        /* Add some default listeners */
        this.addMouseListener(new mouseListener());
        this.addMouseMotionListener(new mouseListener());
        this.addMouseWheelListener(new mouseScrollListener());
    }
    
    
    /**
     * Add a line to the coordinate system.
     * 
     * @param line
     *        a line
     */
    public void addLine(CCSLine line) {
        lines.add(line);
    }
    
    
    
    /**
     * Add a point to the coordinate system.
     * 
     * @param p
     *        a point
     */
    public void addPoint(CCSPoint p) {
        points.add(p);
    }
    
    
    
    /**
     * Add a polygon to the coordinate system.
     * 
     * @param poly
     *        a polygon
     */
    public void addPolygon(CCSPolygon poly) {
        polygons.add(poly);
    }
    
    
    
    /**
     * @param visible
     *        If true, draw axes.
     */
    public void setVisibleAxes(boolean visible) {
        drawXAxis = visible;
        drawYAxis = visible;
    }



    /*
     * Convert points in coordinate system 1 to system 2
     * as described in the class documentation.
     */
    private Point2D.Double translate(Point p) {
        double xscale = (hiX - loX) / this.getWidth();
        double yscale = (hiY - loY) / this.getHeight();

        double newx = p.x * xscale + loX;
        double newy = p.y * yscale + loY;

        return new Point2D.Double(newx, newy);
    }



    /*
     * Convert points in coordinate system 2 to system 1
     * as described in the class documentation.
     */
    private Point translate(Point2D p2d) {
        double xscale = (hiX - loX) / getWidth();
        double yscale = (hiY - loY) / getHeight();
        
        int newx = (int) Math.round((p2d.getX() - loX) / xscale);
        int newy = (int) Math.round((p2d.getY() - loY) / yscale);
        
        /* Convert so that increasing y takes you north instead of south. */
        newy = getHeight() - newy;
        
        return new Point(newx, newy);
    }



    /*
     * Draw the axes and unit lines in the best looking
     * way possible for the given x- and y-ranges.
     */
    public void drawAxes(Graphics2D g2d) {
        /* Find position for the unit line values. */
        boolean ywest = (loX >= 0) ? false : true;
        boolean xsouth = (loY >= 0) ? false : true;
        
        Point o = translate(origo);
        
        g2d.setColor(Color.black);
        if (drawXAxis) {
            Point xaxis_start = new Point(0, o.y);
            Point xaxis_end = new Point(getWidth(), o.y);
            g2d.drawLine(xaxis_start.x, xaxis_start.y,
                    xaxis_end.x, xaxis_end.y);
        }
        
        
        
        if (drawYAxis) {
            Point yaxis_start = new Point(o.x, 0);
            Point yaxis_end = new Point(o.x, getHeight());
            g2d.drawLine(yaxis_start.x, yaxis_start.y,
                    yaxis_end.x, yaxis_end.y);
        }
        
        
        
        if (drawXAxis && drawXUnits) {
            /* Approximate number of pixels needed between each "unit" */
            int pbux = 65;
            
            /* Total number of units on the axis */
            int unitsX = getWidth() / pbux;
            
            /* Exact value between each unit */
            double udistX = distX / unitsX;
            
            /* 
             * The exact value rounded to a value that can be written with
             * very few decimals.
             * 
             * TODO: Find a better mathematical term
             *       for the numbers described above.
             */
            BigDecimal vbuX = findScale(udistX);
            
            int pix = o.x;
            /* Find coefficient for vbuX for the first visible unit */
            int q = (int) (loX / vbuX.doubleValue());
            /* Draw the units on the x-axis */
            while (pix <= getWidth()) {
                /*
                 * Simply using double here introduces rounding errors
                 * when rval reaches the order of 10^23 or higher or
                 * 10^-23 or lower. Therefore using BigDecimal.
                 */
                BigDecimal qbd = new BigDecimal(q++, MC);
                BigDecimal val = vbuX.multiply(qbd, MC);
                
                double rval = val.doubleValue();
                
                Point2D p2d = new Point2D.Double(rval, origo.y);
                Point p = translate(p2d);

                /*
                 * Cannot use BigDecimal's toString-method since it does not
                 * use scientific notation on positive numbers.
                 */
                String strval = Double.toString(rval);
                
                int offset = xsouth ? 20 : -10;
                
                int pixelPerChar = 7;
                int strValPixels = pixelPerChar * strval.length();
                
                if (val.floatValue() != 0.0)
                    g2d.drawString(strval, p.x-strValPixels/2, p.y+offset);
                
                /* Length of unit line in pixels. */
                int size = 4;
                g2d.drawLine(p.x, p.y-size, p.x, p.y+size);
                
                pix = p.x;
            }
        }
        
        
        
        if (drawYAxis && drawYUnits) {
            int pbuy = 65;
            int unitsY = getHeight() / pbuy;
            double udistY = distY / unitsY;
            BigDecimal vbuY = findScale(udistY);

            int pix = o.y;
            int q = (int) (loY / vbuY.doubleValue());

            /* Draw the units on the x-axis */
            while (pix >= 0) {
                BigDecimal qbd = new BigDecimal(q++, MC);
                BigDecimal val = vbuY.multiply(qbd, MC);

                double rval = val.doubleValue();

                Point2D p2d = new Point2D.Double(origo.x, rval);
                Point p = translate(p2d);

                String strval = Double.toString(rval);

                int pixelPerChar = 8;
                int strValPixels = pixelPerChar * strval.length();

                int offset = ywest ? -strValPixels : 5;

                if (val.floatValue() != 0.0)
                    g2d.drawString(strval, p.x+offset, p.y+5);

                int size = 4;
                g2d.drawLine(p.x-size, p.y, p.x+size, p.y);

                pix = p.y;
            }
        }
    }
    
    
    
    /* Draw a single line */
    private void drawLine(Graphics2D g2d, CCSLine l) {
        Point2D p2d1;
        Point2D p2d2;
        if (l.b == 0.0) {
            int mul = (l.a < 0) ? -1 : 1;
            p2d1 = new Point2D.Double(l.c*mul, loY);
            p2d2 = new Point2D.Double(l.c*mul, hiY);
        } else if (l.a == 0.0) {
            int mul = (l.a  < 0) ? -1 : 1;
            p2d1 = new Point2D.Double(loX, l.c*mul);
            p2d2 = new Point2D.Double(hiX, l.c*mul);
        } else {
            p2d1 = new Point2D.Double(loX, (l.c-l.a*loX)/l.b);
            p2d2 = new Point2D.Double(hiX, (l.c-l.a*hiX)/l.b);
        }
        Point p1 = translate(p2d1);
        Point p2 = translate(p2d2);
        
        g2d.setColor(l.color);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
    
    
    
    /* Draw a single point */
    private void drawPoint(Graphics2D g2d, CCSPoint ccsp) {
        Point2D p2d = new Point2D.Double(ccsp.x, ccsp.y);
        Point p = translate(p2d);
        
        g2d.setColor(ccsp.color);
        Ellipse2D r2d = new Ellipse2D.Double(p.x-3, p.y-3, 6, 6);
        if (ccsp.fill) g2d.fill(r2d);
        else g2d.draw(r2d);
    }



    private BigDecimal findScale(double udist) {
        int x = (int)Math.floor(Math.log10(udist));
        /* scale = 10 ^ x */
        BigDecimal scale = new BigDecimal(10, MC).pow((int)x, MC);
        
        double quot = udist / scale.doubleValue();
        if (quot > 5.0) return scale.multiply(new BigDecimal(10), MC);
        if (quot > 2.0) return scale.multiply(new BigDecimal(5), MC);
        if (quot > 1.0) return scale.multiply(new BigDecimal(2), MC);
        else return scale;
    }



    private void move(double moveX, double moveY) {
        loX += moveX;
        hiX += moveX;
        loY += moveY;
        hiY += moveY;
        updatePosition();
    }
    
    
    
    /* 
     * Draw a polygon by drawing lines between its points
     * in the order they are given.
     */
    private void drawPolygon(Graphics2D g2d, CCSPolygon ccspoly) {
        int[] xpoints = new int[ccspoly.npoints];
        int[] ypoints = new int[ccspoly.npoints];
        
        for (int i = 0; i < ccspoly.npoints; i++) {
            double x = ccspoly.xpoints[i];
            double y = ccspoly.ypoints[i];
            Point2D p2d = new Point2D.Double(x, y);
            
            Point p = translate(p2d);
            xpoints[i] = p.x;
            ypoints[i] = p.y;
        }
        Polygon poly = new Polygon(xpoints, ypoints, ccspoly.npoints);
        
        /* 
         * Support the use of GradientPaint. We need to
         * translate the points of the gradient paint.
         */
        if (ccspoly.paint instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint) ccspoly.paint;
            
            Point2D gp1 = gp.getPoint1();
            Point2D gp2 = gp.getPoint2();
            
            Point p1 = translate(gp1);
            Point p2 = translate(gp2);
            
            Color c1 = gp.getColor1();
            Color c2 = gp.getColor2();
            
            g2d.setPaint(new GradientPaint(p1, c1, p2, c2));
        } else {
            g2d.setPaint(ccspoly.paint);
        }
        
        if (ccspoly.fill) g2d.fill(poly);
        else g2d.draw(poly);
    }
    


    /* 
     * Update distance of x/y ranges and recalculate
     * the position of origo.
     */
    private void updatePosition() {
        distX = hiX - loX;
        distY = hiY - loY;
        
        /* Find origo */
        double ox = 0;
        double oy = 0;
        
        if (loX >= 0) ox = loX;
        else if (hiX <= 0) ox = hiX;
        
        if (loY >= 0) oy = loY;
        else if (hiY <= 0) oy = hiY;
        
        origo = new Point2D.Double(ox, oy);
    }
    
    
    
    private void zoom(double zoomX, double zoomY) {
        loX -= zoomX;
        hiX += zoomX;
        loY -= zoomY;
        hiY += zoomY;
        updatePosition();
    }
    
    
    
    public void clear() {
        shapes.clear();
        lines.clear();
        points.clear();
        polygons.clear();
        updateUI();
    }
    
    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // TODO: Might be a bit excessive. Just trying at the moment.
        RenderingHints rh = new RenderingHints(null);
        rh.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
               RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        rh.put(RenderingHints.KEY_ANTIALIASING,
               RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_COLOR_RENDERING,
               RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        rh.put(RenderingHints.KEY_DITHERING,
               RenderingHints.VALUE_DITHER_ENABLE);
        rh.put(RenderingHints.KEY_FRACTIONALMETRICS,
               RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        rh.put(RenderingHints.KEY_INTERPOLATION,
               RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        rh.put(RenderingHints.KEY_RENDERING,
               RenderingHints.VALUE_RENDER_QUALITY);
        rh.put(RenderingHints.KEY_STROKE_CONTROL,
               RenderingHints.VALUE_STROKE_NORMALIZE);
        rh.put(RenderingHints.KEY_TEXT_ANTIALIASING,
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        
        g2d.addRenderingHints(rh);
        
        /* Polygons should be drawn before the axes. */
        for (CCSPolygon poly : polygons) drawPolygon(g2d, poly);
        drawAxes(g2d);
        for (CCSLine line : lines) drawLine(g2d, line);
        for (CCSPoint p : points) drawPoint(g2d, p);
        
//        drawAxes();
//        
//        if (lp != null) {
//            if (lp.getNoBasic() != 2) {
//                String s  = "Current LP is not representable";
//                String s2 = "in two dimensions";
//                g2d.drawString(s, getWidth()/2-s.length()*2-20, getHeight()/2);
//                g2d.drawString(s2, getWidth()/2-s2.length()*3, getHeight()/2+20);
//            } else {
//                checkLPForBounds();
//                
//                drawLP(g2d, lp);
//                drawAxes(g2d);
//                
//                g2d.setColor(Color.RED);
//                drawObjPoint(g2d, lp);
//                
//                drawConstraint(g2d, lp.c.get(0, 0), lp.c.get(1, 0), lp.objVal());
//            }
//        }
    }
    
    
    
    class mouseScrollListener implements MouseWheelListener {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int units = e.getUnitsToScroll();
            
            double distx = hiX - loX;
            double disty = hiY - loY;
            
            double zoomx = distx / 100.0 * units;
            double zoomy = disty / 100.0 * units;
            
            zoom(zoomx, zoomy);
            
            repaint();
        }
    }
    
    
    
    class mouseListener implements MouseListener, MouseMotionListener {
        private int lastX;
        private int lastY;

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            
            int dx = lastX - x;
            int dy = lastY - y;

            double moveX = distX / getWidth() * dx;
            double moveY = distY / getHeight() * dy;
            
            move(moveX, -moveY);

            repaint();
            
            lastX = x;
            lastY = y;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
        }
    }
}
