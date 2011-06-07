package mvc.control;

import imageanalysis.DocumentIdentifier;
import imageanalysis.DocumentUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import mvc.AbstractController;
import mvc.model.ImageBatch;
import mvc.model.ImageBatches;

import common.Document;

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
	public static final String NEW_DOCUMENTS = "Documents";
	public static final String PROGRESS = "Progress";
	private ImageBatches currentBatches = null;
	private volatile boolean stopRequest = false;

	/**
	 * Change the batches that should be processed
	 * 
	 * @param newBatches
	 *            The new set of batches
	 */
	public void changeBatches(File[] newBatches) {
		ImageBatches batch = new ImageBatches(newBatches);
		addModel(batch);
		currentBatches = batch;
		setModelProperty(NEW_BATCHES, batch.getBatches());
	}

	public Collection<Document> AnalyzeBatches() {
		stopRequest = false;
		ImageBatches batches = currentBatches;
		final ImageBatch[] batch = batches.getBatches();

		Vector<Document> docs = new Vector<Document>();
		for (int i = 0; i < batch.length; i++) {
			File[] files = batch[i].getFiles();
			
			for (int j = 0; j < files.length; j++) {
				try {
					Thread.sleep(10);
				} catch(InterruptedException ie) {
					if(stopRequest == true)
					return null;
				}
				DocumentIdentifier docIdent = new DocumentIdentifier(
						files[j].getPath());
				try {
					docIdent.load();
				} catch (IOException e) {
					e.printStackTrace();
				}
				docs.add(docIdent.identify());
				batch[i].setProgress(j+1);
				
			}
		}
		setModelProperty(NEW_DOCUMENTS,
				DocumentUtilities.correctDocuments(docs));
		return DocumentUtilities.correctDocuments(docs);

	}
	
	public void stopRequest() {
		stopRequest = true;
	}
}
