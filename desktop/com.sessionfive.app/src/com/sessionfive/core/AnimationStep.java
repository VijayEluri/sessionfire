package com.sessionfive.core;

import org.jdesktop.animation.timing.Animator;

import com.sessionfive.app.Display;

public class AnimationStep {

	private final Shape endShape;
	private final Focusable startShape;
	private AnimationStyle style;

	public AnimationStep(final Focusable startShape, final Shape endShape) {
		this.startShape = startShape;
		this.endShape = endShape;
		this.style = null;
	}
	
	public Focusable getStartShape() {
		return startShape;
	}
	
	public Focusable getEndShape() {
		return endShape;
	}
	
	public AnimationStyle getStyle() {
		return style;
	}
	
	public void setStyle(AnimationStyle style) {
		this.style = style;
	}

	public Animator getForwardAnimation(Display display) {
		if (style != null) {
			Camera cameraStart = display.getCamera();
			Camera cameraEnd = endShape.getFocussedCamera();
			return style.createForwardAnimator(cameraStart, cameraEnd, display, endShape);
		}
		return null;
	}

	public Animator getBackwardAnimation(Display display) {
		if (startShape == null || this.style == null)
			return null;
		
		Camera cameraStart = display.getCamera();
		Camera cameraEnd = startShape.getFocussedCamera();
		return style.createBackwardAnimator(cameraStart, cameraEnd, display, endShape);
	}

	public void directlyGoTo(Display display) {
		Camera cameraEnd = endShape.getFocussedCamera();
		display.setCamera(cameraEnd);
	}

}