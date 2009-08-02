package com.sessionfive.core.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

public class CentralControlPaletteUI {

	private final CentralControlPalette centralControlPalette;

	private TranslucentPalette window;
	private JButton choosePresentationButton;
	private JComboBox layoutChoice;
	private JComboBox animationChoice;
	private final Component canvas;

	public CentralControlPaletteUI(CentralControlPalette centralControlPalette,
			Component canvas) {
		this.centralControlPalette = centralControlPalette;
		this.canvas = canvas;
		window = new TranslucentPalette("Session Five - Central Control", false);
		initComponents();
		window.pack();
		window.setLocation(100, 100);
	}

	public void show() {
		window.showPalette();
	}

	public void setStatus(String status) {
		window.setStatus(status);
	}

	private void initComponents() {
		choosePresentationButton = new JButton("Choose Presentation...");
		choosePresentationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				centralControlPalette.choosePresentation(canvas,
						(Layouter) layoutChoice.getSelectedItem(),
						animationChoice.getSelectedItem());
			}
		});

		JComponent contentPane = (JComponent) window.getEmbeddedContentPane();
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.setLayout(new GridLayout(4, 1, 15, 15));
		contentPane.add(choosePresentationButton);

		DefaultComboBoxModel layoutModel = new DefaultComboBoxModel();
		Layouter[] allLayouter = centralControlPalette.getLayouter();
		for (Layouter layouter : allLayouter) {
			layoutModel.addElement(layouter);
		}
		layoutChoice = new JComboBox(layoutModel);
		layoutChoice.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				centralControlPalette.changeLayout((Layouter) layoutChoice
						.getSelectedItem());
			}
		});
		contentPane.add(layoutChoice);

		DefaultComboBoxModel animationModel = new DefaultComboBoxModel();
		animationModel.addElement("Zoom Out - Zoom In");
		animationModel.addElement("Move To");
		animationChoice = new JComboBox(animationModel);
		contentPane.add(animationChoice);

		JButton backgroundChooser = new JButton("Choose Background Color...");
		contentPane.add(backgroundChooser);
		backgroundChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseBackground();
			}
		});

	}

	protected void chooseBackground() {
		Color newColor = JColorChooser.showDialog(window,
				"Choose Background Color", centralControlPalette
						.getBackgroundColor());

		if (newColor != null) {
			centralControlPalette.setBackgroundColor(newColor);
		}
	}

}
