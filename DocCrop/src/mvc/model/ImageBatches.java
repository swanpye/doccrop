package mvc.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import mvc.AbstractModel;
import mvc.control.ImageBatchController;

import common.Document;
import common.ImageBatchSettings;

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
	private ArrayList<ImageBatch> batches;
	private Collection<Document> documents;
	private ImageBatchSettings settings;
	private int currentAnalyzedBatch;

	public ImageBatches(File[] batches, ImageBatchSettings settings) {
		this.settings = settings;
		this.batches = new ArrayList<ImageBatch>();
		currentAnalyzedBatch = 0;
		for (int i = 0; i < batches.length; i++) {
			this.batches.add(new ImageBatch(this, batches[i], settings));
		}
	}

	public ImageBatches() {
		this.batches = new ArrayList<ImageBatch>();
		settings = new ImageBatchSettings();
	}

	/**
	 * Set the new set of batches
	 * 
	 * @param batches
	 *            The new set of batches
	 */
	public void setBatches(ImageBatch[] batches) {
		for (int i = 0; i < batches.length; i++) {
			batches[i].setParent(this);
			this.batches.add(batches[i]);
		}
		firePropertyChange(ImageBatchController.NEW_BATCHES, null, this);
	}

	public void updateProgress() {
		firePropertyChange(ImageBatchController.PROGRESS, null, "new progess");
	}

	private BufferedImage oldPreview, newPreview;
	public void updatePreview(BufferedImage img) {
		oldPreview = newPreview;
		newPreview  = img;		
		firePropertyChange(ImageBatchController.PREVIEW, oldPreview, newPreview);
		
	}
	
	public ImageBatch[] getBatches() {
		return batches.toArray(new ImageBatch[batches.size()]);
	}

	public void setDocuments(Collection<Document> docs) {
		documents = docs;
	}

	public Collection<Document> getDocuments() {
		return documents;
	}

	public void analyzeBatches() {
		for (int i = currentAnalyzedBatch; i < batches.size(); i++) {
			currentAnalyzedBatch = i;
			if (!batches.get(i).analyzeBatches())
				return;
		}
		currentAnalyzedBatch++;
		firePropertyChange(ImageBatchController.NEW_DOCUMENTS, null, this);
	}

	public void setStopRequest(Boolean stopRequest) {
		batches.get(currentAnalyzedBatch).stopRequest();
	}

	public void setSettings(ImageBatchSettings settings) {
		this.settings = settings;
	}
	
	public ImageBatchSettings getSettings() {
		return settings;
	}
	
	public void setPreviewVisibility(Boolean isVisible) {
		 firePropertyChange(ImageBatchController.SHOW_PREVIEW, null, isVisible);
	}
	
	public void reset() {
		for(int i = 0; i < batches.size(); i++) {
			batches.get(i).setProgress(0);
		}
		oldPreview = newPreview = null;
		currentAnalyzedBatch = 0;
	}
	
	public void clearBatches() {
		reset();
		firePropertyChange(ImageBatchController.NEW_BATCHES, batches, null);
		batches = new ArrayList<ImageBatch>();
	}
}
