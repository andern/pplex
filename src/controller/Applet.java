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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

import parser.LpFileFormatLexer;
import parser.LpFileFormatParser;

import cartesian.coordinate.CCSystem;

import model.LP;

/**
 * The GUI class creates a {@code JFrame} consisting of two parts:
 * a console and a coordinate system.
 * 
 * @author  Andreas Halle
 * @see     Console
 * @see     cartesian.coordinate.CCSystem
 */
public class Applet extends JApplet {
    private static final long serialVersionUID = 1L;
    
    private JSplitPane jspSplitPane;
    private JMenuItem jmiAbout;
    private JMenuItem jmiZoomIn, jmiZoomOut, jmiNormalSize;
    
    private CCSystem ccs;
    private CLI cli;
    private Console console;
    
    private LP lp;
    
    private Map<String, String> examples = createExamples();
    
    private static Map<String, String> createExamples() {
        Map<String, String> map = new LinkedHashMap<String, String>();

        map.put("easy_example", "max     x  + 2y\n"
                     + "subject to      - 2x  +  y <= 2\n" 
                     +                  "- x  + 2y <= 7\n"
                     +                    "x       <= 3");

        map.put("simple_example", "max     x  +   y\n"
                     + "subject to        2x  +   y <= 6\n" 
                     +                  " 7x  + 13y <= 40\n");

        map.put("optimal_edge", "max -2x + 4y\n"
                     + "subject to   -2x +  y <= 2\n"
                     +               "-x + 2y <= 7\n"
                     +                "x -  y <= 0\n"
                     +                "x      <= 3");

        map.put("unbounded", "max         x  +   y\n"                
                     + "subject to        x  -  2y <= 10\n" 
                     +                  "-x  +  2y <= 10\n");
        
        map.put("degeneracy_1", "max      x  + 10y\n"
                     + "subject to       2x  +   y <= 6\n" 
                     +                   "x  +  2y <= 6\n"
                     +                   "x  +   y <= 4");

        map.put("degeneracy_2", "max x + 2y\n"
                     + "subject to -2x +  y <= 2\n"
                     +             "-x + 2y <= 7\n"
                     +              "x      <= 3\n"
                     +             "-x +  y <= 3");

        map.put("cycling", "max            10x1 -  57x2 -   9x3 - 24x4\n"
                       + "subject to      0.5x1 - 5.5x2 - 2.5x3 +  9x4 <= 0\n"
                       +                 "0.5x1 - 1.5x2 - 0.5x3 +   x4 <= 0\n"
                       +                    "x1                        <= 1\n");

        map.put("phase_I_feasible_z=0", "max                     -z\n"
                     +                   "subject to   x  -  y   -z <= -1\n"
                     +                                "x  +  y   -z <=  2");
        
        map.put("phase_I_feasible_z>0", "max                -z\n"
                      +              "subject to  x  -  y   -z <= -3\n"
                      +                          "x  +  y   -z <=  2\n");
        
        map.put("vanderbei_p20_phase_I", "max       -z\n"
                     + "subject to       -x1 +  x2  -z  <= -1\n"
                     +                  "-x1 - 2x2  -z  <= -2\n"
                     +                         "x2  -z  <=  1"); 
        
        map.put("vanderbei_p20_phase_II", "max -ow2 -  ow1\n"
                      +      "subject to       -ow2 +  ow1 <= 1\n"
                      +                       "-ow2 - 2ow1 <= 4\n"
                      +                        "ow2 -  ow1 <= 2");
        
        map.put("dual_phase_I", "max            2x + y\n"
                      +      "subject to         x - y <= -1\n"
                      +                        " x + y <= 2");
        
        map.put("vanderbei_exc2.8", "max   3x  +  2y\n"
                       + "subject to        x  -  2y <= 1\n" 
                       +                   "x  -   y <= 2\n"
                       +                  "2x  -   y <= 6\n"
                       +                   "x        <= 5\n"
                       +                  "2x  +   y <= 16\n"
                       +                   "x  +   y <= 12\n"
                       +                   "x  +  2y <= 21\n"
                       +                          "y <= 10");
        
        map.put("vanderbei_p39", "max       x1  +  2x2  +  3x3\n"
                       + "subject to        x1          +  2x3 <= 3\n" 
                       +                           "x2  +  2x3 <= 2");
        
        map.put("vanderbei_p40", "max       x1  +  2x2  +  3x3\n"
                       + "subject to        x1          +  2x3 <= 2\n" 
                       +                           "x2  +  2x3 <= 2");
        
        map.put("vanderbei_p49", "max    -50b1  -  5b2 - 0.5b3  + 100x1  +  10x2  +  x3\n"
                       + "subject to       -b1  +                    x1                 <= 0\n" 
                       +                "-10b1  -   b2          +  20x1  +    x2        <= 0\n"
                       +               "-100b1  - 10b2  -   b3  + 200x1  +  20x2   + x3 <= 0");
        
        map.put("vanderbei_p71", "max       -x1  + 4x2\n"
                       + "subject to      - 2x1  -  x2 <=  4\n" 
                       +                 "- 2x1  + 4x2 <= -8\n" 
                       +                 "-  x1  + 3x2 <= -7");
                
        map.put("vanderbei_exc5.6", "max - x1 - 2x2\n"
                   +        "subject to - 2x1 + 7x2 <=  6\n"
                   +                   "- 3x1 +  x2 <= -1\n"
                   +                     "9x1 - 4x2 <=  6\n"
                   +                      "x1 -  x2 <=  1\n"
                   +                     "7x1 - 3x2 <=  6\n"
                   +                   "- 5x1 + 2x2 <= -3");
           

    	return map;
    }
    
