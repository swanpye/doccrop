package mvc.model;

import imageanalysis.DocumentIdentifier;
import imageanalysis.DocumentUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import common.Document;
import common.Document.DocumentType;
import common.ImageBatchSettings;
import common.ImageUtilities;

/**
 * This class represents a batch of image files. A batch contains, in addition
 * to the image files, common properties of former.
 * 
 * @author Tomas Toss
 * 
 */
public class ImageBatch {

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

	private volatile boolean stopRequest = false;
	ImageBatches parent;
	private DocumentType docType;
	private DocumentBehavior docBehavior;
	private Document[] docs;
	private String batchPath;
	private File[] files;
	private BatchProgressBar progressBar;
	private int length;
	private int[] padding = { 0, 0 };
	private ArrayList<String> fileSuffixes = new ArrayList<String>();

	public ImageBatch(File batch, ImageBatchSettings settings) {
		this(null, batch, settings);
	}

	public ImageBatch(ImageBatches parent, File batch,
			ImageBatchSettings settings) {
		this.parent = parent;
		// Set document behavior to simple and single as standard
		docBehavior = settings.getBehaviour();
		docType = settings.getType();
		padding[0] = settings.getPaddingWidth();
		padding[1] = settings.getPaddingHeight();
		final String[] fileFilter = settings.getFileFilter();
		// If the batch is a directory, add all the files in that directory
		// to the File[], else set the File[] only to contain batch
		if (batch.isDirectory()) {
			this.batchPath = batch.getPath();
			// Filter out all files except for the ones conforming with the
			// filter
			files = batch.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					for (int i = 0; i < fileFilter.length; i++) {
						if (name.endsWith(fileFilter[i]))
							return true;
					}
					return false;
				}
			});
		} else {
			this.batchPath = batch.getParent();
			for (int i = 0; i < fileFilter.length; i++) {
				if (batch.getPath().endsWith(fileFilter[i])) {
					files = new File[1];
					files[0] = batch;
					break;
				}
			}
		}
		if (files == null) {
			length = 0;
		} else {
			length = files.length;
			
			//Get all file suffixes in the batch
			for (int i = 0; i < length; i++) {
				int dotIndex = files[i].getName().indexOf(".");
				if (dotIndex == -1) {
				} else {
					String suffix = files[i].getName().substring(dotIndex);
					if (fileSuffixes.contains(suffix)) {

					} else {
						fileSuffixes.add(suffix);
					}
				}
			}
		}
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

	public void setDocuments(Document[] docs) {
		this.docs = docs;
	}

	public Document[] getDocuments() {
		return docs;
	}

	public void setParent(ImageBatches parent) {
		this.parent = parent;
	}

	public ImageBatches getParent() {
		return parent;
	}

	public String[] getFileSuffixes() {
		return fileSuffixes.toArray(new String[fileSuffixes.size()]);
	}
	public void stopRequest() {
		stopRequest = true;
	}

	public boolean analyzeBatches() {
		stopRequest = false;
		Vector<Document> documentBatch = new Vector<Document>();
		for (int j = 0; j < files.length; j++) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				if (stopRequest == true)
					return false;
			}
			DocumentIdentifier docIdent = new DocumentIdentifier(
					files[j].getPath());
			docIdent.setPadding(padding);
			docIdent.setDocumentType(docType);

			try {
				docIdent.load();
			} catch (IOException e) {
				String stackTrace = "";
				for (int i = 0; i < e.getStackTrace().length; i++) {
					stackTrace += e.getStackTrace()[i].toString() + "\n";
				}
				JOptionPane.showMessageDialog(new JFrame(), stackTrace);
			}
			documentBatch.add(docIdent.identify());
			parent.updatePreview(docIdent.markDocument(new Color(0.5f, 1.0f,
					0.0f, 0.4f)));
			setProgress(j + 1);
		}
		documentBatch = DocumentUtilities.correctDocuments(documentBatch);
		docs = new Document[documentBatch.size()];
		docs = documentBatch.toArray(docs);
		return true;
	}

	@SuppressWarnings("serial")
	public class BatchProgressBar extends JProgressBar {
		private int stop;

		public BatchProgressBar() {
			super(0, ImageBatch.this.getNumberOfFiles());

			stop = ImageBatch.this.getNumberOfFiles();
		}

		public void setProgress(int progress) {
			if (progress == stop) {
				setValue(stop);
				setStringPainted(true);
				String str = "Cropping completed";
				setString(str);
			} else {
				setValue(progress);
				setString("");
			}
		}
	}
}