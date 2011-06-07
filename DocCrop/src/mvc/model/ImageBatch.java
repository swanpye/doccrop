package mvc.model;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JProgressBar;

import common.Document;

/**
 * This class represents a batch of image files. A batch contains, in addition
 * to the image files, common properties of former.
 * 
 * @author Tomas Toss
 * 
 */
public class ImageBatch {
	/**
	 * A document batch can be classified either as single paged or as double
	 * paged
	 */
	public enum DocumentType {
		SINGLE, DOUBLE
	}

	/**
	 * A document batch have either simple of complex behavior. SIMPLE - Use
	 * this option when the variation of position and size of the individual
	 * documents in the batch is small. Also, the position of the spine of the
	 * document in the image could approximately be described as a linear curve
	 * with respect to position in the batch COMPLEX - Use this option when
	 * there is large variation in position of the documents in the batch. Or
	 * possibly if the position of the spine of the documents in the batch do
	 * not behave linearly with respect to position in the batch
	 */
	public enum DocumentBehavior {
		SIMPLE, COMPLEX
	}

	ImageBatches parent;
	private DocumentType docType;
	private DocumentBehavior docBehavior;
	private Document doc;
	private String batchPath;
	private File[] files;
	private BatchProgressBar progressBar;
	private int length;
	private int[] padding = { 0, 0 };

	public ImageBatch(ImageBatches parent, File batch, final String fileFilter) {
		this.parent = parent;
		this.batchPath = batch.getPath();
		// Set document behavior to simple and single as standard
		docBehavior = DocumentBehavior.SIMPLE;
		docType = DocumentType.SINGLE;
		// If the batch is a directory, add all the files in that directory
		// to the File[], else set the File[] only to contain batch
		if (batch.isDirectory()) {
			// Filter out all files except for the ones conforming with the
			// filter
			files = batch.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(fileFilter))
						return true;
					else
						return false;
				}
			});
		} else {
			if (batchPath.endsWith(fileFilter)) {
				files = new File[1];
				files[0] = batch;
			}
		}
		length = files.length;
		progressBar = new BatchProgressBar();
	}

	public File[] getFiles() {
		return files;
	}

	public int getNumberOfFiles() {
		return length;
	}

	public String getBatchPath() {
		return batchPath;
	}

	public void setDocType(DocumentType docType) {
		this.docType = docType;
	}

	public DocumentType getDocType() {
		return docType;
	}

	public DocumentBehavior getDocBehavior() {
		return docBehavior;
	}

	public void setDocBehavior(DocumentBehavior docBehavior) {
		this.docBehavior = docBehavior;
	}

	public void setProgress(int progress) {
		progressBar.setProgress(progress);
		parent.updateProgress();
	}

	public BatchProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * Get the amount of padding which should be added to the cropping rectangle
	 * after processing
	 * 
	 * @return The amount of padding: [x, y]
	 */
	public int[] getPadding() {
		return padding;
	}

	/**
	 * Set the amount of padding which should be added to the cropping rectangle
	 * 
	 * @param padding
	 *            The amount of padding: [x,y] -100 < x,y < 100
	 */
	public void setPadding(int[] padding) {
		if (padding[0] >= 100) {
			padding[0] = 99;
		} else if (padding[0] <= -100) {
			padding[0] = -99;
		}
		if (padding[1] >= 100) {
			padding[1] = 99;
		} else if (padding[1] <= -100) {
			padding[1] = -99;
		}
		this.padding = padding;
	}

	public void setDocument(Document doc) {
		this.doc = doc;
	}

	public Document getDocument() {
		return doc;
	}

	@SuppressWarnings("serial")
	public class BatchProgressBar extends JProgressBar {
		private int stop;

		public BatchProgressBar() {
			super(0, ImageBatch.this.getNumberOfFiles());

			stop = ImageBatch.this.getNumberOfFiles();
		}

		public void setProgress(int progress) {
			if(progress == stop) {
				setValue(stop);
				setStringPainted(true);
				String str = "Cropping completed";
				setString(str);
			} else{
				setValue(progress);
			}
		}
	}
}