    /**
     * Initialize the GUI.
     */
    public Applet() {
        ccs = new CCSystem();
        cli = new CLI();
        console = new Console(cli);
        
        jspSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                            console, ccs);
        jspSplitPane.setDividerSize(3);
        jspSplitPane.setDividerLocation(500);
        jspSplitPane.setResizeWeight(0);
        
        add(jspSplitPane);
        
        setJMenuBar(initializeMenu());
        
        setVisible(true);
        /*  
         * NB! setSize MUST be placed after setVisible(true)! On
         * some system the window is drawn completely blank if it is not.
         */
        setSize(820, 400);
    }
    
    
    
    /**
     * Initialize the menu bar used in the main window.
     * 
     * @return A {@code JMenuBar}
     */
    private final JMenuBar initializeMenu() {
        final JMenuBar jmbMenu = new JMenuBar();
        
        final JMenu jmView = new JMenu("View");
        final JMenu jmHelp = new JMenu("Help");
        final JMenu jmExamples = new JMenu("Examples");
        
        jmView.setMnemonic(KeyEvent.VK_V);
        jmHelp.setMnemonic(KeyEvent.VK_H);
        
        jmiAbout = new JMenuItem("About");
        jmiZoomIn = new JMenuItem("Zoom In");
        jmiZoomOut = new JMenuItem("Zoom Out");
        jmiNormalSize = new JMenuItem("Normal Size");
        
        jmiAbout.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK));
        jmiZoomIn.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));
        jmiZoomOut.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));
        jmiNormalSize.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK));
        
        jmbMenu.add(jmView);
        jmbMenu.add(jmHelp);
        
        jmView.add(jmiZoomIn);
        jmView.add(jmiZoomOut);
        jmView.add(jmiNormalSize);
        
        jmHelp.add(jmiAbout);
        
        jmbMenu.add(jmExamples);
        
        jmiAbout.addActionListener(new aboutListener());
        jmiZoomIn.addActionListener(new viewZoomInListener());
        jmiZoomOut.addActionListener(new viewZoomOutListener());
        jmiNormalSize.addActionListener(new viewNormalSizeListener());
        
        final Map<String, LP> lpExamples = new LinkedHashMap<String, LP>();
        
        for (String s : examples.keySet()) {
        	CharStream stream;
            try {
            	stream = new ANTLRStringStream(examples.get(s));

            	LpFileFormatLexer lexer = new LpFileFormatLexer(stream);
            	TokenStream tokenStream = new CommonTokenStream(lexer);
            	LpFileFormatParser parser = new LpFileFormatParser(tokenStream);
                lpExamples.put(s, parser.lpfromfile());
            } catch (Exception e) {
            	System.out.println(e.getLocalizedMessage());
            }
        }
        
        /* Add examples as menu items */
        for (final String s : lpExamples.keySet()) {
            JMenuItem jmiEx = new JMenuItem(s);
            
            jmiEx.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cli.addLp(lpExamples.get(s));
                    repaint();
                    console.putText("Loaded example " + s + " successfully.\n");
                }});
            
            jmExamples.add(jmiEx);
        }
        
        return jmbMenu;
    }
    
    
    
    @Override
    public void repaint() {
        super.repaint();
        lp = cli.getCurrentProgram();
        VisLP.drawLP(ccs, lp);
    }
    
    
    
    /* ActionListener for opening the About dialog. */
    private class aboutListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            About about = new About();
            about.setLocationRelativeTo(Applet.this);
            about.setVisible(true);
        }
    }
    
    
    
    /* ActionListener for increase font size. */
    private class viewZoomInListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            console.increaseFont();
        }
    }
    
    
    
    
    /* ActionListener for decrease font size. */
    private class viewZoomOutListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            console.decreaseFont();
        }
    }
    
    
    
    /* ActionListener for setting default font size. */
    private class viewNormalSizeListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            console.defaultFont();
        }
    }
}
