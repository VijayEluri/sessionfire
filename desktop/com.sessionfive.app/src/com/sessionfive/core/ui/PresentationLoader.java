package com.sessionfive.core.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import javax.media.opengl.GLCanvas;
import javax.swing.JFileChooser;
import javax.swing.ProgressMonitor;

import com.sessionfive.core.Presentation;

public class PresentationLoader implements PropertyChangeListener {

	private PresentationLoaderTask task;
	private ProgressMonitor progressMonitor;

	public PresentationLoader() {
	}

	public void loadPresentation(Presentation presentation, GLCanvas canvas,
			Layouter layouter, AnimationFactory animationFactory) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);

		int showDialog = chooser.showDialog(null, "Choose Presentation");

		if (showDialog == JFileChooser.APPROVE_OPTION) {
			File[] files = getFiles(chooser);

			files = sortImageFiles(files);

			if (files.length > 0) {
				presentation.setPath(getPresentationPath(chooser));
				readFiles(presentation, files, canvas, layouter, animationFactory);
			}
		}
	}

	private File[] sortImageFiles(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File o1, File o2) {
				Long no1 = extractNumber(o1.getName());
				Long no2 = extractNumber(o2.getName());

				if (no1 != null && no2 != null) {
					return (int) (no1.longValue() - no2.longValue());
				}
				return (int) (o1.lastModified() - o2.lastModified());
			}
		});
		return files;
	}

	private File[] getFiles(JFileChooser chooser) {
		File[] selectedFiles = chooser.getSelectedFiles();
		if (selectedFiles != null) {
			if (selectedFiles.length == 1 && selectedFiles[0].isDirectory()) {
				return selectedFiles[0].listFiles();
			} else {
				return selectedFiles;
			}
		} else {
			return new File[0];
		}
	}
	
	private String getPresentationPath(JFileChooser chooser) {
		File[] selectedFiles = chooser.getSelectedFiles();
		if (selectedFiles != null) {
			if (selectedFiles.length == 1 && selectedFiles[0].isDirectory()) {
				return selectedFiles[0].getAbsolutePath();
			} else {
				return chooser.getCurrentDirectory().getAbsolutePath();
			}
		} else {
			return "";
		}
	}

	private Long extractNumber(String name) {
		StringBuilder digits = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			if (Character.isDigit(name.charAt(i))) {
				digits.append(name.charAt(i));
			}
		}

		if (digits.length() > 0) {
			try {
				long parsed = Long.parseLong(digits.toString());
				return parsed;
			}
			catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	private void readFiles(Presentation presentation, File[] files, GLCanvas canvas,
			Layouter layouter, AnimationFactory animationFactory) {
		ShapeExtensionCreator creator = new ShapeExtensionCreator();

		progressMonitor = new ProgressMonitor(null, "Loading Presentation", "",
				0, 100);
		progressMonitor.setProgress(0);

		task = new PresentationLoaderTask(files, creator, presentation, canvas, layouter, animationFactory);
		task.addPropertyChangeListener(this);
		task.execute();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressMonitor.setProgress(progress);
			String message = String.format("Completed %d%%.\n", progress);
			progressMonitor.setNote(message);
			if (progressMonitor.isCanceled() || task.isDone()) {
				if (progressMonitor.isCanceled()) {
					task.cancel(true);
				}
			}
		}
	}

	public void savePresentation(Presentation presentation, String layoutName, String animationName) {
	}

}
