package mvc.view;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import mvc.AbstractViewPanel;
import mvc.control.Action;
import mvc.control.ActionManager;
import mvc.control.ImageBatchController;
import mvc.model.ImageBatch;
import mvc.model.ImageBatch.DocumentBehavior;
import mvc.model.ImageBatches;
import start.DocCrop;

import common.Configuration;
import common.Document.DocumentType;
import common.FileUtilities;
import common.ImageBatchSettings;
import common.SimplePrinter;

@SuppressWarnings("serial")
public class LeftPane extends AbstractViewPanel {

	/**
	 * Controller, used for communicating with the connected models
	 */
	private ImageBatchController controller;

	// Resource bundle used for application localization
	private ResourceBundle rBundle = DocCrop.rBundle;
	// Action manager, coordinates the actions for the various components
	private ActionManager actionManager = null;
	// Components of the view
	private JButton chooseButton = null;
	private JButton runButton = null;
	private JButton abortButton = null;
	private JButton previewButton = null;
	private JButton clearButton = null;
	private JFileChooser fc = null;
	// File extension visible in the file chooser
	private String[] fileExtensions = { "jpg", "jpeg", "png", "tif", "tiff",
			"bmp" };

	/**
	 * Create LeftPane. The controller is automatically connected to this view,
	 * so explicit code for connecting this view is neccesary
	 * 
	 * @param controller
	 *            A controller connected with a suitable model (
	 *            <code>ImageBatches</code>, or a subclass of it.)
	 */
	public LeftPane(ImageBatchController controller) {
		super();
		controller.addView(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setMaximumSize(new Dimension(100, Short.MAX_VALUE));
		setBackground(new Color(200, 200, 200));
		actionManager = new ActionManager(this, rBundle);
		this.controller = controller;
		Configuration.loadApplicationProperties();
		// Set filechooser start path and file filter
		fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "JPEG and PNG ";
			}

			@Override
			public boolean accept(File f) {
				String path = f.getAbsolutePath().toLowerCase();
				if (f.isDirectory())
					return true;
				for (int i = 0; i < fileExtensions.length; i++) {
					String extension = fileExtensions[i];
					if (path.endsWith(extension)
							&& (path.charAt(path.length() - extension.length()
									- 1) == '.')) {
						return true;
					}
				}
				return false;
			}
		});
		fc.setMultiSelectionEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		// Connect actions to the buttons and set their default state
		chooseButton = new JButton(actionManager.getAction("chooseFile"));
		chooseButton.setMaximumSize(new Dimension(168, 30));
		runButton = new JButton(actionManager.getAction("run"));
		runButton.setMaximumSize(new Dimension(168, 30));
		abortButton = new JButton(actionManager.getAction("abort"));
		abortButton.setMaximumSize(new Dimension(168, 30));
		previewButton = new JButton(
				actionManager.getAction("setPreviewVisibility"));
		previewButton.setMaximumSize(new Dimension(168, 30));
		previewButton.setPreferredSize(new Dimension(168, 30));

		String showPreview = Configuration
				.getProperty("settings.program.preview.show");
		if (showPreview.equals("TRUE")) {
			isPreviewVisible = true;
			previewButton.setText(rBundle
					.getString("mvc.view.LeftPane.setPreviewVisibility.hide"));
		} else {
			isPreviewVisible = false;
			previewButton.setText(rBundle
					.getString("mvc.view.LeftPane.setPreviewVisibility.show"));
		}

		clearButton = new JButton(actionManager.getAction("clear"));
		clearButton.setMaximumSize(new Dimension(168, 30));

		// Set abort button disabled at start
		actionManager.getAction("abort").setEnabled(false);
		actionManager.getAction("run").setEnabled(false);
		actionManager.getAction("clear").setEnabled(false);

		add(chooseButton);
		add(runButton);
		add(abortButton);
		add(clearButton);
		add(previewButton);
	}

	/**
	 * This method is called by the supplied controller, when any of the
	 * connected models are updated. Only <code>PropertyChangeEvents</code>
	 * related to this view needs to be taken care of.
	 */
	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {

		// If the documents have been updated. This occurs after document
		// identification
		if (evt.getPropertyName().equals(ImageBatchController.NEW_DOCUMENTS)) {
			try {
				FileUtilities.printToFile((ImageBatches) evt.getNewValue(),
						new SimplePrinter());
			} catch (FileNotFoundException e) {
				JOptionPane
						.showMessageDialog(
								this,
								rBundle.getString("error.message.properties_not_found"));
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
						rBundle.getString("error.message.file_not_created"));
				e.printStackTrace();
			}
		}

	}

	/**
	 * Thread for running analysis on batches
	 * 
	 * @author Tomas
	 * 
	 */
	private class ImageThread extends Thread {
		@Override
		public void run() {
			try {
			controller.AnalyzeBatches();
			} catch(Exception e) {
				JOptionPane.showMessageDialog(runButton, e.getMessage(),"Fel",JOptionPane.ERROR_MESSAGE);
			}
			actionManager.getAction("chooseFile").setEnabled(true);
			actionManager.getAction("run").setEnabled(false);
			actionManager.getAction("abort").setEnabled(false);
			actionManager.getAction("clear").setEnabled(true);	
		}

		public void stopRequest() {
			controller.stopRequest();
			controller.reset();
		}
	};

	ImageThread bkgThread;

	/**
	 * Start cropping process
	 */
	@Action
	public void run() {
		AbstractAction chooseFile = actionManager.getAction("chooseFile");
		chooseFile.setEnabled(false);
		actionManager.getAction("run").setEnabled(false);
		actionManager.getAction("clear").setEnabled(false);
		actionManager.getAction("abort").setEnabled(true);
		bkgThread = new ImageThread();
		bkgThread.setDaemon(true);
		bkgThread.start();
	}

	/**
	 * Abort cropping process
	 */
	@Action
	public void abort() {
		AbstractAction chooseFile = actionManager.getAction("chooseFile");
		chooseFile.setEnabled(true);
		AbstractAction run = actionManager.getAction("run");
		run.setEnabled(true);
		AbstractAction abort = actionManager.getAction("abort");
		abort.setEnabled(false);
		bkgThread.interrupt();
		bkgThread.stopRequest();

	}

	/**
	 * Show file chooser, and retrieve the information
	 */
	@Action
	public void chooseFile() {
		Configuration.loadApplicationProperties();
		fc.setCurrentDirectory(new File(Configuration
				.getProperty("settings.program.files.recentlyChosen")));
		int returnVal = fc.showOpenDialog(this);
		Configuration.setProperty("settings.program.files.recentlyChosen", fc
				.getCurrentDirectory().getPath());
		Configuration.saveApplicationProperties();
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Get the selected files/batches and update the model

			File[] imgFiles = fc.getSelectedFiles();
			boolean isValid = true;
			for (File batch : imgFiles) {
				if (batch.exists()) {
				} else {
					isValid = false;
					break;
				}
			}
			if (isValid && imgFiles.length > 0) {
				ImageBatch[] batch = toBatches(imgFiles);

				// Check that the selected folders indeed contains images
				boolean containsImages = true;
				for (int i = 0; i < batch.length; i++) {
					if (batch[i].getNumberOfFiles() == 0) {
						containsImages = false;
					}
				}
				// If all folders contains images, update batches, if not, show
				// error message
				if (containsImages) {
					controller.changeBatches(batch);
					actionManager.getAction("run").setEnabled(true);
					actionManager.getAction("clear").setEnabled(true);

				} else {
					JOptionPane.showMessageDialog(this,
							rBundle.getString("error.message.empty_batch"),
							rBundle.getString("error.message.file_error"),
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this,
						rBundle.getString("mvc.view.CenterPane.errorMessage"),
						rBundle.getString("mvc.view.CenterPane.fileError"),
						JOptionPane.ERROR_MESSAGE);

			}
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}

	@Action
	public void clear() {
		controller.clearBatches();
		actionManager.getAction("run").setEnabled(false);
		actionManager.getAction("abort").setEnabled(false);
		actionManager.getAction("chooseFile").setEnabled(true);
		actionManager.getAction("clear").setEnabled(false);
	}

	private boolean isPreviewVisible;

	/**
	 * Switch between showing/hiding the preview pane
	 */
	@Action
	public void setPreviewVisibility() {
		if (isPreviewVisible) {
			isPreviewVisible = false;
			previewButton.setText(rBundle
					.getString("mvc.view.LeftPane.setPreviewVisibility.show"));
		} else {
			isPreviewVisible = true;
			previewButton.setText(rBundle
					.getString("mvc.view.LeftPane.setPreviewVisibility.hide"));
		}
		controller.setPreviewVisibility(isPreviewVisible);
	}

	private ImageBatch[] toBatches(File[] imgFiles) {
		Configuration.loadApplicationProperties();
		Properties prop = Configuration.getApplicationProperties();
		DocumentType type;
		DocumentBehavior behaviour;
		int paddingWidth, paddingHeight;

		if (prop.getProperty("settings.program.documentType").equals("SINGLE"))
			type = DocumentType.SINGLE_PAGE;
		else
			type = DocumentType.DOUBLE_PAGE;
		if (prop.getProperty("settings.program.documentBehaviour").equals(
				"SIMPLE"))
			behaviour = DocumentBehavior.SIMPLE;
		else
			behaviour = DocumentBehavior.COMPLEX;

		paddingWidth = Integer.parseInt(prop
				.getProperty("settings.program.padding.width"));
		paddingHeight = Integer.parseInt(prop
				.getProperty("settings.program.padding.height"));
		ImageBatchSettings settings = new ImageBatchSettings(paddingWidth,
				paddingHeight, behaviour, type, fileExtensions);

		ImageBatch[] batches = new ImageBatch[imgFiles.length];
		for (int i = 0; i < batches.length; i++) {
			batches[i] = new ImageBatch(imgFiles[i], settings);
		}
		return batches;
	}

}
