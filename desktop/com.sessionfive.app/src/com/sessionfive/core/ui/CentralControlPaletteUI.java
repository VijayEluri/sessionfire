package com.sessionfive.core.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.media.opengl.GLCanvas;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.explodingpixels.macwidgets.HudWidgetFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CentralControlPaletteUI {

	private final CentralControlPalette centralControlPalette;

	private TranslucentPalette window;
	private JButton choosePresentationButton;
	private JButton startPresentationButton;
	private JButton helpButton;
	private JComboBox layoutChoice;
	private JComboBox animationChoice;
	private JTextField layerText;
	private final GLCanvas canvas;

	private JSlider xRotationSlider;
	private JSlider yRotationSlider;
	private JSlider zRotationSlider;

	private List<TranslucentPalette> extensionPalettes;

	private JPanel subContentPane;

	boolean helpshown = false;

	private Collection<HelpWindow> helpWindows;

	public CentralControlPaletteUI(CentralControlPalette centralControlPalette, GLCanvas canvas) {
		this.centralControlPalette = centralControlPalette;
		this.canvas = canvas;
		Window windowAncestor = SwingUtilities.getWindowAncestor(canvas);
		window = new TranslucentPalette("Sessionfire - Central Control", false, windowAncestor);
		initComponents();
		window.pack();
		window.setLocation(100, 100);
		initExtensions();

		helpWindows = new HashSet<HelpWindow>();
	}

	public void show() {
		window.showPalette();

		TranslucentPalette previousWindow = window;
		for (TranslucentPalette palette : extensionPalettes) {
			palette.setLocation(previousWindow.getLocationOnScreen().x, previousWindow
					.getLocationOnScreen().y
					+ previousWindow.getSize().height);
			palette.setSize(previousWindow.getSize().width, palette.getSize().height);
			palette.showPalette();
			previousWindow = palette;
		}
	}

	public void setStatus(String status) {
		window.setStatus(status);
	}

	private void initComponents() {
		JComponent contentPane = (JComponent) window.getEmbeddedContentPane();
		contentPane.setLayout(new BorderLayout());

		FormLayout layout = new FormLayout(
				"fill:pref:grow", // columns
				"pref, 3dlu, pref, 1dlu, pref, 6dlu, pref, 3dlu, pref, 6dlu, pref, 6dlu, pref, 6dlu, pref, 0dlu, pref, 0dlu, pref, 0dlu, pref"); // rows

		CellConstraints cc = new CellConstraints();
		subContentPane = new JPanel(layout);
		subContentPane.setOpaque(false);

		subContentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.add(subContentPane, BorderLayout.NORTH);

		choosePresentationButton = HudWidgetFactory.createHudButton("Choose Presentation...");
		choosePresentationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				centralControlPalette.choosePresentation(canvas, (Layouter) layoutChoice
						.getSelectedItem(), (AnimationFactory) animationChoice.getSelectedItem());

				centralControlPalette.setRotation(xRotationSlider.getValue(), yRotationSlider
						.getValue(), zRotationSlider.getValue());
			}
		});
		subContentPane.add(choosePresentationButton, cc.xy(1, 1));

		startPresentationButton = HudWidgetFactory.createHudButton("Start Presentation");

		startPresentationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				centralControlPalette.startPresentation();
			}
		});
		subContentPane.add(startPresentationButton, cc.xy(1, 3));
		//subContentPane.add(HelpLabelFactory.createHelpLabel("Press ESC or F11 to switch back"), cc.xy(1, 5));

		DefaultComboBoxModel layoutModel = new DefaultComboBoxModel();
		Layouter[] allLayouter = centralControlPalette.getLayouter();
		for (Layouter layouter : allLayouter) {
			layoutModel.addElement(layouter);
		}
		layoutChoice = HudWidgetFactory.createHudComboBox(layoutModel);
		layoutChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selectedLayouter = layoutChoice.getSelectedItem();
				if (selectedLayouter != null) {
					centralControlPalette.changeLayout((Layouter) selectedLayouter);
				}
			}
		});
		subContentPane.add(layoutChoice, cc.xy(1, 7));

		DefaultComboBoxModel animationModel = new DefaultComboBoxModel();
		AnimationFactory[] animationFactories = centralControlPalette.getAnimators();
		for (AnimationFactory animationFactory : animationFactories) {
			animationModel.addElement(animationFactory);
		}
		animationChoice = HudWidgetFactory.createHudComboBox(animationModel);
		animationChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selectedAnimation = animationChoice.getSelectedItem();
				if (selectedAnimation != null) {
					centralControlPalette.changeAnimation((AnimationFactory) selectedAnimation);
				}
			}
		});
		// Workaround to prevent Classcast Exception in apple laf
		animationChoice.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				char keyChar = e.getKeyChar();
				if (keyChar == ' ') {
					e.consume();
				}
			}
		});
		subContentPane.add(animationChoice, cc.xy(1, 9));

		JButton backgroundChooser = HudWidgetFactory.createHudButton("Choose Background Color...");
		backgroundChooser.setMaximumSize(new Dimension(10, 5));
		subContentPane.add(backgroundChooser, cc.xy(1, 11));
		backgroundChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseBackground();
			}
		});

		layerText = HudWidgetFactory.createHudTextField(centralControlPalette.getLayerText());
		subContentPane.add(layerText, cc.xy(1, 13));
		layerText.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				centralControlPalette.setLayerText(layerText.getText());
			}

			public void insertUpdate(DocumentEvent e) {
				centralControlPalette.setLayerText(layerText.getText());
			}

			public void changedUpdate(DocumentEvent e) {
				centralControlPalette.setLayerText(layerText.getText());
			}
		});

		xRotationSlider = new JSlider(0, 360, 0);
		yRotationSlider = new JSlider(0, 360, 0);
		zRotationSlider = new JSlider(0, 360, 0);
		subContentPane.add(xRotationSlider, cc.xy(1, 15));
		subContentPane.add(yRotationSlider, cc.xy(1, 17));
		subContentPane.add(zRotationSlider, cc.xy(1, 19));

		ChangeListener rotationSliderListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				centralControlPalette.setRotation(xRotationSlider.getValue(), yRotationSlider
						.getValue(), zRotationSlider.getValue());
			}
		};

		xRotationSlider.addChangeListener(rotationSliderListener);
		yRotationSlider.addChangeListener(rotationSliderListener);
		zRotationSlider.addChangeListener(rotationSliderListener);

		helpButton = HudWidgetFactory.createHudButton("?");
		subContentPane.add(helpButton, cc.xy(1, 21));

		helpButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!helpshown) {
					showhelp();
				}
			}
		});

		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				if (event instanceof MouseEvent) {
					MouseEvent mevent = (MouseEvent) event;
					if (mevent.getClickCount() > 0 && helpshown) {
						hidehelp();
						mevent.consume();
					}

				}
			}
		}, AWTEvent.MOUSE_EVENT_MASK);

	}

	protected void initExtensions() {
		extensionPalettes = new ArrayList<TranslucentPalette>();

		PanelExtension[] extensions = centralControlPalette.getExtensionPanels();

		for (PanelExtension panelExtension : extensions) {
			JPanel panelToEmbed = panelExtension.getPanel();
			if (panelToEmbed != null) {
				TranslucentPalette palette = new TranslucentPalette(panelExtension.getName(),
						false, (Window) window.getParent());
				JComponent contentPane = (JComponent) palette.getEmbeddedContentPane();
				contentPane.setLayout(new BorderLayout());

				contentPane.add(panelToEmbed, BorderLayout.NORTH);

				palette.pack();
				extensionPalettes.add(palette);
			}
		}
	}

	private void showhelp() {
		helpWindows.add(new HelpWindow(choosePresentationButton, HelpWindowPosition.ABOVE,
				"Select your presentation as an set", "or an folder of images"));
		helpWindows.add(new HelpWindow(startPresentationButton, HelpWindowPosition.BELOW,
				"Press to start your presentation", "and press ESC or F11 to switch back"));
		helpWindows.add(new HelpWindow(animationChoice, HelpWindowPosition.ABOVE,
				"Use these controls to select an animation", "and an layout of your shapes"));
		helpWindows.add(new HelpWindow(yRotationSlider, HelpWindowPosition.ABOVE,
				"Use these sliders to control", "the X,Y and Z axis of your shapes"));
		helpWindows.add(new HelpWindow(helpButton, HelpWindowPosition.NO_ARROW,
				"Navigation: Use the arrow ... png", "...png"));
		helpshown = true;
	}

	private void hidehelp() {
		for (HelpWindow window : helpWindows) {
			window.hideHoverWindow(new Runnable() {
				@Override
				public void run() {	
					helpshown = false;
				}
			});
			window = null;
		}
		helpWindows = new HashSet<HelpWindow>();
	}

	protected void chooseBackground() {
		Color newColor = JColorChooser.showDialog(window, "Choose Background Color",
				centralControlPalette.getBackgroundColor());

		if (newColor != null) {
			centralControlPalette.setBackgroundColor(newColor);
		}

	}

}
