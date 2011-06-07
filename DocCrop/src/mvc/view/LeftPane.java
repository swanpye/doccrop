package mvc.view;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mvc.AbstractViewPanel;
import mvc.control.Action;
import mvc.control.ActionManager;
import mvc.control.ImageBatchController;
import start.DocCrop;


@SuppressWarnings("serial")
public class LeftPane extends AbstractViewPanel {

	private ImageBatchController controller;
	private JButton batchButton = null;
	private JButton runButton = null;
	private JButton abortButton = null;
	private JFileChooser fc = null;
	private ResourceBundle rBundle = DocCrop.rBundle;
	private ActionManager actionManager = null;

	public LeftPane(ImageBatchController controller) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setMaximumSize(new Dimension(100, Short.MAX_VALUE));
		setBackground(new Color(200, 200, 200));
		actionManager = new ActionManager(this, rBundle);
		this.controller = controller;
		fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		batchButton = new JButton(actionManager.getAction("chooseFile"));
		batchButton.setMaximumSize(new Dimension(150, 30));
		runButton = new JButton(actionManager.getAction("run"));
		runButton.setMaximumSize(new Dimension(150, 30));
		abortButton = new JButton(actionManager.getAction("abort"));
		abortButton.setMaximumSize(new Dimension(150, 30));
		// Set abort button disabled at start
		actionManager.getAction("abort").setEnabled(false);
		add(batchButton);
		add(runButton);
		add(abortButton);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub

	}

	private class ImageThread extends Thread {
			@Override
			public void run() {
				System.out.println(controller.AnalyzeBatches());
			}
			
			public void stopRequest() {
				controller.stopRequest();
				this.interrupt();
			}
		};
	ImageThread bkgThread = new ImageThread();
	/**
	 * Start cropping process
	 */
	@Action
	public void run() {
		AbstractAction chooseFile = actionManager.getAction("chooseFile");
		chooseFile.setEnabled(false);
		AbstractAction run = actionManager.getAction("run");
		run.setEnabled(false);
		AbstractAction abort = actionManager.getAction("abort");
		abort.setEnabled(true);
		bkgThread.start();
	}

	@Action
	public void abort() {
		AbstractAction chooseFile = actionManager.getAction("chooseFile");
		chooseFile.setEnabled(true);
		AbstractAction run = actionManager.getAction("run");
		run.setEnabled(true);
		AbstractAction abort = actionManager.getAction("abort");
		abort.setEnabled(false);
		bkgThread.stopRequest();
	}

	/**
	 * Show file chooser, and retrieve the information
	 */
	@Action
	public void chooseFile() {
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Get the selected files/batches and update the model

			File[] batches = fc.getSelectedFiles();
			boolean isValid = true;
			for (File batch : batches) {
				if (batch.exists()) {
				} else {
					isValid = false;
					break;
				}
			}
			if (isValid)
				controller.changeBatches(batches);
			else {
				JOptionPane.showMessageDialog(this,
						rBundle.getString("mvc.view.CenterPane.errorMessage"),
						rBundle.getString("mvc.view.CenterPane.fileError"),
						JOptionPane.ERROR_MESSAGE);

			}
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}

}
