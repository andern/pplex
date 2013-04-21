/*
 * Copyright (C) 2012, 2013 Andreas Halle
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import controller.Data;

/**
 * Class for the about dialog
 * 
 * @author Andreas Halle
 */
public class About extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    private JButton jbnOK;
    private JLabel jlbLogo, jlbText;
    
    
    
    public About() {
        setTitle("About");
        //setPreferredSize(new Dimension(420, 380));
        setResizable(false);
        setLocationRelativeTo(null);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 0, 0);
        
        /* Try to add the logo */
		try {
			BufferedImage logo;
			logo = ImageIO.read(new File("res/big_icon.png"));
			jlbLogo = new JLabel(new ImageIcon(logo));
			jlbLogo.setHorizontalAlignment(SwingConstants.LEFT);
	        gbc.gridx = 0;
	        gbc.gridy = 0;
	        gbc.gridwidth = 2;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        add(jlbLogo, gbc);
		} catch (IOException e) {
			/* Don't add the logo if error occurs. */
		}
		
		jlbText = new JLabel("<html>"
              + "pplex <br>"
              + "Version " + Data.VERSION + "<br>"
              + "A pedagogical implementation of the simplex method <br>"
              + Data.COPY + "<br>"
              + "</html>");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(jlbText, gbc);
        
        jbnOK = new JButton("OK");
        jbnOK.addActionListener(this);
        gbc.gridx = 3;
        gbc.gridy = 2;
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
