package controller;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.math.MathContext;

import javax.swing.JPanel;

import model.LP;

public class Coordinates extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public static LP lp = null;
    
    /* Define the range of the visible xy-plane */
    private double loX = -10;
    private double hiX = 10;
    private double loY = -10;
    private double hiY = 10;
    
    private double distX = hiX - loX;
    private double distY = hiY - loY;
    
    /* Use this precision for BigDecimal */
    private MathContext mc = MathContext.DECIMAL128;
    
    
    
    public Coordinates() {
        this.addMouseListener(new mouseListener());
        this.addMouseMotionListener(new mouseListener());
        this.addMouseWheelListener(new mouseScrollListener());
    }
    
    
    
    void updateDist() {
        distX = hiX - loX;
        distY = hiY - loY;
    }
    
    
    
    void move(double moveX, double moveY) {
        loX += moveX;
        hiX += moveX;
        loY += moveY;
        hiY += moveY;
        updateDist();
    }
    
    
    
    void zoom(double zoomX, double zoomY) {
        loX -= zoomX;
        hiX += zoomX;
        loY -= zoomY;
        hiY += zoomY;
        updateDist();
    }
    
    
    
    void setHiX(double hiX) {
        this.hiX = hiX;
        updateDist();
    }



//    Point2D.Double coordinate(int x, int y) {
//        double xscale = (hix - lowx) / this.getWidth();
//        double yscale = (hiy - lowy) / this.getHeight();
//        
//        double newx = x * xscale + lowx;
//        double newy = y * yscale + lowy;
//        
//        return new Point2D.Double(newx, newy);
//    }
    
    
    
    void drawConstraint(Graphics2D g2d, double cx, double cy, double b) {
        if (cx == 0.0) {
            drawLine(g2d, b, loY, b, hiY);
        } else if (cy == 0.0) {
            drawLine(g2d, loX, b, hiX, b);
        } else {
            drawLine(g2d, loX, (b-cx*loX)/cy, hiX, (b-cx*hiX)/cy);
        }
    }
    
    
    
    void drawLine(Graphics2D g2d, double x1, double y1, double x2, double y2) {
        Point p1 = coordinate(x1, y1);
        Point p2 = coordinate(x2, y2);
        g2d.setColor(Color.BLUE);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
    
    
    
    private boolean isInside(double x, double y) {
        return (x >= loX && x <= hiX && y >= loY && y <= hiY);
    }
    
    
    
    /**
     * Convert 
     * 
     * @param x
     * @param y
     * @return
     */
    Point coordinate(double x, double y) {
        /* Includes padding */
        double xscale = (hiX - loX) / (getWidth() - 10);
        double yscale = (hiY - loY) / (getHeight() - 10);
        
        int newx = (int) Math.round((x - loX) / xscale);
        int newy = (int) Math.round((y - loY) / yscale);
        /* Convert so that increasing y takes you north instead of south. */
        newy = getHeight() - newy;
        
        /* More padding */
        newx += 5;
        newy -= 5;
        
        return new Point(newx, newy);
    }
    
    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        drawAxes(g2d);
        
        if (lp != null) {
//            System.out.println(lp.N_);
            for (int i = 0; i < lp.getNoNonBasic(); i++) {
                drawConstraint(g2d, lp.N_.get(i, 0), 
                        lp.N_.get(i, 1),
                        lp.b.get(i, 0));
            }
        }
        
        
//        drawPoint(g2d, 1, 1);
//        drawPoint(g2d, 0.5, -1.7);
//        drawPoint(g2d, 1.4, -1.4);
//        
//        g2d.setColor(Color.blue);
//        drawLine(g2d, 0.5, 1.7, 1.4, -1.4);
    }
    
    
    
    private BigDecimal findScale(double udist) {
        int x = (int)Math.log10(udist);
        double quot = udist / Math.pow(10, x);
        /* scale = 10 ^ x */
        BigDecimal scale = new BigDecimal(10, mc).pow((int)x, mc);
        if (quot > 5.0) return scale.multiply(new BigDecimal(10), mc);
        if (quot > 4.0) return scale.multiply(new BigDecimal(5), mc);
        if (quot > 2.0) return scale.multiply(new BigDecimal(4), mc);
        if (quot > 1.0) return scale.multiply(new BigDecimal(2), mc);
        else return scale;
    }
    
    
    
    void drawAxes(Graphics2D g2d) {
        /* Find "origo" */
        double ox = 0;
        double oy = 0;
        if (loX >= 0) ox = loX;
        else if (hiX <= 0) ox = hiX;
        if (loY >= 0) oy = loY;
        else if (hiY <= 0) oy = hiY;
        Point o = coordinate(ox, oy);
        
        /* Find axes points in the system */
        Point xaxis_start = new Point(0, o.y);
        Point xaxis_end = new Point(getWidth(), o.y);
        Point yaxis_start = new Point(o.x, 0);
        Point yaxis_end = new Point(o.x, getHeight());
        
        /* Create the axes and draw them */
        g2d.drawLine(xaxis_start.x, xaxis_start.y,
                     xaxis_end.x, xaxis_end.y);
        g2d.drawLine(yaxis_start.x, yaxis_start.y,
                     yaxis_end.x, yaxis_end.y);
        
        
        
        /* Approximate number of pixels needed between each "unit" */
        int pbu = 65;
        
        /* Total number of units on both axes */
        int unitsX = getWidth() / pbu;
        int unitsY = getHeight() / pbu;
        
        /* Exact value between each unit */
        double udistX = distX / unitsX;
        double udistY = distY / unitsY;
        
        /* 
         * The exact value rounded to a value that can be written with
         * very few decimals. Is also "easily" divisible by 5. 
         */
        BigDecimal vbux = findScale(udistX);
        BigDecimal vbuy = findScale(udistY);
        
        /* Divide by 5 to get value between each "minor" unit. */
        BigDecimal five = new BigDecimal(5, mc);
        BigDecimal svbux = vbux.divide(five, mc);
        BigDecimal svbuy = vbuy.divide(five, mc);
        
        /* Draw the units on the x-axis */
        int pix = o.x;
        int q = (int) (loX / svbux.doubleValue());
        while (pix <= getWidth()) {
            /*
             * Simply using double here introduces rounding errors
             * when rval reaches the order of 10^23 or higher or
             * 10^-23 or lower.
             */
            BigDecimal qbd = new BigDecimal(q, mc);
            BigDecimal val = svbux.multiply(qbd, mc);
            double rval = val.doubleValue();
            Point p = coordinate(rval, oy);

            int size = 2;

            if (q++ % 5 == 0 && p.x != o.x) {
                size = 4;
                /*
                 * Cannot use BigDecimal's toString-method since it does not
                 * use scientific notation on positive numbers.
                 */
                String strval = Double.toString(rval);
                g2d.drawString(strval, p.x-strval.length()/2*8, p.y+20);
            }
            g2d.drawLine(p.x, p.y-size, p.x, p.y+size);
            pix = p.x;
        }
        
        
        
        /* Draw the units on the y-axis */
        pix = o.y;
        q = (int) (loY / svbuy.doubleValue());
        while (pix >= 0) {
            BigDecimal qbd = new BigDecimal(q, mc);
            BigDecimal val = svbuy.multiply(qbd, mc);
            double rval = val.doubleValue();
            Point p = coordinate(ox, rval);

            int size = 2;

            if (q++ % 5 == 0 && p.y != o.y) {
                size = 4;
                
                String strval = Double.toString(rval);
                g2d.drawString(strval, p.x-strval.length()*8, p.y+5);
            }
            g2d.drawLine(p.x-size, p.y, p.x+size, p.y);
            pix = p.y;
        }
    }
    
    
    
    void drawPoint(Graphics2D g2d, double x, double y) {
        Point p = coordinate(x, y);
        Ellipse2D r2d = new Ellipse2D.Double(p.x-3, p.y-3, 6, 6);
        g2d.setColor(Color.RED);
        g2d.draw(r2d);
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