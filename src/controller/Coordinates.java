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

import javax.swing.JPanel;

import model.LP;

public class Coordinates extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public static LP lp = null;
    
    /* Define the range of the visible xy-plane */
    private double loX = -1;
    private double hiX = 5;
    private double loY = -1;
    private double hiY = 5;
    
    
    
    public Coordinates() {
        this.addMouseListener(new mouseListener());
        this.addMouseMotionListener(new mouseListener());
        this.addMouseWheelListener(new mouseScrollListener());
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
            System.out.println(lp.N_);
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
    
    
    
    private double findDist(int power, double udist) {
        double pow = Math.pow(10, power);
        if (udist < pow) return findDist(power-1, udist);
        if (udist < 2*pow) return pow;
        if (udist < 4*pow) return 2*pow;
        if (udist < 5*pow) return 4*pow;
        if (udist < 10*pow) return 5*pow;
        else return findDist(power+1, udist); // if (udist >= 10*pow);
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
        
        
        int pbu = 100;
        double distx = hiX - loX;
        double disty = hiY - loY;
        int unitsx = getWidth() / pbu;
        int unitsy = getHeight() / pbu;
        double udistx = distx / unitsx;
        double udisty = disty / unitsy;
        double vbux = findDist(1, udistx);
        double vbuy = findDist(1, udisty);
        double svbux = vbux / 5;
        double svbuy = vbuy / 5;
        
        /* Draw the units on the x-axis on the right side of "origo" */
        for (double i = ox+vbux; i <= hiX; i += vbux) {
            Point p = coordinate(i, oy);
            g2d.drawLine(p.x, p.y-4, p.x, p.y+4);
            
            String val = String.format("%.1f", i);
            g2d.drawString(val, p.x-7, p.y+20);
        }
        for (double i = ox+svbux; i <= hiX; i += svbux) {
            Point p = coordinate(i, oy);
            g2d.drawLine(p.x, p.y-2, p.x, p.y+2);
        }
        
        
        
        /* Draw the units on the x-axis on the left side of "origo" */
        for (double i = ox-vbux; i >= loX; i -= vbux) {
            Point p = coordinate(i, oy);
            g2d.drawLine(p.x, p.y-4, p.x, p.y+4);
            
            String val = String.format("%.1f", i);
            g2d.drawString(val, p.x-7, p.y+20);
        }
        for (double i = ox-svbux; i >= loX; i -= svbux) {
            Point p = coordinate(i, oy);
            g2d.drawLine(p.x, p.y-2, p.x, p.y+2);
        }
        
        
        
        
        
        
        /* Draw the units on the y-axis north of "origo" */
        for (double i = oy+vbuy; i <= hiY; i += vbuy) {
            Point p = coordinate(ox, i);
            g2d.drawLine(p.x-4, p.y, p.x+4, p.y);
            
            String val = String.format("%.1f", i);
            g2d.drawString(val, p.x-25, p.y+5);
        }
        for (double i = ox+svbuy; i <= hiY; i += svbuy) {
            Point p = coordinate(ox, i);
            g2d.drawLine(p.x-2, p.y, p.x+2, p.y);
        }
        
        
        
        /* Draw the units on the y-axis south of "origo" */
        for (double i = oy-vbuy; i >= loY; i -= vbuy) {
            Point p = coordinate(ox, i);
            g2d.drawLine(p.x-4, p.y, p.x+4, p.y);
            
            String val = String.format("%.1f", i);
            g2d.drawString(val, p.x-30, p.y+5);
        }
        for (double i = ox-svbuy; i >= loY; i -= svbuy) {
            Point p = coordinate(ox, i);
            g2d.drawLine(p.x-2, p.y, p.x+2, p.y);
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
            
            double zoom = units / 10.0;
            
            loX -= zoom;
            hiX += zoom;
            
            loY -= zoom;
            hiY += zoom;
            
            repaint();
        }
        
    }
    
    
    
    class mouseListener implements MouseListener, MouseMotionListener {
        int lastX;
        int lastY;

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

            loX += dx / 100.0;
            hiX += dx / 100.0;
            loY -= dy / 100.0;
            hiY -= dy / 100.0;

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