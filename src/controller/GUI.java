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

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

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
class GUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JSplitPane jspSplitPane;
    private JMenuItem jmiExit, jmiAbout, jmiScreenshot;
    private JMenuItem jmiZoomIn, jmiZoomOut, jmiNormalSize;
    
    private CCSystem ccs;
    private CLI cli;
    private Console console;
    
    private LP lp;
    
    
    
    /**
     * Initialize the GUI.
     */
    public GUI() {
        super("pplex");
        setTitle("pplex");
        
        ccs = new CCSystem();
        ccs.setAxesVisible(false);
        ccs.setGridVisible(false);
        cli = new CLI();
        console = new Console(cli);
        
        jspSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                            console, ccs);
        jspSplitPane.setDividerSize(3);
        jspSplitPane.setDividerLocation(500);
        jspSplitPane.setResizeWeight(0);
        
        add(jspSplitPane);
        
        setJMenuBar(initializeMenu());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setVisible(true);
        /*  
         * NB! setSize MUST be placed after setVisible(true)! On
         * some system the window is drawn completely blank if it is not.
         */
        setSize(820, 400);
        setLocationRelativeTo(null);
    }
    
    
    
    /**
     * Initialize the menu bar used in the main window.
     * 
     * @return A {@code JMenuBar}
     */
    private final JMenuBar initializeMenu() {
        final JMenuBar jmbMenu = new JMenuBar();
        
        final JMenu jmFile = new JMenu("File");
        final JMenu jmExports = new JMenu("Export as");
        final JMenu jmView = new JMenu("View");
        final JMenu jmHelp = new JMenu("Help");
        
        jmFile.setMnemonic(KeyEvent.VK_F);
        jmExports.setMnemonic(KeyEvent.VK_E);
        jmView.setMnemonic(KeyEvent.VK_V);
        jmHelp.setMnemonic(KeyEvent.VK_H);
        
        jmiExit = new JMenuItem("Exit");
        jmiAbout = new JMenuItem("About");
        jmiScreenshot = new JMenuItem("PNG");
        jmiZoomIn = new JMenuItem("Zoom In");
        jmiZoomOut = new JMenuItem("Zoom Out");
        jmiNormalSize = new JMenuItem("Normal Size");
        
        jmiExit.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        jmiAbout.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK));
        jmiScreenshot.setAccelerator(KeyStroke.getKeyStroke(
                                    KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        jmiZoomIn.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));
        jmiZoomOut.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));
        jmiNormalSize.setAccelerator(KeyStroke.getKeyStroke(
                                     KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK));
        
        jmbMenu.add(jmFile);
        jmbMenu.add(jmView);
        jmbMenu.add(jmHelp);
        
        jmFile.add(jmExports);
        jmFile.add(jmiExit);
        
        jmExports.add(jmiScreenshot);
        
        jmView.add(jmiZoomIn);
        jmView.add(jmiZoomOut);
        jmView.add(jmiNormalSize);
        
        jmHelp.add(jmiAbout);
        
        jmiExit.addActionListener(new exitListener());
        jmiAbout.addActionListener(new aboutListener());
        jmiScreenshot.addActionListener(new screenshotListener());
        jmiZoomIn.addActionListener(new viewZoomInListener());
        jmiZoomOut.addActionListener(new viewZoomOutListener());
        jmiNormalSize.addActionListener(new viewNormalSizeListener());
        
        return jmbMenu;
    }
    
    
    
    @Override
    public void repaint() {
        super.repaint();
        lp = cli.getCurrentProgram();
        VisLP.drawLP(ccs, lp);
    }
    
    
    
    /* ActionListener for a button exiting the program. */
    private class exitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    
    
    
    /* ActionListener for opening the About dialog. */
    private class aboutListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            About about = new About();
            about.setLocationRelativeTo(GUI.this);
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
    
    
    /* ActionListener for taking screenshot. */
    private class screenshotListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Screenshot screenshot = new Screenshot(ccs);
            screenshot.setLocationRelativeTo(GUI.this);
            screenshot.setVisible(true);
        }
    }
}
