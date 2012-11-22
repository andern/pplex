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

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import cartesian.coordinate.CCSystem;


/**
 * This class takes screenshot of the class {@code ccs.CCSystem}.
 *
 * @author  Thomas Le
 * @see     cartesian.coordinate.CCSystem
 */
public class Screenshot extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JLabel jlbFilePath;
    private JTextField jtfFilePath, jtfWidth, jtfHeight;
    private CCSystem ccs;
    private JButton jbnBrowse, jbnOK, jbnCancel;
    private JComboBox jcResolutionList;
    private ResolutionType rt;


    /**
     * Enum of resolutions
     */
    private enum ResolutionType {
        DEFAULT("Default",0,0), SD480("SD (480p)",640,480), HD720("HD (720p)",1280,720), HD1080("HD (1080p)",1920,1080);

        private final String name;
        private final int width, height;

        private ResolutionType(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }

        public String toString() {
            if(name.equals("Default"))
                return name+" (window)";
            return name+" ("+width+"x"+height+")";
        }
    }



    /**
     * Construct the screenshot window
     * 
     * @param ccs screenshot of the coordinate
     */
    public Screenshot(CCSystem ccs) {
        this.ccs = ccs;
        setTitle("Export image as PNG");
        setResizable(false);
        setLocationRelativeTo(null);


        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        /*
         * Resolution box
         */
        JPanel jpRes = new JPanel();
        jpRes.setBorder(new TitledBorder("Resolution"));
        jpRes.setLayout(new GridBagLayout());

        /* Resolution label */
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 0;
        gbc.gridy = 0;
        jpRes.add(new JLabel("Resolution:"), gbc);

        /* Resolution ComboBox */
        jcResolutionList = new JComboBox(ResolutionType.values());
        jcResolutionList.addActionListener(this);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        jpRes.add(jcResolutionList, gbc);

        /* Width label */
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 0;
        gbc.gridy = 1;
        jpRes.add(new JLabel("Width:"), gbc);

        /* Width field */
        jtfWidth = new JTextField(4);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 1;
        gbc.gridy = 1;
        jpRes.add(jtfWidth, gbc);

        /* Height label */
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 0;
        gbc.gridy = 2;
        jpRes.add(new JLabel("Height:"), gbc);

        /* Height field */
        jtfHeight = new JTextField(4);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 1;
        gbc.gridy = 2;
        jpRes.add(jtfHeight, gbc);

        /* Add resolution box */
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0.3;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(jpRes, gbc);

        /* Reset back to default */
        gbc.gridwidth = 1;

        /*
         * Directory box 
         */
        JPanel jpDir = new JPanel();
        jpDir.setBorder(new TitledBorder("Directory"));
        jpDir.setLayout(new GridBagLayout());

        /* File path label */
        jlbFilePath = new JLabel("File path:");
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        jpDir.add(jlbFilePath, gbc);

        /* File path Field */
        jtfFilePath = new JTextField(25);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 1;
        gbc.gridy = 0;
        jpDir.add(jtfFilePath, gbc);

        /* Browse button */
        jbnBrowse = new JButton("Browse");
        jbnBrowse.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        jpDir.add(jbnBrowse, gbc);       

        /* Add directory box */
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(jpDir, gbc);

        /* Reset back to default */
        gbc.gridwidth = 1;

        /*
         * Buttons
         */
        /* Ok button */
        jbnOK = new JButton("OK");
        jbnOK.addActionListener(this);
        gbc.insets = new Insets(10, 300, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        add(jbnOK, gbc);

        /* Cancel button */
        jbnCancel = new JButton("Cancel");
        jbnCancel.addActionListener(this);
        gbc.insets = new Insets(10, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        add(jbnCancel, gbc);

        pack();

        /* Set default values to fields */
        jtfFilePath.setText("screenshot.png");
        jtfWidth.setText(Integer.toString(ccs.getWidth()));
        jtfHeight.setText(Integer.toString(ccs.getHeight()));
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getID() != ActionEvent.ACTION_PERFORMED)
            return;

        /* Cancel Button */
        if (e.getSource().equals(jbnCancel)) {
            this.dispose();
        }
        /* OK Button */
        else if(e.getSource().equals(jbnOK)) {
            try {
                screenshot(jtfFilePath.getText(), 
                        Integer.parseInt(jtfWidth.getText()),
                        Integer.parseInt(jtfHeight.getText()));
                this.dispose();
            } catch (Exception ex) {
                String msg = "Error: width/height should only contain numbers.";
                JOptionPane.showMessageDialog(new JFrame(), msg, "Error: Width/Height",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        /* Browser Button */
        else if(e.getSource().equals(jbnBrowse)) {
            JFileChooser jfc = new JFileChooser();
            jfc.setSelectedFile(new File("screenshot.png"));

            int returnVal = jfc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File path = jfc.getSelectedFile();
                jtfFilePath.setText(path.getAbsolutePath());
            }
        }
        /* Resolution ComboBox */
        else if(e.getSource().equals(jcResolutionList)) {
            rt = (ResolutionType) jcResolutionList.getSelectedItem();
            if(rt.equals(ResolutionType.DEFAULT)) {
                jtfWidth.setText(Integer.toString(ccs.getWidth()));
                jtfHeight.setText(Integer.toString(ccs.getHeight()));
            } else {
                jtfWidth.setText(Integer.toString(rt.getWidth()));
                jtfHeight.setText(Integer.toString(rt.getHeight()));
            }
        }

    }



    /**
     * This function takes screenshot of the CCSystem, and saves it as .png file.
     * 
     * @param filePath where the files should be saved.
     * @param width the width of the image to be saved.
     * @param height the height of the image to be saved.
     */
    public void screenshot(String filePath, int width, int height) {
        /* Need this to change the window size back. */
        int orgWidth = ccs.getWidth();
        int orgHeight = ccs.getHeight();

        
        ccs.setSize(width, height);

        /* Make image of CCSystem */
        BufferedImage img = new BufferedImage(ccs.getWidth(), 
                ccs.getHeight(), 
                BufferedImage.TYPE_INT_ARGB);
        Graphics gx = img.getGraphics();
        ccs.paint(gx);

        /* Save image */
        try {
            String filename;
            if(!filePath.toLowerCase().endsWith(".png"))
                filename = filePath + ".png";
            else 
                filename = filePath;
            File file = new File(filename);
            ImageIO.write(img, "png", file);
        } catch (IOException e) {

        }

        /* Change back to original window size. */
        ccs.setSize(orgWidth, orgHeight);
    }
}
