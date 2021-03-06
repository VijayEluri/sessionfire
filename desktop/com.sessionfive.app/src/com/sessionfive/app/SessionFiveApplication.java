package com.sessionfive.app;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.imageio.ImageIO;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLException;
import javax.media.opengl.GLPbuffer;
import javax.swing.ImageIcon;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.sessionfive.animation.AnimationController;
import com.sessionfive.core.AnimationStep;
import com.sessionfive.core.Presentation;
import com.sessionfive.core.ui.CentralControlPalette;
import com.sessionfive.core.ui.CentralControlPaletteUI;
import com.sessionfive.core.ui.ExplodingGroupListener;
import com.sun.opengl.util.Screenshot;

public class SessionFiveApplication implements IApplication {

	private Frame fullScreenFrame;

	private GLCapabilities caps;
	private GLCanvas canvas;

	private MultiplexingKeyListener keyListener;
	private MultiplexingKeyListener globalKeyListener;
	private CameraMover cameraMover;

	private Display display;
	private Frame frame;

	private AnimationController animationController;
	private SelectionService selectionService;
	private Presentation presentation;

	private static SessionFiveApplication application;

	private CentralControlPalette centralControlPalette;
	private CentralControlPaletteUI centralControlPaletteUI;

	private DisplayRepaintManager displayRepaintManager;
	private DisplayRepaintManager fullScreenDisplayRepaintManager;

	public SessionFiveApplication() {
		application = this;
	}

	public static SessionFiveApplication getInstance() {
		return application;
	}

	public AnimationController getAnimationController() {
		return animationController;
	}
	
	public SelectionService getSelectionService() {
		return selectionService;
	}

	public Presentation getPresentation() {
		return presentation;
	}

	public Object start(final IApplicationContext context) throws Exception {
		final org.eclipse.swt.widgets.Display swtDisplay = new org.eclipse.swt.widgets.Display();
		swtDisplay.asyncExec(new Runnable() {
			@Override
			public void run() {
				start(swtDisplay);
				context.applicationRunning();
			}
		});
		while (!swtDisplay.isDisposed()) {
			if (!swtDisplay.readAndDispatch())
				swtDisplay.sleep();
		}

		return EXIT_OK;
	}

	public Object start(final org.eclipse.swt.widgets.Display swtDisplay) {
		frame = new Frame("Sessionfire - A New Kind of Presentation Tool");
		frame.setIconImage(new ImageIcon(this.getClass().getResource("sf16.png")).getImage());

		caps = new GLCapabilities();
		caps.setSampleBuffers(true);
		caps.setNumSamples(4);

		canvas = new GLCanvas(caps);
		presentation = new Presentation();
		display = new Display(presentation);
		animationController = new AnimationController();
		selectionService = new SelectionServiceImpl();

		displayRepaintManager = new DisplayRepaintManager(display, presentation, canvas);

		canvas.addGLEventListener(display);

		keyListener = new MultiplexingKeyListener();
		globalKeyListener = new MultiplexingKeyListener();
		
		cameraMover = new CameraMover(presentation, animationController);

		new KeyListenerExtensionReader().addKeyListenerExtensionsTo(keyListener);

		keyListener.addKeyListener(new NavigationKeyListener(animationController, this));

		globalKeyListener.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_F11) {
					switchFullScreen();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					if (isFullScreenShowing()) {
						switchFullScreen();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_O
						&& (e.getModifiers() & KeyEvent.META_MASK) != 0) {
					centralControlPalette.choosePresentation(canvas);
				} else if (e.getKeyCode() == KeyEvent.VK_S
						&& (e.getModifiers() & KeyEvent.META_MASK) != 0) {
					centralControlPalette.savePresentation(canvas);
				}
			}
		});

		canvas.addKeyListener(keyListener);
		frame.addKeyListener(keyListener);

		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				if (event instanceof KeyEvent) {
					KeyEvent keyevent = (KeyEvent) event;
					if (keyevent.getID() == KeyEvent.KEY_PRESSED) {
						globalKeyListener.keyPressed(keyevent);
					}
				}
			}
		}, AWTEvent.KEY_EVENT_MASK);

		
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				cameraMover.mouseMoved(e);
			}
		});
		
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				cameraMover.mouseClicked(e);
			}
		});
		
		frame.setLayout(new BorderLayout());

		frame.add(canvas, BorderLayout.CENTER);
		frame.setSize(900, 600);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				new Thread(new Runnable() {
					public void run() {
						displayRepaintManager.dispose();
						swtDisplay.asyncExec(new Runnable() {
							@Override
							public void run() {
								swtDisplay.dispose();
							}
						});
					}
				}).start();
			}
		});

		frame.setVisible(true);
		animationController.init(presentation, display);
		animationController.addFocusListener(new ExplodingGroupListener());
