package com.sessionfive.animation;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.KeyFrames;
import org.jdesktop.animation.timing.interpolation.KeyTimes;
import org.jdesktop.animation.timing.interpolation.KeyValues;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import com.sessionfive.app.Display;
import com.sessionfive.core.Animation;
import com.sessionfive.core.Shape;

public class ZoomOutZoomInAnimation implements Animation {

	private final Shape endShape;
	private final Shape startShape;

	public ZoomOutZoomInAnimation(final Shape startShape, final Shape endShape) {
		this.startShape = startShape;
		this.endShape = endShape;
	}

	@Override
	public Animator getForwardAnimation(Display display) {
		CameraSetting startSetting = display.getCameraSetting();

		float shapeX = endShape.getX();

		CameraSetting cameraMid = new CameraSetting(shapeX - 70f, -3.1f, 100f,
				shapeX - 10f, -3.1f, 0, startSetting.getUpX(), startSetting.getUpY(), startSetting.getUpZ());
		
		float rotationAngle = -endShape.getRotation();
		double rotationRadian = Math.toRadians(rotationAngle);
		
		double upX = Math.sin(rotationRadian);
		double upY = Math.cos(rotationRadian);
		double upZ = 0f;
		
		CameraSetting cameraEnd = new CameraSetting(shapeX + 22.5f, -3.1f, 45f,
				shapeX + 22.5f, -3.1f, 0, (float)upX, (float)upY, (float)upZ);

		KeyValues<CameraSetting> values2 = KeyValues.create(
				new EvaluatorCameraSetting(), startSetting, cameraMid,
				cameraEnd);
		KeyTimes times2 = new KeyTimes(0f, 0.5f, 1f);
		KeyFrames frames2 = new KeyFrames(values2, times2);
		PropertySetter ps2 = new PropertySetter(display, "cameraSetting",
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

		CameraSetting startSetting = display.getCameraSetting();

		float shapeX = endShape.getX();
		CameraSetting cameraMid = new CameraSetting(shapeX - 70f, -3.1f, 100f,
				shapeX - 10f, -3.1f, 0, startSetting.getUpX(), startSetting.getUpY(), startSetting.getUpZ());

		shapeX = startShape.getX();
		float rotationAngle = -startShape.getRotation();
		double rotationRadian = Math.toRadians(rotationAngle);
		
		double upX = Math.sin(rotationRadian);
		double upY = Math.cos(rotationRadian);
		double upZ = 0f;

		CameraSetting cameraEnd = new CameraSetting(shapeX + 22.5f, -3.1f, 45f,
				shapeX + 22.5f, -3.1f, 0, (float)upX, (float)upY, (float)upZ);

		KeyValues<CameraSetting> values2 = KeyValues.create(
				new EvaluatorCameraSetting(), startSetting, cameraMid,
				cameraEnd);
		KeyTimes times2 = new KeyTimes(0f, 0.5f, 1f);
		KeyFrames frames2 = new KeyFrames(values2, times2);
		PropertySetter ps2 = new PropertySetter(display, "cameraSetting",
				frames2);

		Animator animator = new Animator(1500, ps2);
		animator.setStartDelay(0);
		animator.setAcceleration(0.4f);
		animator.setDeceleration(0.4f);

		return animator;
	}

}
