package mvc.control;

import mvc.AbstractController;
import mvc.model.ImageBatch;

/**
 * This controller implements the required methods and provides the properties
 * necessary to update the current set of batches. Each of the methods in this
 * class can be called upon by the views to update the state of the registered
 * models.
 * 
 * @author Tomas Toss
 */
public class ImageBatchController extends AbstractController {

	/**
	 * A new set of batches has been chosen.
	 */
	public static final String NEW_BATCHES = "Batches";
	/**
	 * Documents of a model has been updated
	 */
	public static final String NEW_DOCUMENTS = "Documents";
	/**
	 * Indication that a new preview of the cropping is available
	 */
	public static final String PREVIEW = "Preview";
	/**
	 * Indication that progress of batch analysis has been made
	 */
	public static final String PROGRESS = "Progress";
	/**
	 * Indicate if the preview should be shown
	 */
	public static final String SHOW_PREVIEW = "PreviewVisibility";
	/**
	 * String used for calling the method analyzeBatches in the connected models
	 */
	private static final String ANALYZE_BATCHES = "analyzeBatches";
	/**
	 * String used for issuing a stop request, aborting current batch analysis
	 * of the connected models
	 */
	private static final String STOP_REQUEST = "StopRequest";
	
	private static final String CLEAR_BATCHES = "clearBatches";

	/**
	 * Reset progress
	 */
	private static final String RESET = "reset";
	/**
	 * Change the batches that should be processed
	 * 
	 * @param newBatches
	 *            The new set of batches
	 */
	public void changeBatches(ImageBatch[] newBatches) {
		setModelProperty(NEW_BATCHES, newBatches);
	}

	public void AnalyzeBatches() {
		refreshModelProperty(ANALYZE_BATCHES);
	}

	public void stopRequest() {
		setModelProperty(STOP_REQUEST, true);
	}

	public void setPreviewVisibility(boolean isVisible) {
		setModelProperty(SHOW_PREVIEW, isVisible);
	}
	
	public void reset() {
		refreshModelProperty(RESET);
	}
	
	public void clearBatches() {
		refreshModelProperty(CLEAR_BATCHES);
	}
}
