package com.sessionfive.animation;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.KeyFrames;
import org.jdesktop.animation.timing.interpolation.KeyTimes;
import org.jdesktop.animation.timing.interpolation.KeyValues;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import com.sessionfive.app.Display;
import com.sessionfive.core.AnimationStyle;
import com.sessionfive.core.Camera;
import com.sessionfive.core.Shape;

public class GoToAnimation implements AnimationStyle {

	public static final String NAME = "Go To";

	@Override
	public Animator createForwardAnimator(Camera cameraStart, Camera cameraEnd,
			Display display, Shape endShape) {
		KeyValues<Camera> values = KeyValues.create(
				new EvaluatorCameraSetting(), cameraStart, cameraEnd);
		KeyTimes times = new KeyTimes(0f, 1f);
		KeyFrames frames = new KeyFrames(values, times);
		PropertySetter ps = new PropertySetter(display, "camera", frames);

		Animator animator = new Animator(1, ps);
		animator.setStartDelay(0);

		return animator;
	}

	@Override
	public Animator createBackwardAnimator(Camera cameraStart,
			Camera cameraEnd, Display display, Shape endShape) {
		KeyValues<Camera> values = KeyValues.create(
				new EvaluatorCameraSetting(), cameraStart, cameraEnd);
		KeyTimes times = new KeyTimes(0f, 1f);
		KeyFrames frames = new KeyFrames(values, times);
		PropertySetter ps = new PropertySetter(display, "camera", frames);

		Animator animator = new Animator(1, ps);
		animator.setStartDelay(0);

		return animator;
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
		if (!(obj instanceof GoToAnimation))
			return false;
		return toString().equals(obj.toString());
	}

}