//		animationController.addFocusListener(new RedrawPerformanceListener(display));

		centralControlPalette = new CentralControlPalette(presentation, animationController, selectionService);
		centralControlPaletteUI = new CentralControlPaletteUI(centralControlPalette, presentation, canvas);
		centralControlPaletteUI.show();

		return EXIT_OK;
	}

	public void switchFullScreen() {
		if (fullScreenFrame == null) {
			fullScreenFrame = new Frame("Session Five");
			fullScreenFrame.setFocusable(true);
			fullScreenFrame.setResizable(false);
			fullScreenFrame.setSize(600, 500);
			fullScreenFrame.setUndecorated(true);
			fullScreenFrame.setLayout(new BorderLayout());

			final GLCanvas fullScreenCanvas = new GLCanvas(caps, null, canvas.getContext(), null);
			fullScreenCanvas.addGLEventListener(display);
			fullScreenCanvas.setFocusable(true);
			fullScreenDisplayRepaintManager = new DisplayRepaintManager(display, presentation,
					fullScreenCanvas);
			fullScreenFrame.add(fullScreenCanvas, BorderLayout.CENTER);

			fullScreenFrame.addKeyListener(keyListener);
			fullScreenCanvas.addKeyListener(keyListener);
			fullScreenFrame.addWindowListener(new WindowAdapter() {
				public void windowOpened(WindowEvent we) {
					fullScreenFrame.requestFocus();
				}
			});
			
			GraphicsDevice screenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice();
			screenDevice.setFullScreenWindow(fullScreenFrame);
		} else {
			GraphicsDevice screenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice();

			screenDevice.setFullScreenWindow(null);
			fullScreenFrame.setVisible(false);
			fullScreenFrame.dispose();
			fullScreenFrame = null;

			fullScreenDisplayRepaintManager.dispose();
			fullScreenDisplayRepaintManager = null;
			frame.toFront();
		}
	}

	public void stop() {
	}

	public GLContext getGLContext() {
		return canvas.getContext();
	}

	public boolean isFullScreenShowing() {
		return fullScreenFrame != null;
	}

	public byte[] getKeyFrame(int parsedNumber) {
		GLCapabilities caps = new GLCapabilities();
		caps.setSampleBuffers(true);
		caps.setNumSamples(2);
		caps.setDoubleBuffered(false);

		if (!GLDrawableFactory.getFactory().canCreateGLPbuffer()) {
			throw new GLException("Pbuffers not supported with this graphics card");
		}
		GLPbuffer pbuffer = GLDrawableFactory.getFactory().createGLPbuffer(caps, null, 512, 512,
				canvas.getContext());

		Display offscreenDisplay = new Display(presentation);

		boolean alpha = false;
		if (parsedNumber >= 0 && parsedNumber < presentation.getTotalAnimationStepCount()) {
			AnimationStep animationStep = presentation.getFirstAnimationStep();
			int counter = 0;
			while (counter < parsedNumber) {
				if (animationStep.hasChild()) {
					animationStep = animationStep.getChild();
				}
				else if (animationStep.hasNext()) {
					animationStep = animationStep.getNext();
				}
				else if (animationStep.hasParent()) {
					animationStep = animationStep.getParent();
					
					while (!animationStep.hasNext() && animationStep.hasParent()) {
						animationStep = animationStep.getParent();
					}
					if (animationStep.hasNext()) {
						animationStep = animationStep.getNext();
					}
				}
				counter++;
			}

			if (animationStep != null) {
				animationStep.directlyGoTo(offscreenDisplay);
				alpha = true;
			}
		} else {
			offscreenDisplay.setCamera(presentation.getStartCamera());
		}

		pbuffer.addGLEventListener(offscreenDisplay);

		KeyFrameCreator keyFrameCreator = new KeyFrameCreator(512, 512, alpha);
		FutureTask<byte[]> futureTask = new FutureTask<byte[]>(keyFrameCreator);
		offscreenDisplay.executeInDisplay(futureTask);
		pbuffer.display();

		byte[] result = null;
		try {
			result = futureTask.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		pbuffer.removeGLEventListener(offscreenDisplay);
		pbuffer.destroy();

		return result;
	}

	protected static class KeyFrameCreator implements Callable<byte[]> {

		private final int width;
		private final int height;
		private final boolean alpha;

		public KeyFrameCreator(int width, int height, boolean alpha) {
			this.width = width;
			this.height = height;
			this.alpha = alpha;
		}

		public byte[] call() throws Exception {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			try {
				BufferedImage image = Screenshot.readToBufferedImage(0, 0, width, height, alpha);
				if (!ImageIO.write(image, "png", bos)) {
					throw new IOException("Unsupported file format png");
				}
				return bos.toByteArray();
			} catch (GLException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}

	}

}
