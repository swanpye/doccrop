package mvc.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import mvc.AbstractModel;
import mvc.control.ImageBatchController;

import common.Document;

/**
 * A model containing the complete set of image batches currently in use by the
 * program. When the model is updated, it fires events recognized by
 * {@link ImageBatchController}, which in its turn is responsible for updating
 * the connected views
 * 
 * 
 * @author Tomas Toss
 */
public class ImageBatches extends AbstractModel {
	private Collection<ImageBatch> batches;
	private Collection<Document> documents;

	/**
	 * Type of document. If the image contains images with only one page
	 * visible, use SINGLE_PAGE. IF the image contains images with both pages
	 * visible, use DOUBLE_PAGE
	 */
	public enum DocumentType {
		SINGLE_PAGE, DOUBLE_PAGE
	}

	/**
	 * The behavior of an image batch. If the position of the documents likely
	 * can be well approximated by a linear curve, use SIMPLE. For more complex
	 * behavior, use COMPLEX.
	 */
	public enum DocumentBehavoir {
		SIMPLE, COMPLEX
	}

	public ImageBatches(File[] batches) {
		this.batches = new ArrayList<ImageBatch>();
		for (int i = 0; i < batches.length; i++) {
			this.batches.add(new ImageBatch(this, batches[i], ".png"));
		}
	}

	public ImageBatches() {
		this.batches = new ArrayList<ImageBatch>();
	}

	/**
	 * Set the new set of batches
	 * 
	 * @param batches
	 *            The new set of batches
	 */
	public void setBatches(ImageBatch[] batches) {
		this.batches = new ArrayList<ImageBatch>(batches.length);
		for (ImageBatch b : batches)
			this.batches.add(b);
		firePropertyChange(ImageBatchController.NEW_BATCHES, null, batches);
	}

	public void updateProgress() {
		firePropertyChange(ImageBatchController.PROGRESS, "test", "new progess");
	}
	public ImageBatch[] getBatches() {
		return batches.toArray(new ImageBatch[batches.size()]);
	}
	
	public void setDocuments(Collection<Document> docs) {
		documents = docs;
	}
	
	public Collection<Document> getDocuments(){
		return documents;
	}

}
