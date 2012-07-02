package controller;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;

import model.LP;
import model.Matrix;

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
public class Coordinates extends JPanel {
    private static final long serialVersionUID = 1L;
    
    // TODO: Solve this in another way.
    public static LP lp = null;
    
    /* Define the range of the visible xy-plane */
    private double loX = -10;
    private double hiX = 10;
    private double loY = -10;
    private double hiY = 10;
    
    /* The length of each range */
    private double distX = hiX - loX;
    private double distY = hiY - loY;
    
    /* Use this precision for BigDecimal */
    private MathContext mc = MathContext.DECIMAL128;
    
    
    
    /**
     * Initialize a new empty coordinate system.
     */
    public Coordinates() {
        this.addMouseListener(new mouseListener());
        this.addMouseMotionListener(new mouseListener());
        this.addMouseWheelListener(new mouseScrollListener());
    }
    
    
    
    /*
     * Dictionary form assumes all x-values >= 0. For a geometric
     * representation we need these constraints explicitly stated
     * in order to draw the feasible region.
     * 
     * This method adds the constraints x >= 0 (-x <= 0) 
     * and y >= 0 (-y <= 0) unless more bounding constraints
     * on the x and y-values already exists.
     */
    private void checkLPForBounds() {
        boolean lowerx = false;
        boolean lowery = false;
        
        /* Does lower bounds already exist? */
        for (int i = 0; i < lp.N_.rows(); i++) {
            double x = lp.N_.get(i, 0);
            double y = lp.N_.get(i, 1);
            if (x < 0 && y == 0) {
                lowerx = true;
            } else if (x == 0 && y < 0) {
                lowery = true;
            }
        }
        
        /* Add lower bounds if they do not exist */
        if (!lowerx) {
            Matrix c = new Matrix(new double[] {-1, 0});
            lp.N_ = lp.N_.addBlock(c, Matrix.UNDER);
            
            c = new Matrix(new double[] {0});
            lp.b = lp.b.addBlock(c, Matrix.UNDER);
        }
        if (!lowery) {
            Matrix c = new Matrix(new double[] {0, -1});
            lp.N_ = lp.N_.addBlock(c, Matrix.UNDER);
            
            c = new Matrix(new double[] {0});
            lp.b = lp.b.addBlock(c, Matrix.UNDER);
        }
    }



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
    private Point2D[] convex(Point2D[] points) {
        Arrays.sort(points, new Point2DComparator());
        
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
    
    
    /*
     * Convert points in coordinate system 1 to system 2
     * as described in the class documentation.
     */
    Point2D.Double translate(Point p) {
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
        /* Includes padding */
        double xscale = (hiX - loX) / (getWidth() - 10);
        double yscale = (hiY - loY) / (getHeight() - 10);
        
        int newx = (int) Math.round((p2d.getX() - loX) / xscale);
        int newy = (int) Math.round((p2d.getY() - loY) / yscale);
        /* Convert so that increasing y takes you north instead of south. */
        newy = getHeight() - newy;
        
        /* More padding */
        newx += 5;
        newy -= 5;
        
        return new Point(newx, newy);
    }



    /*
     * Draw the axes and unit lines in the best looking
     * way possible for the given x- and y-ranges.
     */
    private void drawAxes(Graphics2D g2d) {
        /* Find left/right position for the unit line values. */
        boolean xsouth = true;
        boolean ywest = true;
        
        /* Find origo */
        double ox = 0;
        double oy = 0;
        
        if (loX >= 0) {
            ox = loX;
            ywest = false;
        } else if (hiX <= 0) {
            ox = hiX;
        }
        
        if (loY >= 0) {
            oy = loY;
            xsouth = false;
        } else if (hiY <= 0) {
            oy = hiY;
        }
        
        Point2D o2d = new Point2D.Double(ox, oy);
        Point o = translate(o2d);

        /* Find axes points in the system */
        Point xaxis_start = new Point(0, o.y);
        Point xaxis_end = new Point(getWidth(), o.y);
        Point yaxis_start = new Point(o.x, 0);
        Point yaxis_end = new Point(o.x, getHeight());

        g2d.setColor(Color.BLACK);
        /* Create the axes and draw them */
        g2d.drawLine(xaxis_start.x, xaxis_start.y,
                xaxis_end.x, xaxis_end.y);
        g2d.drawLine(yaxis_start.x, yaxis_start.y,
                yaxis_end.x, yaxis_end.y);

        /* Approximate number of pixels needed between each "unit" */
        int pbux = 65;
        int pbuy = 65;

        /* Total number of units on both axes */
        int unitsX = getWidth() / pbux;
        int unitsY = getHeight() / pbuy;

        /* Exact value between each unit */
        double udistX = distX / unitsX;
        double udistY = distY / unitsY;

        /* 
         * The exact value rounded to a value that can be written with
         * very few decimals.
         * 
         * TODO: Find a better mathematical term
         *       for the numbers described above.
         */
        //        BigDecimal vbux = findScale(udistX);
        //        BigDecimal vbuy = findScale(udistY);

        BigDecimal vbuX = findDist(1, new BigDecimal(udistX, mc));
        BigDecimal vbuY = findDist(1, new BigDecimal(udistY, mc));

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
            BigDecimal qbd = new BigDecimal(q++, mc);
            BigDecimal val = vbuX.multiply(qbd, mc);
            
            double rval = val.doubleValue();
            
            Point2D p2d = new Point2D.Double(rval, oy);
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
        
        
        
        pix = o.y;
        /* Find coefficient for vbuX for the first visible unit */
        q = (int) (loY / vbuY.doubleValue());

        /* Draw the units on the x-axis */
        while (pix >= 0) {
            /*
             * Simply using double here introduces rounding errors
             * when rval reaches the order of 10^23 or higher or
             * 10^-23 or lower. Therefore using BigDecimal.
             */
            BigDecimal qbd = new BigDecimal(q++, mc);
            BigDecimal val = vbuX.multiply(qbd, mc);
            
            double rval = val.doubleValue();
            
            Point2D p2d = new Point2D.Double(ox, rval);
            Point p = translate(p2d);

            /*
             * Cannot use BigDecimal's toString-method since it does not
             * use scientific notation on positive numbers.
             */
            String strval = Double.toString(rval);
            
            int pixelPerChar = 8;
            int strValPixels = pixelPerChar * strval.length();
            
            int offset = ywest ? -strValPixels : 5;
            
            if (val.floatValue() != 0.0)
                g2d.drawString(strval, p.x+offset, p.y+5);
            
            /* Length of unit line in pixels. */
            int size = 4;
            g2d.drawLine(p.x-size, p.y, p.x+size, p.y);
            
            pix = p.y;
        }
    }



    /* Draw a single linear constraint */
    private void drawConstraint(Graphics2D g2d, double cx, double cy,
                                                            double b) {
        Point2D p2d1;
        Point2D p2d2;
        if (cy == 0.0) {
            int mul = (cx < 0) ? -1 : 1;
            p2d1 = new Point2D.Double(b*mul, loY);
            p2d2 = new Point2D.Double(b*mul, hiY);
        } else if (cx == 0.0) {
            int mul = (cy < 0) ? -1 : 1;
            p2d1 = new Point2D.Double(loX, b*mul);
            p2d2 = new Point2D.Double(hiX, b*mul);
        } else {
            p2d1 = new Point2D.Double(loX, (b-cx*loX)/cy);
            p2d2 = new Point2D.Double(hiX, (b-cx*hiX)/cy);
        }
        drawLine(g2d, p2d1, p2d2);
    }



    /* Draw all constraints of an LP. */
    private void drawConstraints(Graphics2D g2d, LP lp) {
        Matrix cons = lp.getConstraints();
        for (int i = 0; i < cons.rows(); i++) {
            drawConstraint(g2d, cons.get(i, 0), cons.get(i, 1),
                                                              cons.get(i, 2));
        }
    }



    /* Draw a straight line between the given points. */
    private void drawLine(Graphics2D g2d, Point2D p2d1, Point2D p2d2) {
        Point p1 = translate(p2d1);
        Point p2 = translate(p2d2);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }



    /* Draw an LP's constraints and color its feasible region. */
    private void drawLP(Graphics2D g2d, LP lp) {
        Point2D[] pconv = convex(getFeasibleIntersections(lp));

        Polygon poly = polygon(pconv);

        g2d.setColor(new Color(245, 234, 230));
        g2d.drawPolygon(poly);
        g2d.fillPolygon(poly);

        g2d.setColor(Color.GRAY);
        drawConstraints(g2d, lp);
        g2d.setColor(Color.black);
        for (Point2D p : pconv) drawPoint(g2d, p);
    }



    private void drawObjPoint(Graphics2D g2d, LP lp) {
        double[] pdouble = lp.point();
        Point2D p2d = new Point2D.Double(pdouble[0], pdouble[1]);
        drawPoint(g2d, p2d);
    }



    private void drawPoint(Graphics2D g2d, Point2D p2d) {
        Point p = translate(p2d);
        Ellipse2D r2d = new Ellipse2D.Double(p.x-3, p.y-3, 6, 6);
        g2d.fill(r2d);
    }



    private BigDecimal findDist(int power, BigDecimal udist) {
        BigDecimal pow = new BigDecimal(10).pow(power, mc);
        if (udist.compareTo(pow) < 0) return findDist(power-1, udist);

        BigDecimal pow2 = pow.multiply(new BigDecimal(2));
        if (udist.compareTo(pow2) < 0) return pow;

        BigDecimal pow5 = pow.multiply(new BigDecimal(5));
        if (udist.compareTo(pow5) < 0) return pow2;

        BigDecimal pow10 = pow.multiply(new BigDecimal(10));
        if (udist.compareTo(pow10) < 0) return pow5;

        return findDist(power+1, udist);

        /* Method without BigDecimal is kept to make this less confusing */
        /*
         * double pow = Math.pow(10, power);
         * if (udist < pow) return findDist(power-1, udist);
         * if (udist < 2*pow) return pow;
         * if (udist < 4*pow) return 2*pow;
         * if (udist < 5*pow) return 4*pow;
         * if (udist < 10*pow) return 5*pow;
         * return findDist(power+1, udist); // if (udist >= 10*pow);
         */
    }



    // TODO: Fix this method. Earlier versions were better but slower.
    private BigDecimal findScale(double udist) {
        int x = (int)Math.log10(udist);
        /* scale = 10 ^ x */
        BigDecimal scale = new BigDecimal(10, mc).pow((int)x, mc);
        
        double quot = udist / scale.doubleValue();
        if (quot > 5.0) return scale.multiply(new BigDecimal(10), mc);
        if (quot > 4.0) return scale.multiply(new BigDecimal(5), mc);
        if (quot > 2.0) return scale.multiply(new BigDecimal(4), mc);
        if (quot > 1.0) return scale.multiply(new BigDecimal(2), mc);
        else return scale;
    }



    /* 
     * Return all intersections that are satisfied
     * by ALL inequalities of the LP.
     */
    private Point2D[] getFeasibleIntersections(LP lp) {
        LinkedList<Point2D> points = new LinkedList<Point2D>();
        
        /* Find all intersections */
        for (int i = 0; i < lp.N_.rows(); i++) {
            for (int j = 0; j < lp.N_.rows(); j++) {
                if (i == j) continue;
                
                Matrix line1 = lp.N_.getRow(i);
                Matrix line2 = lp.N_.getRow(j);
                
                double[] bval = new double[] {lp.b.get(i, 0), lp.b.get(j, 0)};
                Matrix b = new Matrix(bval).transpose();
                Matrix sys = line1.addBlock(line2, Matrix.UNDER);
                
                try {
                    Matrix point = sys.inverse().product(b);
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
        
        /* Remove intersections that are not satisfied by ALL inequalities. */
        Iterator<Point2D> iter = points.iterator();
        
        while (iter.hasNext()) {
            Point2D p2d = iter.next();
            for (int i = 0; i < lp.N_.rows(); i++) {
                double x = p2d.getX();
                double y = p2d.getY();
                
                float val = (float) (lp.N_.get(i, 0)*x + lp.N_.get(i, 1)*y);
                if (val > lp.b.get(i, 0)) {
                    iter.remove();
                    break;
                }
            }
        }
        return points.toArray(new Point2D[0]);
    }



    private void move(double moveX, double moveY) {
        loX += moveX;
        hiX += moveX;
        loY += moveY;
        hiY += moveY;
        updateDist();
    }



    /* Create a convex polygon of ordered value points.
     * 
     * Points must be ordered so that drawing lines
     * between the points in the given order forms a
     * convex polygon.
     */
    private Polygon polygon(Point2D[] points) {
        int[] xpoints = new int[points.length];
        int[] ypoints = new int[points.length];
        
        int i = 0;
        for (Point2D p2d : points) {
            Point p = translate(p2d);
            xpoints[i] = p.x;
            ypoints[i++] = p.y;
        }
        return new Polygon(xpoints, ypoints, points.length);
    }



    private void setRange(double loX, double hiX, double loY, double hiY) {
        this.loX = loX;
        this.hiX = hiX;
        this.loY = loY;
        this.hiY = hiY;
        updateDist();
    }



    private void updateDist() {
        distX = hiX - loX;
        distY = hiY - loY;
    }
    
    
    
    private void zoom(double zoomX, double zoomY) {
        loX -= zoomX;
        hiX += zoomX;
        loY -= zoomY;
        hiY += zoomY;
        updateDist();
    }
    
    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (lp != null) {
            if (lp.getNoBasic() != 2) {
                String s  = "Current LP is not representable";
                String s2 = "in two dimensions";
                g2d.drawString(s, getWidth()/2-s.length()*2-20, getHeight()/2);
                g2d.drawString(s2, getWidth()/2-s2.length()*3, getHeight()/2+20);
            } else {
                checkLPForBounds();
                
                drawLP(g2d, lp);
                drawAxes(g2d);
                
                g2d.setColor(Color.RED);
                drawObjPoint(g2d, lp);
                
                drawConstraint(g2d, lp.c.get(0, 0), lp.c.get(1, 0), lp.objVal());
            }
        }
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
    
    
    
    private class Point2DComparator implements Comparator<Point2D> {

        @Override
        public int compare(Point2D o1, Point2D o2) {
            double s = o1.getX() - o2.getX();
            if (s > 0) return 1;
            if (s < 0) return -1;
            
            s = o1.getY() - o2.getY();
            if (s > 0) return 1;
            if (s < 0) return -1;
            
            return 0;
        }
    }
}
