package com.sessionfive.core.ui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.LinearGradientPaint;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;

import com.sun.awt.AWTUtilities;

public class TranslucentPalette extends JWindow {

	private static final long serialVersionUID = -2224207638362869833L;

	private final boolean toFade = true;
	private int currOpacity;
	private Timer fadeInTimer;
	private Timer fadeOutTimer;

	private JLabel titleLabel;

	private JButton closeButton;

	private JComponent titlePane;

	private JLabel resizeLabel;

	private JComponent bottomPane;

	private JPanel embeddedContentPane;

	public TranslucentPalette(String title, boolean closeable) {
		final JPanel contentPane = new JPanel() {
			private static final long serialVersionUID = -1505112600358149151L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g.create();

				int height = TranslucentPalette.this.getSize().height;
				int width = TranslucentPalette.this.getSize().width;

				Area box = new Area(new RoundRectangle2D.Double(0, 0, width,
						height, 20.0, 20.0));

				GraphicsConfiguration gc = g2d.getDeviceConfiguration();
				BufferedImage img = gc.createCompatibleImage(width, height,
						Transparency.TRANSLUCENT);
				Graphics2D g2 = img.createGraphics();

				g2.setComposite(AlphaComposite.Clear);
				g2.fillRect(0, 0, width, height);

				g2.setComposite(AlphaComposite.Src);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(0, 0, 0));
				g2.fill(box);
				g2.dispose();

				g2d.drawImage(img, 0, 0, this);
				g2d.dispose();
			}
		};
		contentPane.setDoubleBuffered(false);
		contentPane.setOpaque(false);
		contentPane.setLayout(new BorderLayout());
		this.setContentPane(contentPane);

		embeddedContentPane = new JPanel();
		embeddedContentPane.setOpaque(false);

		titleLabel = new JLabel(title, JLabel.CENTER);
		titleLabel.setForeground(Color.WHITE);
		closeButton = new JButton();
		closeButton.setIcon(new ImageIcon(this.getClass().getResource(
				"close.png")));
		// closeButton.setRolloverIcon(new
		// ImageIcon(this.getClass().getResource(
		// "close_hover.png")));
		// closeButton.setPressedIcon(new ImageIcon(this.getClass().getResource(
		// "close_pressed.png")));
		closeButton.setFocusable(false);
		closeButton.setFocusPainted(false);
		closeButton.setBorderPainted(false);
		closeButton.setContentAreaFilled(false);
		titlePane = createTitlePane();
		titlePane.setBorder(new EmptyBorder(5, 10, 5, 10));
		titlePane.setLayout(new BorderLayout());
		if (closeable) titlePane.add(closeButton, BorderLayout.WEST);
		titlePane.add(titleLabel, BorderLayout.CENTER);
		resizeLabel = new JLabel(new ImageIcon(this.getClass().getResource(
				"resize.png")));
		bottomPane = createBottomPane();

		contentPane.add(titlePane, BorderLayout.NORTH);
		contentPane.add(embeddedContentPane, BorderLayout.CENTER);
		contentPane.add(bottomPane, BorderLayout.SOUTH);

		MouseInputHandler handler = new MouseInputHandler(this);
		titlePane.addMouseListener(handler);
		titlePane.addMouseMotionListener(handler);
	}

	public JComponent getEmbeddedContentPane() {
		return embeddedContentPane;
	}

	public void showPalette() {
		if (this.fadeInTimer != null && this.fadeInTimer.isRunning()) {
			return;
		}

		if (this.fadeOutTimer != null && this.fadeOutTimer.isRunning()) {
			this.fadeOutTimer.stop();
		}

		if (this.toFade) {
			this.currOpacity = 0;
			AWTUtilities.setWindowOpacity(this, (currOpacity / 100.0f));
		}

		this.setVisible(true);
		// this.hoverWindow.pack();

		AWTUtilities.setWindowOpaque(this, false);

		if (this.toFade) {
			this.fadeInTimer = new Timer(50, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					currOpacity += 20;
					if (currOpacity <= 95) {
						AWTUtilities.setWindowOpacity(TranslucentPalette.this,
								(currOpacity / 100.0f));
						TranslucentPalette.this.getContentPane().repaint();
					} else {
						currOpacity = 95;
						fadeInTimer.stop();
					}
				}
			});
			this.fadeInTimer.setRepeats(true);
			this.fadeInTimer.start();
		}
	}

	public void hidePalette() {
		if (this.fadeOutTimer != null && this.fadeOutTimer.isRunning()) {
			return;
		}

		if (this.fadeInTimer != null && this.fadeInTimer.isRunning()) {
			this.fadeInTimer.stop();
		}

		if (this.toFade) {
			if (this.fadeInTimer.isRunning())
				this.fadeInTimer.stop();

			this.fadeOutTimer = new Timer(50, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					currOpacity -= 19;
					if (currOpacity >= 0) {
						AWTUtilities.setWindowOpacity(TranslucentPalette.this,
								(currOpacity / 100.0f));
						TranslucentPalette.this.getContentPane().repaint();
					} else {
						fadeOutTimer.stop();
						TranslucentPalette.this.setVisible(false);
						currOpacity = 0;
					}
				}
			});
			this.fadeOutTimer.setRepeats(true);
			this.fadeOutTimer.start();
		} else {
			this.setVisible(false);
		}
	}

	private JComponent createBottomPane() {
		JPanel result = new JPanel();
		result.setOpaque(false);
		result.setLayout(new FlowLayout(FlowLayout.RIGHT));
		result.add(resizeLabel);
		return result;
	}

	private JComponent createTitlePane() {
		JComponent result = new JComponent() {
			private static final long serialVersionUID = 5262821975356719994L;

			protected void paintComponent(Graphics g) {
				setOpaque(false);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				Composite old = g2.getComposite();
				g2.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.75f));

				LinearGradientPaint paint = new LinearGradientPaint(0, 0, 0,
						getHeight(), new float[] { .0f, .499f, .5f, 1.0f },
						new Color[] { new Color(0x858585), new Color(0x3c3c3c),
								new Color(0x2c2c2c), new Color(0x333334) });
				g2.setPaint(paint);
				Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(),
						getHeight(), 16, 16);
				g2.fill(shape);
				g2.setComposite(old);
				g2.dispose();
			}
		};
		return result;
	}
	
	private class MouseInputHandler implements MouseInputListener {
		
        private boolean isMovingWindow;
        private int dragOffsetX;
        private int dragOffsetY;
		private Window w;
        private static final int BORDER_DRAG_THICKNESS = 5;
        
        public MouseInputHandler(Window w) {
        	this.w = w;
        }

        public void mousePressed(MouseEvent ev) {
            Point dragWindowOffset = ev.getPoint();
            Point convertedDragWindowOffset = SwingUtilities.convertPoint(
                           w, dragWindowOffset, titlePane);

            if (titlePane.contains(convertedDragWindowOffset)) {
                if (dragWindowOffset.y >= BORDER_DRAG_THICKNESS
                        && dragWindowOffset.x >= BORDER_DRAG_THICKNESS
                        && dragWindowOffset.x < w.getWidth()
                            - BORDER_DRAG_THICKNESS) {
                    isMovingWindow = true;
                    dragOffsetX = dragWindowOffset.x;
                    dragOffsetY = dragWindowOffset.y;
                }
            }
            else {
                dragOffsetX = dragWindowOffset.x;
                dragOffsetY = dragWindowOffset.y;
            }
        }

        public void mouseReleased(MouseEvent ev) {
            isMovingWindow = false;
        }

        public void mouseDragged(MouseEvent ev) {
            if (isMovingWindow) {
                Point windowPt = MouseInfo.getPointerInfo().getLocation();
                windowPt.x = windowPt.x - dragOffsetX;
                windowPt.y = windowPt.y - dragOffsetY;
                w.setLocation(windowPt);
            }
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseMoved(MouseEvent e) {}
	}

}