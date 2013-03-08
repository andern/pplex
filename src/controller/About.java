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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import controller.shell.Data;

/**
 * Class for the about dialog
 * 
 * @author Andreas Halle
 */
public class About extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    private JButton jbnOK;
    private JLabel jlbLabel;
    
    
    
    public About() {
        setTitle("About");
        setPreferredSize(new Dimension(420, 380));
        setResizable(false);
        setLocationRelativeTo(null);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        /* Adding a label */
        jlbLabel = new JLabel("<html><br>pplex<br>"
                            + "version " + Data.VERSION + "<br>"
                            + "<br>A pedagogical implementation of the"
                            + " simplex method.<br>"
                            + "<br>"
                            + "Copyright (C) 2012 Andreas Halle"
                            + "<br>");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(jlbLabel, gbc);
        
        /* Adding an OK button */
        jbnOK = new JButton("OK");
        jbnOK.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(jbnOK, gbc);
        
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getID() != ActionEvent.ACTION_PERFORMED)
            return;
        
        if (e.getSource().equals(jbnOK))
            this.dispose();
    }
   
}
