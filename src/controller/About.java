package controller;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

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
                            + "version 0.2.0<br>"
                            + "<br>A pedagogical implementation of the"
                            + " simplex method.<br>");
//                            + "<br>"
//                            + "Copyright (C) 2012 Andreas Halle, Marc Bezem"
//                            + "<br>");
        
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
