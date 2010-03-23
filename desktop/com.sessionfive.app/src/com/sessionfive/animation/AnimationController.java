package com.sessionfive.animation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

import com.sessionfive.app.Display;
import com.sessionfive.core.AnimationStep;
import com.sessionfive.core.Camera;
import com.sessionfive.core.Presentation;
import com.sessionfive.core.Shape;
import com.sessionfive.core.ShapeFocusListener;

public class AnimationController {

	private Animator currentAnimator;

	private Display display;
	private Presentation presentation;
	private AnimationStep currentAnimationStep;

	private Set<ShapeFocusListener> focusListeners;
	
	public AnimationController() {
		this.focusListeners = new HashSet<ShapeFocusListener>();
	}
	
	public void init(Presentation presentation, Display display) {
		this.presentation = presentation;
		this.display = display;

		this.currentAnimationStep = null;
	}

	public boolean canGoForward() {
		if (currentAnimationStep == null) {
			return presentation.getTotalAnimationStepCount() > 0;
		} else if (currentAnimationStep.isAutoZoomEnabled()
				&& currentAnimationStep.hasChild()) {
			return true;
		} else {
			AnimationStep step = currentAnimationStep;
			while (step != null && !step.hasNext() && step.hasParent()) {
				step = step.getParent();
			}
			return step != null && step.hasNext();
		}
	}

	public boolean canGoBackward() {
		return currentAnimationStep != null;
	}

	public boolean canZoomIn() {
		return currentAnimationStep != null && currentAnimationStep.hasChild();
	}

	public boolean canZoomOut() {
		return currentAnimationStep != null && currentAnimationStep.hasParent();
	}

	public void forward() {
		if (!canGoForward()) {
			return;
		}

		if (canZoomIn() && currentAnimationStep.isAutoZoomEnabled()) {
			zoomIn();
		} else {
			Shape lastFocussedShape = currentAnimationStep != null ? currentAnimationStep.getFocussedShape() : null;
			
			if (currentAnimationStep == null) {
				currentAnimationStep = presentation.getFirstAnimationStep();
			} else {
				lastFocussedShape = currentAnimationStep.getFocussedShape();

				while (!currentAnimationStep.hasNext()
						&& currentAnimationStep.hasParent()) {
					currentAnimationStep = currentAnimationStep.getParent();
				}

				if (currentAnimationStep.hasNext()) {
					currentAnimationStep = currentAnimationStep.getNext();
				}
			}

			Shape nextFocussedShape = currentAnimationStep != null ? currentAnimationStep.getFocussedShape() : null;
			Animator animator = currentAnimationStep
					.getForwardAnimation(display);
			startFocusAnimator(lastFocussedShape, nextFocussedShape, animator);
		}
	}

	public void backward() {
		if (!canGoBackward()) {
			return;
		}
		
		Shape focussedShape = getLastFocussedShape();

		if (currentAnimationStep.hasPrevious()) {
			currentAnimationStep = currentAnimationStep.getPrevious();

			if (currentAnimationStep.isAutoZoomEnabled()) {
				while (currentAnimationStep.hasChild()) {
					currentAnimationStep = currentAnimationStep.getChild();
					while (currentAnimationStep.hasNext()) {
						currentAnimationStep = currentAnimationStep.getNext();
					}
				}
			}
		} else if (currentAnimationStep.hasParent()) {
			currentAnimationStep = currentAnimationStep.getParent();
		} else {
			currentAnimationStep = null;
		}
		
		Animator animator = null;
		if (currentAnimationStep != null) {
			animator = currentAnimationStep.getBackwardAnimation(display);
		} else {
			Camera cameraStart = display.getCamera();
			Camera cameraEnd = presentation.getStartCamera();
			animator = presentation.getDefaultAnimation().createBackwardAnimator(
					cameraStart, cameraEnd, display, focussedShape);
		}
		Shape nextFocussedShape = currentAnimationStep != null ? currentAnimationStep.getFocussedShape() : null;
		startFocusAnimator(focussedShape, nextFocussedShape, animator);
	}

