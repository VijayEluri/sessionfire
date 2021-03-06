package com.sessionfive.animation;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.KeyFrames;
import org.jdesktop.animation.timing.interpolation.KeyTimes;
import org.jdesktop.animation.timing.interpolation.KeyValues;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import com.sessionfive.app.Display;
import com.sessionfive.core.AnimationStyle;
import com.sessionfive.core.Camera;
import com.sessionfive.core.CameraAnimator;
import com.sessionfive.core.Shape;

public class GoToAnimationStyle implements AnimationStyle {

	public static final String NAME = "Go To";

	@Override
	public CameraAnimator createForwardAnimator(Camera cameraStart, Camera cameraEnd,
			Display display, Shape endShape) {
		KeyValues<Camera> values = KeyValues.create(
				new CameraMoveEvaluator(), cameraStart, cameraEnd);
		KeyTimes times = new KeyTimes(0f, 1f);
		KeyFrames frames = new KeyFrames(values, times);
		PropertySetter ps = new PropertySetter(display, "camera", frames);

		Animator animator = new Animator(1, ps);
		animator.setStartDelay(0);

		return new CameraAnimator(animator, ps);
	}

	@Override
	public CameraAnimator createBackwardAnimator(Camera cameraStart,
			Camera cameraEnd, Display display, Shape endShape) {
		KeyValues<Camera> values = KeyValues.create(
				new CameraMoveEvaluator(), cameraStart, cameraEnd);
		KeyTimes times = new KeyTimes(0f, 1f);
		KeyFrames frames = new KeyFrames(values, times);
		PropertySetter ps = new PropertySetter(display, "camera", frames);

		Animator animator = new Animator(1, ps);
		animator.setStartDelay(0);

		return new CameraAnimator(animator, ps);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GoToAnimationStyle))
			return false;
		return toString().equals(obj.toString());
	}

}
