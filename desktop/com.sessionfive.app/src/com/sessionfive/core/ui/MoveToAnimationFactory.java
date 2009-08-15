package com.sessionfive.core.ui;

import com.sessionfive.animation.MoveToAnimation;
import com.sessionfive.core.Animation;
import com.sessionfive.core.Focusable;
import com.sessionfive.core.Shape;

public class MoveToAnimationFactory implements AnimationFactory {

	@Override
	public Animation createAnimation(Focusable startShape, Shape endShape) {
		return new MoveToAnimation(startShape, endShape);
	}

	@Override
	public String getName() {
		return "Move To";
	}
	
	@Override
	public String toString() {
		return getName();
	}

}