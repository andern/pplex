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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import controller.GUI;

/**
 * The {@code Console} class represents a visual/graphical console
 * relaying messages from/to the command line interface.
 * 
 * @author  Andreas Halle
 * @author  Thomas Le
 * @see     CLI
 * @see     cartesian.coordinate.CCSystem
 * @see     GUI
 */
class Console extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JScrollPane jspConsoleScroll;
    private JTextArea jtaConsole;
    private JTextField jtfInput;
    private Font consoleFont;
    
    /* Keep a history of commands sent to the console */
    private ArrayList<String> cliHistory;
    private int p;
    
    private CLI cli;
    
    
    
    /**
     * Initialize the console.
     */
    public Console(CLI cli) {
        this.cli = cli;
        
        setLayout(new BorderLayout());
        
        jtaConsole = new JTextArea(Data.FWELCOME + "\n");
        jtaConsole.setLineWrap(true);
        jtaConsole.setEditable(false);
        jtaConsole.setAutoscrolls(true);
        jtaConsole.setCaretPosition(Data.FWELCOME.length()+1);
        jtaConsole.setFont(new Font("Monospaced",Font.PLAIN,12));
        jtaConsole.setBackground(super.getBackground());
        
        // Sets global value for font.
        consoleFont = jtaConsole.getFont();

        jspConsoleScroll = new JScrollPane(jtaConsole);
        jspConsoleScroll.addMouseWheelListener(new mouseWheelListener());
        add(jspConsoleScroll, BorderLayout.CENTER);
        
        jtfInput = new JTextField();
        jtfInput.setFont(new Font("Courier",Font.PLAIN,12));
        add(jtfInput, BorderLayout.PAGE_END);
        
        jtfInput.addKeyListener(new InputListener());
        
        
        cliHistory = new ArrayList<String>();
//        cliHistory.add("");
        p = 0;
    }
    
    
    
    protected void putText(String s) {
        jtaConsole.append(s);
    }
    
    
    
    /* KeyListener for the console. Implements history-browsing. */
    // TODO: WISHLIST: Tab-completion of commands and files.
    private class InputListener implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                String text = jtfInput.getText();
                if (!text.equals("")) {
                    jtaConsole.append("> " + text + "\n");
                    jtaConsole.append(cli.parseCmd(text));
                    jtaConsole.append("\n");
                    cliHistory.add(text);
                    jtaConsole.setCaretPosition(jtaConsole.getDocument().getLength());
                }
                p = cliHistory.size();
                jtfInput.setText("");
                /* 
                 * Update parent so that the coordinate system gets updated
                 * according to the new changes done by the console.
                 */
                getTopLevelAncestor().repaint();
                repaint();
                break;
                
            case KeyEvent.VK_UP:
                if (cliHistory.size() == 0) break;
                if (p > 0) p--;
                jtfInput.setText(cliHistory.get(p));
                break;
                
            case KeyEvent.VK_DOWN:
                if (cliHistory.size() == 0) break;
                if (p == cliHistory.size()-1) {
                    p = cliHistory.size();
                    jtfInput.setText("");
                } else if(p < cliHistory.size()-1) {
                    jtfInput.setText(cliHistory.get(++p));
                }
                break;
                
            case KeyEvent.VK_PAGE_UP:
                if (cliHistory.size() == 0) break;
                p = 0;
                jtfInput.setText(cliHistory.get(p));
                break;
                
            case KeyEvent.VK_PAGE_DOWN:
                if (cliHistory.size() == 0) break;
                if (p == cliHistory.size()-1) {
                    p = cliHistory.size();
                    jtfInput.setText("");
                } else {
                    p = cliHistory.size()-1;
                    jtfInput.setText(cliHistory.get(p));
                }
                break;
            }
        }
        public void keyReleased(KeyEvent e) {}
        public void keyTyped(KeyEvent e) {}
    }
    
    
    
    /**
     * Decrease the font size of output console.
     */
    protected void decreaseFont() {
        float size = consoleFont.getSize() - 1.0f;
        if (size >= 7) {
            size--;
            consoleFont = consoleFont.deriveFont(size);
            updateFont();
        }
    }



    /**
     * Set the default font-size.
     */
    protected void defaultFont() {
        float size = 12f;
        consoleFont = consoleFont.deriveFont(size);
        updateFont();
    }



    /**
     * Increase the font size of output console.
     */
    protected void increaseFont() {
        float size = consoleFont.getSize();
        if (size <= 100) {
            size++;
            consoleFont = consoleFont.deriveFont(size);
            updateFont();
        }
    }
    

    
    /*
     * Update the font-size on console output and input field.
     */
    private void updateFont() {
        jtaConsole.setFont(consoleFont);
        jtfInput.setFont(consoleFont);
        FontMetrics fm = jtfInput.getFontMetrics(consoleFont);
        jtfInput.setSize(new Dimension(jtfInput.getWidth(), fm.getHeight()));

        /* Repaints the console to prevent window size issues */
        getTopLevelAncestor().repaint();
    }
    
    
    
    /* MouseWheelListener for the console. Implements Ctrl+Wheel zoom. */
    class mouseWheelListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if(e.isControlDown()) {
                int units = e.getUnitsToScroll();
                
                if(units < 0) increaseFont();
                else decreaseFont();
            } 
        }
    }
    
}