	public void zoomIn() {
		if (!canZoomIn()) {
			return;
		}

		Shape lastFocussedShape = currentAnimationStep != null ? currentAnimationStep.getFocussedShape() : null;
		currentAnimationStep = currentAnimationStep.getChild();
		Animator animator = currentAnimationStep.getForwardAnimation(display);
		Shape nextFocussedShape = currentAnimationStep != null ? currentAnimationStep.getFocussedShape() : null;
		startFocusAnimator(lastFocussedShape, nextFocussedShape, animator);
	}

	public void zoomOut() {
		if (!canZoomOut()) {
			return;
		}

		Shape lastFocussedShape = currentAnimationStep != null ? currentAnimationStep.getFocussedShape() : null;
		currentAnimationStep = currentAnimationStep.getParent();
		Animator animator = currentAnimationStep.getForwardAnimation(display);
		Shape nextFocussedShape = currentAnimationStep != null ? currentAnimationStep.getFocussedShape() : null;
		startFocusAnimator(lastFocussedShape, nextFocussedShape, animator);
	}

	public Shape getLastFocussedShape() {
		return currentAnimationStep != null ? currentAnimationStep
				.getFocussedShape() : null;
	}

	public int getNumberOfKeyFrames() {
		return presentation.getTotalAnimationStepCount();
	}

	public void goToKeyframeNo(int keyframeNo) {
		if (keyframeNo < 0 || keyframeNo >= getNumberOfKeyFrames()) {
			return;
		}

		currentAnimationStep = presentation.getFirstAnimationStep();
		int counter = 0;
		while (counter < keyframeNo) {
			if (currentAnimationStep.hasChild()) {
				currentAnimationStep = currentAnimationStep.getChild();
			} else if (currentAnimationStep.hasNext()) {
				currentAnimationStep = currentAnimationStep.getNext();
			} else if (currentAnimationStep.hasParent()) {
				currentAnimationStep = currentAnimationStep.getParent();

				while (!currentAnimationStep.hasNext()
						&& currentAnimationStep.hasParent()) {
					currentAnimationStep = currentAnimationStep.getParent();
				}
				if (currentAnimationStep.hasNext()) {
					currentAnimationStep = currentAnimationStep.getNext();
				}
			}
			counter++;
		}

		Animator animator = currentAnimationStep.getForwardAnimation(display);
		startFocusAnimator(null, null, animator);
	}

	public void readjustSmoothlyTo(Shape focussedShape) {
		if (focussedShape == null) {
			reset();
		} else {

			currentAnimationStep = presentation.getFirstAnimationStep();
			while (currentAnimationStep != null
					&& currentAnimationStep.getFocussedShape() != focussedShape) {
				if (currentAnimationStep.hasChild()) {
					currentAnimationStep = currentAnimationStep.getChild();
				} else if (currentAnimationStep.hasNext()) {
					currentAnimationStep = currentAnimationStep.getNext();
				} else if (currentAnimationStep.hasParent()) {
					currentAnimationStep = currentAnimationStep.getParent();

					while (!currentAnimationStep.hasNext()
							&& currentAnimationStep.hasParent()) {
						currentAnimationStep = currentAnimationStep.getParent();
					}
					if (currentAnimationStep.hasNext()) {
						currentAnimationStep = currentAnimationStep.getNext();
					}
				}
			}

			if (currentAnimationStep != null) {
				Animator animator = currentAnimationStep
						.getForwardAnimation(display);
				startFocusAnimator(null, null, animator);
			}
		}
	}

	public void readjustDirectly() {
		if (currentAnimationStep != null) {
			currentAnimationStep.directlyGoTo(display);
		} else {
			display.setCamera(presentation.getStartCamera());
		}
	}

