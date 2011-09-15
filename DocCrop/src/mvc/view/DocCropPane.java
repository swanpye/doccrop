package mvc.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import common.Configuration;
import common.ImageUtilities;

import mvc.AbstractViewPanel;
import mvc.control.ActionManager;
import mvc.control.ImageBatchController;
import mvc.model.ImageBatches;
import start.DocCrop;

@SuppressWarnings("serial")
public class DocCropPane extends AbstractViewPanel {
	private ResourceBundle rBundle = DocCrop.rBundle;
	private ActionManager actionManager = null;
	private ImageBatchController controller;
	private ImageBatches model = new ImageBatches();
	private CenterPane centerPane = null;
	private LeftPane leftPane = null;
	static PropertyChangeListener propListener = null;
	private JFrame previewWindow = new JFrame();
	private boolean isPreviewVisible;

	public DocCropPane(ImageBatchController controller) {
		this.controller = controller;
		controller.addView(this);
		Configuration.loadApplicationProperties();
		String showPreview = Configuration.getProperty("settings.program.preview.show");
		if(showPreview.equals("TRUE")) {
			isPreviewVisible = true;
		} else {
			isPreviewVisible = false;
		}
		previewWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		previewWindow.setPreferredSize(new Dimension(400, 600));
		previewWindow.pack();
		// Set basic properties of the DocCropWindow
		setLayout(new BorderLayout());

		actionManager = new ActionManager(this, rBundle);
		// Set up controller, model and views.
		controller.addModel(model);
		leftPane = new LeftPane(controller);
		centerPane = new CenterPane(controller);

		add(leftPane, BorderLayout.LINE_START);
		add(centerPane, BorderLayout.CENTER);
		
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ImageBatchController.PREVIEW)) {
			BufferedImage copy = ImageUtilities.copyImage((BufferedImage) evt.getNewValue());
			final JLabel newCanvas = ImageUtilities
					.createCanvas(copy);
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					previewWindow.getContentPane().removeAll();
					previewWindow.getContentPane().add(newCanvas);
					newCanvas.revalidate();
					previewWindow.repaint();
				}
			});
			previewWindow.setVisible(isPreviewVisible);
		} else if(evt.getPropertyName().equals(ImageBatchController.SHOW_PREVIEW)) {
			isPreviewVisible = (Boolean) evt.getNewValue();
			previewWindow.setVisible(isPreviewVisible);
		}
	}
}