package com.sessionfive.core.ui;

import java.awt.Color;
import java.io.File;
import java.util.Iterator;

import javax.media.opengl.GLCanvas;

import com.sessionfive.animation.AnimationController;
import com.sessionfive.animation.GoToAnimationStyle;
import com.sessionfive.animation.MoveToAnimationStyle;
import com.sessionfive.animation.ZoomOutZoomInAnimationStyle;
import com.sessionfive.app.SelectionService;
import com.sessionfive.app.SessionFiveApplication;
import com.sessionfive.core.AnimationStep;
import com.sessionfive.core.AnimationStyle;
import com.sessionfive.core.Presentation;
import com.sessionfive.core.Shape;

public class CentralControlPalette {

	private final Presentation presentation;
	private final AnimationController animationController;
	private final RotationView rotationView;

	public CentralControlPalette(Presentation presentation,
			AnimationController animationController, SelectionService selectionService) {
		this.presentation = presentation;
		this.animationController = animationController;
		this.rotationView = new RotationView(selectionService);
	}

	public void show() {
	}

	public void choosePresentation(GLCanvas canvas) {
		PresentationLoader loader = new PresentationLoader();
		loader.loadPresentation(presentation, canvas, getLayouter(),
				getAnimationStyles(), getAnimationPathLayouter());

		canvas.requestFocus();
	}

	public void loadPresentation(File[] files, GLCanvas canvas) {
		PresentationLoader loader = new PresentationLoader();
		loader.loadPresentation(presentation, canvas, getLayouter(),
				getAnimationStyles(), getAnimationPathLayouter(), files);

		canvas.requestFocus();
	}

	public void savePresentation(GLCanvas canvas) {
		PresentationLoader loader = new PresentationLoader();
		loader.savePresentation(presentation, canvas);
		
//		new JSONOutput().store(presentation);
	}

	public void startPresentation() {
		if (!SessionFiveApplication.getInstance().isFullScreenShowing()) {
			SessionFiveApplication.getInstance().switchFullScreen();
		}
	}

	public void changeLayout(Layouter layouter) {
		Shape focussedShape = animationController.getLastFocussedShape();

		layouter.layout(presentation);
		// layouter.animate(presentation, animationStyle);

		presentation.setDefaultLayouter(layouter);
		presentation.resetStartCamera();
		animationController.readjustSmoothlyTo(focussedShape);
	}

	public void changeAnimationStyle(AnimationStyle animationStyle) {
		presentation.setDefaultAnimationStyle(animationStyle);
		
		AnimationStep animationStep = presentation.getFirstAnimationStep();
		setAnimationStyleRecursively(animationStep, animationStyle);
	}

	protected void setAnimationStyleRecursively(AnimationStep animationStep,
			AnimationStyle animationStyle) {
		while (animationStep != null) {
			animationStep.setStyle(animationStyle);
			setAnimationStyleRecursively(animationStep.getChild(), animationStyle);
			animationStep = animationStep.getNext();
		}
	}

	public void changeAnimationPath(AnimationPathLayouter pathLayouter,
			AnimationStyle animationStyle) {
		Shape focussedShape = animationController.getLastFocussedShape();

		presentation.setDefaultAnimationPathLayouter(pathLayouter);
		pathLayouter.layoutAnimationPath(presentation, animationStyle);
		animationController.readjustSmoothlyTo(focussedShape);
	}

	public void setBackgroundColor(Color newColor) {
		this.presentation.setBackgroundColor(newColor);
	}

	public Layouter[] getLayouter() {
		return new Layouter[] { new LineLayouter(), new TileLayouter(),
				new CircleLayouter(), new LineGroupingLayouter(),
				new SphereGroupingLayouter(), new ExplodingLineGroupingLayouter() };
	}

	public AnimationStyle[] getAnimationStyles() {
		return new AnimationStyle[] { new ZoomOutZoomInAnimationStyle(),
				new MoveToAnimationStyle(), new GoToAnimationStyle() };
	}

	public AnimationPathLayouter[] getAnimationPathLayouter() {
		return new AnimationPathLayouter[] { new LinearAnimationPathLayouter(),
				new GroupedAnimationPathLayouter() };
	}

	public void setLayerText(String text) {
		this.presentation.setLayerText(text);
	}

	public void setSpace(float value, Layouter layouter) {
		presentation.setSpace(value, layouter);
	}

	public void setReflectionEnabled(boolean reflectionEnabled) {
		presentation.setDefaultReflectionEnabled(reflectionEnabled);

		Iterator<Shape> iter = presentation.shapeIterator(true);
		while (iter.hasNext()) {
			Shape shape = iter.next();
			shape.setReflectionEnabled(reflectionEnabled);
		}
	}

	public void setFocusScale(float focusScale) {
		presentation.setDefaultFocusScale(focusScale);
		Iterator<Shape> iter = presentation.shapeIterator(true);
		while (iter.hasNext()) {
			Shape shape = iter.next();
			shape.setFocusScale(focusScale);
		}
		animationController.readjustDirectly();
	}
	
	public Style[] getStyles() {
		Style standardLine = new StyleImpl("Standard Line Style", new LineLayouter(), 0, 0, 0);
		Style timeLine = new StyleImpl("Timeline Style", new LineLayouter(), 0, 300, 0);
		
		return new Style[] {standardLine, timeLine};
	}
	
	public Style getCustomStyle() {
		return new StyleImpl("Custom Style - Use Expert Settings", null, 0, 0, 0);
	}

	public void setStyle(Style style) {
		if (!style.equals(getCustomStyle())) {
			this.changeLayout(style.getLayout());
			this.rotationView.setRotation(style.getRotationX(), style.getRotationY(), style.getRotationZ());
		}
	}

	public PanelExtension[] getExtensionPanels() {
		return new PanelExtensionLoader().loadExtensions(animationController,
				presentation);
	}

	public RotationView getRotationView() {
		return rotationView;
	}

	public Style getMatchingStyle() {
		Style found = getCustomStyle();
		Style[] styles = getStyles();
		for (int i = 0; i < styles.length; i++) {
			if (styles[i].matches(presentation)) {
				found = styles[i];
				break;
			}
		}
		
		return found;
	}

}
