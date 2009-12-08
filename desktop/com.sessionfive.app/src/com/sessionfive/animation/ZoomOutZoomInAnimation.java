package com.sessionfive.animation;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.KeyFrames;
import org.jdesktop.animation.timing.interpolation.KeyTimes;
import org.jdesktop.animation.timing.interpolation.KeyValues;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import com.sessionfive.app.Display;
import com.sessionfive.core.Animation;
import com.sessionfive.core.Camera;
import com.sessionfive.core.Focusable;
import com.sessionfive.core.Shape;

public class ZoomOutZoomInAnimation implements Animation {

	public static final String NAME = "Zoom Out - Zoom In";

	private final Shape endShape;
	private final Focusable startShape;

	public ZoomOutZoomInAnimation(final Focusable startShape, final Shape endShape) {
		this.startShape = startShape;
		this.endShape = endShape;
	}

	@Override
	public Animator getForwardAnimation(Display display) {
		Camera startSetting = display.getCamera();
		Camera endSetting = endShape.getFocussedCamera();

		float shapeX = endShape.getX();

		float positionY = (startSetting.getLocation().getY() + endSetting.getLocation().getY()) / 2;
		float targetY = (startSetting.getTarget().getY() + endSetting.getTarget().getY()) / 2;

		Camera cameraMid = new Camera(shapeX - 70f, positionY, 100f,
				shapeX - 10f, targetY, 0, startSetting.getUp().getX(), startSetting.getUp().getY(), startSetting.getUp().getZ());

		KeyValues<Camera> values2 = KeyValues.create(
				new EvaluatorCameraSetting(), startSetting, cameraMid,
				endSetting);
		KeyTimes times2 = new KeyTimes(0f, 0.5f, 1f);
		KeyFrames frames2 = new KeyFrames(values2, times2);
		PropertySetter ps2 = new PropertySetter(display, "camera",
				frames2);

		Animator animator = new Animator(1500, ps2);
		animator.setStartDelay(0);
		animator.setAcceleration(0.4f);
		animator.setDeceleration(0.4f);

		return animator;
	}

	@Override
	public Animator getBackwardAnimation(Display display) {
		if (startShape == null)
			return null;

		Camera startSetting = display.getCamera();
		Camera endSetting = startShape.getFocussedCamera();

		float shapeX = endShape.getX();

		float positionY = (startSetting.getLocation().getY() + endSetting.getLocation().getY()) / 2;
		float targetY = (startSetting.getTarget().getY() + endSetting.getTarget().getY()) / 2;

		Camera cameraMid = new Camera(shapeX - 70f, positionY, 100f,
				shapeX - 10f, targetY, 0, startSetting.getUp().getX(), startSetting.getUp().getY(), startSetting.getUp().getZ());

		KeyValues<Camera> values2 = KeyValues.create(
				new EvaluatorCameraSetting(), startSetting, cameraMid,
				endSetting);
		KeyTimes times2 = new KeyTimes(0f, 0.5f, 1f);
		KeyFrames frames2 = new KeyFrames(values2, times2);
		PropertySetter ps2 = new PropertySetter(display, "camera",
				frames2);

		Animator animator = new Animator(1500, ps2);
		animator.setStartDelay(0);
		animator.setAcceleration(0.4f);
		animator.setDeceleration(0.4f);

		return animator;
	}

	@Override
	public void directlyGoTo(Display display) {
		Camera cameraEnd = endShape.getFocussedCamera();
		display.setCamera(cameraEnd);
	}

}