	public void reset() {
		this.currentAnimationStep = null;

		Camera cameraStart = display.getCamera();
		Camera cameraEnd = presentation.getStartCamera();
		Animator animator = new MoveToAnimationStyle().createBackwardAnimator(
				cameraStart, cameraEnd, display, null);
		startFocusAnimator(null, null, animator);
	}

	protected void startFocusAnimator(final Shape lastFocussed, final Shape nextFocussed, final Animator animator) {
		List<TimingTarget> cancelTargets = null;
		if (currentAnimator != null && currentAnimator.isRunning()) {
			currentAnimator.cancel();
			if (lastFocussed != null) {
				cancelTargets = fireCancelFocussingShape(lastFocussed);
			}
		}

		currentAnimator = animator;
		if (currentAnimator != null) {
			addTimingTargets(currentAnimator, cancelTargets);
			if (nextFocussed != null) {
				List<TimingTarget> timings = fireStartsFocussingShape(nextFocussed);
				addTimingTargets(currentAnimator, timings);
			}
			
			currentAnimator.addTarget(new TimingTarget() {
				@Override
				public void timingEvent(float fraction) {
				}
				
				@Override
				public void repeat() {
				}
				
				@Override
				public void end() {
					fireFinishedFocussingShape(nextFocussed);
				}
				
				@Override
				public void begin() {
				}
			});
			currentAnimator.start();
		}
	}
	
	private void addTimingTargets(Animator animator, List<TimingTarget> targets) {
		if (targets != null && animator != null) {
			for (TimingTarget timingTarget : targets) {
				currentAnimator.addTarget(timingTarget);
			}
		}
	}
	
	public void addFocusListener(ShapeFocusListener action) {
		focusListeners.add(action);
	}

	public void removeFocusListener(ShapeFocusListener action) {
		focusListeners.remove(action);
	}
	
	protected List<TimingTarget> fireStartsFocussingShape(Shape shape) {
		List<TimingTarget> result = null;
		for (ShapeFocusListener listener : this.focusListeners) {
			TimingTarget timingTarget = listener.startsFocussing(shape);
			result = acculumateTimingTarget(result, timingTarget);
		}
		return result;
	}
	
	protected List<TimingTarget> fireCancelFocussingShape(Shape shape) {
		List<TimingTarget> result = null;
		for (ShapeFocusListener listener : this.focusListeners) {
			TimingTarget timingTarget = listener.cancelFocussing(shape);
			result = acculumateTimingTarget(result, timingTarget);
		}
		return result;
	}
	
	protected List<TimingTarget> fireFinishedFocussingShape(Shape shape) {
		List<TimingTarget> result = null;
		for (ShapeFocusListener listener : this.focusListeners) {
			TimingTarget timingTarget = listener.finishedFocussing(shape);
			result = acculumateTimingTarget(result, timingTarget);
		}
		return result;
	}
	
	private List<TimingTarget> acculumateTimingTarget(
			List<TimingTarget> result, TimingTarget timingTarget) {
		if (timingTarget != null) {
			if (result == null) {
				result = new ArrayList<TimingTarget>();
			}
			result.add(timingTarget);
		}
		return result;
	}

	/*
	 * protected void updateSelection() { Integer level1 =
	 * this.animationState.get(0); if (level1 == -1) { Shape[] allShapes =
	 * presentation.getShapes(LayerType.CAMERA_ANIMATED).toArray(new Shape[0]);
	 * SessionFiveApplication
	 * .getInstance().getSelectionService().setSelection(allShapes); } else {
	 * Shape currentFocussedShape =
	 * presentation.getShapes(LayerType.CAMERA_ANIMATED).get(level1);
	 * SessionFiveApplication
	 * .getInstance().getSelectionService().setSelection(new Shape[]
	 * {currentFocussedShape}); }
	 * 
	 * }
	 */

}
