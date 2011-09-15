package imageanalysis;

import imageanalysis.canny.CannyEdgeDetector;
import imageanalysis.hough.HoughData;
import imageanalysis.hough.HoughLine;
import imageanalysis.hough.HoughUtilities;
import imageanalysis.hough.Intersection;
import imageanalysis.morphology.Closing;
import imageanalysis.morphology.Dilation;
import imageanalysis.morphology.Erosion;
import imageanalysis.morphology.MorphologicalOperation;
import imageanalysis.morphology.MorphologicalOperation.STRUCTURING_ELEMENT_SHAPE;
import imageanalysis.morphology.None;
import imageanalysis.morphology.Opening;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import common.Document;
import common.Document.DocumentType;
import common.ImageUtilities;
import common.PointUtilities;

/**
 * A class for document identification in images. The
 * <code>DocumentIdentifier</code> can be used to identify documents that are
 * fairly rectangular. The identified document can be visualized TODO: and
 * cropped by the <code>DocumentIdentifier</code>.
 * 
 * @author Tomas Toss 16 maj 2011
 */
public class DocumentIdentifier {

	/**
	 * Events fired by the DocumentIdentifier, indicating in which state it is
	 */
	public static final String IMAGE_READ = "image_read";
	public static final String IMAGE_SCALED = "iamge_scaled";
	public static final String IMAGE_GRAYSCALED = "image_grayscaled";
	public static final String IMAGE_MORPHED = "image_morphed";
	public static final String IMAGE_EDGE_DETECTED = "image_edge_detected";
	public static final String IMAGE_HOUGH_TRANSFORMED = "image_hough_transformed";
	public static final String INTERSECTIONS_FILTERED = "intersections_filtered";

	private String filePath;
	private int[] padding = { 0, 0 };
	private DocumentType docType;
	private BufferedImage processImg;
	private BufferedImage img;
	private Document document;
	private double scaleFactor;
	private MorphologicalOperation morphOp;
	private EdgeDetector edgeDetector;
	static JLabel canvas = null;

	private double[] lineThreshold = new double[2];
	private static final int LOW = 0, HIGH = 1;
	private int noisyThreshold = 15;
	private float interToLineThreshold = 0.3f;

	// Identifier settings
	private final int MAX_LINES_FOUND = 50;
	private int MAX_NUM_TRIES = 3;
	private int imageProcessSize = 600;
	private int lineIterations = 5;
	private int colorThreshold = 10;
	private int minimalDocumentArea;
	private PropertyChangeSupport notifier = new PropertyChangeSupport(this);

	public DocumentIdentifier() {
		this("");
	}

	public DocumentIdentifier(String filePath) {
		this(filePath, new Closing(STRUCTURING_ELEMENT_SHAPE.SQUARE, 1));
	}

	public DocumentIdentifier(String filePath, MorphologicalOperation morphOp) {
		this(filePath, morphOp, new CannyEdgeDetector());
	}

	public DocumentIdentifier(String filePath, MorphologicalOperation morphOp,
			EdgeDetector edgeDet) {
		this.filePath = filePath;
		this.morphOp = morphOp;
		edgeDetector = edgeDet;
		lineThreshold[HIGH] = 0.5;
		lineThreshold[LOW] = 0.1;
	}

	/**
	 * Load the image into the <code>documentIdentifier</code>
	 * 
	 * @return A copy of the image loaded
	 * @throws IOException
	 *             If an error occurs during re
	 */
	public BufferedImage load() throws IOException {
		img = ImageUtilities.readImage(filePath);
		firePropertyChange(IMAGE_READ, null, img);
		return ImageUtilities.copyImage(img);
	}

	/**
	 * Identify the document in the loaded image. While running, the method
	 * fires <code>propertyChangeEvents</code> to indicate in which state the
	 * identification process currently is.
	 * 
	 * @return The identified document
	 */
	public Document identify() {
		BufferedImage sourceImg;
		// Variables for identification stop criteria
		int tryNumber = 0;
		boolean tryAgain = true;

		// Compute which factor the image should be scaled with
		int originalMax = Math.max(img.getHeight(), img.getWidth());
		scaleFactor = (double) imageProcessSize / originalMax;

		// Scale the image for faster processing
		sourceImg = ImageUtilities.scaleImage(img, scaleFactor);
		// /////////////////Break point///////////////////////
		if (Thread.interrupted())
			return null;
		// ///////////////////////////////////////////////////
		processImg = ImageUtilities.copyImage(sourceImg);
		// /////////////////Break point///////////////////////
		if (Thread.interrupted())
			return null;
		// ///////////////////////////////////////////////////

		minimalDocumentArea = processImg.getHeight() * processImg.getWidth()
				/ 4;
		firePropertyChange(IMAGE_SCALED, null, sourceImg);

		// Convert image to grayscale, needed for further processing
		sourceImg = ImageUtilities.convertToGrayScale(sourceImg);
		// /////////////////Break point///////////////////////
		if (Thread.interrupted())
			return null;
		// ///////////////////////////////////////////////////
		firePropertyChange(IMAGE_GRAYSCALED, null, sourceImg);
		BufferedImage original = ImageUtilities.copyImage(sourceImg);
		// /////////////////Break point///////////////////////
		if (Thread.interrupted())
			return null;
		// ///////////////////////////////////////////////////

		// Execute document identification steps until stop criteria is
		// fulfilled
		while (tryAgain && tryNumber < MAX_NUM_TRIES) {
			// Apply morphological operation, removing noise
			sourceImg = morphOp.execute(original);
			firePropertyChange(IMAGE_MORPHED, null, sourceImg);

			// Detect edges
			edgeDetector.setSourceImage(sourceImg);
			
			
			// /////////////////Break point///////////////////////
			if (Thread.interrupted())
				return null;
			// ///////////////////////////////////////////////////
			
			
			edgeDetector.process();
			
			// /////////////////Break point///////////////////////
			if (Thread.interrupted())
				return null;
			// ///////////////////////////////////////////////////
			
			
			sourceImg = edgeDetector.getEdgesImage();
			firePropertyChange(IMAGE_EDGE_DETECTED, null, sourceImg);

			// Run Hough transform, and find suitable lines by automatically
			// adjust line threshold
			HoughData houghData = null;
			Vector<Intersection> filteredIntersections = null;
			Vector<HoughLine> lines = null;
			Vector<Intersection> intersections = null;
			boolean findMoreLines = true;

			// Find lines, iterate until enough lines has been found
			do {
				
				// /////////////////Break point///////////////////////
				if (Thread.interrupted())
					return null;
				// ///////////////////////////////////////////////////
				
				
				houghData = HoughUtilities
						.runHoughIterations(sourceImg, lineIterations,
								lineThreshold[HIGH], lineThreshold[LOW]);
				lines = houghData.getLines();
				intersections = houghData.getIntersections();
				// /////////////////Break point///////////////////////
				if (Thread.interrupted())
					return null;
				// ///////////////////////////////////////////////////
				firePropertyChange(IMAGE_HOUGH_TRANSFORMED, null, intersections);

				// Filter out intersection points that are not located on one of
				// the document edges
				filteredIntersections = HoughUtilities.filterIntersections(
						processImg, intersections, colorThreshold);
				firePropertyChange(INTERSECTIONS_FILTERED, null,
						filteredIntersections);

				int numInter, numFiltered;
				numInter = intersections.size();

				// Check if there are any intersections after filtration
				if (filteredIntersections != null)
					numFiltered = filteredIntersections.size();
				else {
					numFiltered = 1;
					filteredIntersections = new Vector<Intersection>();
					filteredIntersections.add(new Intersection(new Point(0, 0),
							0, 0));
				}
				// Adjust line filtering settings if needed
				if (numFiltered < 4) {
					if (numInter < MAX_LINES_FOUND) {
						if (lineThreshold[HIGH] > 0.25) {
							lineThreshold[HIGH] -= 0.1;
							findMoreLines = true;
						} else {
							findMoreLines = false;
						}
					} else {
						findMoreLines = false;
					}
				} else {
					// TODO: Do something more, possibly adjust edge detection
					findMoreLines = false;
				}
			} while (findMoreLines);

			Vector<Point> filterPoints = new Vector<Point>();
			for (Intersection intersection : filteredIntersections) {
				filterPoints.add(intersection.getIntersection());
			}

			// Create cropping rectangle
			Point[] minRect = PointUtilities.minimalEnclosingRectangle(
					filterPoints, PointUtilities.METRIC_AREA);

			
			// If no minimal rectangle was created (for example when no points
			// was supplied to the method)
			// create a default rectangle with size 0.
			if (minRect == null) {
				minRect = new Point[5];
				minRect[0] = new Point(0, 0);
				minRect[1] = new Point(0, 0);
				minRect[2] = new Point(0, 0);
				minRect[3] = new Point(0, 0);
				minRect[4] = new Point(0, 0);
			}

			//Map the cropping rectangle to fit the original image			
			for(int i = 0; i < minRect.length; i++) {
				minRect[i].x /= scaleFactor;
				minRect[i].y /= scaleFactor;
			}
			
			document = Document.toDocment(minRect[0], minRect[1], minRect[2],
					minRect[3]);
			document.setId(filePath);

			// Apply pre set padding
			document.setHeight(document.getHeight() + padding[1]);
			document.setWidth(document.getWidth() + padding[0]);

			// /////////////////Break point///////////////////////
			if (Thread.interrupted())
				return null;
			// ///////////////////////////////////////////////////
			
			
			if (lines.size() > noisyThreshold
					|| intersections.size() / lines.size() < interToLineThreshold) {
				int maxShapeSize = Math.min(processImg.getWidth(),
						processImg.getHeight()) / 50;
				int shapeSize = morphOp.getShapeSize();
				if (morphOp instanceof Closing || morphOp instanceof Opening
						|| morphOp instanceof None) {
					morphOp = new Erosion(STRUCTURING_ELEMENT_SHAPE.SQUARE,
							shapeSize);
				} else if (morphOp instanceof Dilation
						&& shapeSize < maxShapeSize) {
					morphOp = new Dilation(STRUCTURING_ELEMENT_SHAPE.SQUARE,
							++shapeSize);
				} else if (morphOp instanceof Erosion
						&& shapeSize < maxShapeSize) {
					morphOp = new Erosion(STRUCTURING_ELEMENT_SHAPE.SQUARE,
							++shapeSize);
				}
				System.out.println("Adjusting morphological operation...");
				continue;
			}
			// /////////////////Break point///////////////////////
			if (Thread.interrupted())
				return null;
			// ///////////////////////////////////////////////////
			if (isOutsideImage(minRect)) {
				// If there are many intersection points, try to make the image
				// smoother...
				if (lines.size() > noisyThreshold
						|| intersections.size() / lines.size() < interToLineThreshold
						&& filteredIntersections.size() > 4) {
					int opSize = morphOp.getShapeSize();
					// If the structuring element is relatively small, make it
					// larger
					if (opSize <= Math.min(processImg.getWidth(),
							processImg.getHeight()) / 25) {
						opSize++;
					}
					morphOp = new Dilation(STRUCTURING_ELEMENT_SHAPE.SQUARE,
							opSize);
				}
				System.out
						.println("Noisy edge image, applying smoothening morphological operand ...");

				// If the identified document has to small area, adjust the edge
				// detector
			} else if (document.getArea() <= minimalDocumentArea/scaleFactor) {
				tryAgain = adjustEdgeDetector();

			} else {
				tryAgain = false;
			}

			if (tryAgain) {
				tryNumber++;
			}
		}

		return document;
	}

	/**
	 * Adjust the edge detector to produce a better result
	 * 
	 * @return True if the edge detector settings has been changed, false
	 *         otherwise.
	 */
	private boolean adjustEdgeDetector() {
		if (edgeDetector instanceof AdjustableDetector) {
			int sensitivity = ((AdjustableDetector) edgeDetector)
					.getEdgeSensitivity();

			if (sensitivity == AdjustableDetector.LOW_EDGE_SENSITIVITY) {
				((AdjustableDetector) edgeDetector)
						.setEdgeSensitivity(AdjustableDetector.MEDIUM_EDGE_SENSITIVITY);

			} else if (sensitivity == AdjustableDetector.MEDIUM_EDGE_SENSITIVITY) {
				((AdjustableDetector) edgeDetector)
						.setEdgeSensitivity(AdjustableDetector.HIGH_EDGE_SENSITIVITY);

			} else {
				morphOp = new None();
				return true;
			}
			System.out.println("Adjusting edge detector threshold ...");
			return true;
		}
		return false;
	}

	private boolean isOutsideImage(Point[] points) {
		for (int i = 0; i < points.length; i++) {
			if (points[i].x < 0 || points[i].x >= img.getWidth())
				return true;
			else if (points[i].y < 0 || points[i].y >= img.getHeight())
				return true;
		}
		return false;
	}

	/**
	 * Visualize the document identified by the <code>DocumentIdentifier</code>
	 * with a colored rectangle. Note that the <code>DocumentIdentifier</code>
	 * must have been run at least once for this method to produce a meaningful
	 * result.
	 * 
	 * @param color
	 *            The color of the marking rectangle
	 * @return The original image with the identified document marked by a
	 *         colored rectangle
	 */
	public BufferedImage markDocument(Color color) {
		if (document == null || processImg == null)
			return null;
		else {
			BufferedImage markedImg = ImageUtilities.copyImage(processImg);
			Point[] corners = document.toArray();
			//Map the cropping rectangle to fit the process image	
			for(int i = 0; i < corners.length; i++) {
				corners[i].x *= scaleFactor;
				corners[i].y *= scaleFactor;
			}
			Graphics g = markedImg.getGraphics();
			int[] x = new int[corners.length];
			int[] y = new int[corners.length];
			for (int i = 0; i < corners.length; i++) {
				x[i] = corners[i].x;
				y[i] = corners[i].y;
			}
			g.setColor(color);
			g.fillPolygon(x, y, x.length);
			g.setColor(Color.BLACK);
			g.drawPolyline(x, y, x.length);
			g.dispose();
			return markedImg;
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		notifier.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		notifier.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		notifier.firePropertyChange(propertyName, oldValue, newValue);
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setHighLineThreshold(int highTresh) {
		if (highTresh < 0 || highTresh > 1 || highTresh < lineThreshold[LOW])
			throw new IllegalArgumentException(
					"High line threshold must be >= 0, <= 1 and >= low line threshold");
		lineThreshold[HIGH] = highTresh;
	}

	public void setLowLineThreshold(int lowTresh) {
		if (lowTresh < 0 || lowTresh > 1 || lowTresh > lineThreshold[HIGH])
			throw new IllegalArgumentException(
					"Low line threshold must be >= 0, <= 1 and <= high line threshold");
		lineThreshold[HIGH] = lowTresh;
	}

	public void setMaxIterations(int iterations) {
		MAX_NUM_TRIES = iterations;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Get the internal process image size (the maximum of the height and width
	 * of the process image).
	 * 
	 * @return The process image size
	 */
	public int getImageProcessSize() {
		return imageProcessSize;
	}

	/**
	 * Set the size of the internal process image that the
	 * <code>DocumentIdentifier</code> should work on. A smaller process image
	 * leads to better performance, but might decrease accuracy. Default value
	 * is set to 500.
	 * 
	 * @param imageProcessSize
	 *            The new size of the internal process image
	 */
	public void setImageProcessSize(int imageProcessSize) {
		if (imageProcessSize <= 0)
			throw new IllegalArgumentException(
					"Image working size must be >= 0.");
		this.imageProcessSize = imageProcessSize;
	}

	/**
	 * Get the morphological operand to be used by the
	 * <code>DocumentIdentifier</code>.
	 * 
	 * @return The morphological operand
	 */
	public MorphologicalOperation getMorphOp() {
		return morphOp;
	}

	/**
	 * Set the morphological operand to be used by the
	 * <code>DocumentIdentifier</code>. The default is {@link Closing} with an
	 * {@link MorphologicalOperation.STRUCTURING_ELEMENT_SHAPE #SQUARE} of size
	 * 2. Note: The <code>DocumentIdentifier</code> might automatically change
	 * the morphological operation if it thinks it will yield better results.
	 * 
	 * @param
	 */
	public void setMorphOp(MorphologicalOperation morphOp) {
		this.morphOp = morphOp;
	}

	public EdgeDetector getEdgeDetector() {
		return edgeDetector;
	}

	/**
	 * Set the edge detector used by <code>DocumentIdentifier</code>. The
	 * default detector is @ CannyEdgeDetector} .
	 * 
	 * @param edgeDetector
	 */
	public void setEdgeDetector(EdgeDetector edgeDetector) {
		this.edgeDetector = edgeDetector;
	}

	public Document getDocument() {
		return document;
	}

	/**
	 * Set the type of document the documentIdentifier should look for
	 * 
	 * @param docType
	 *            The type of document @see {@link Document.DocumentType}
	 */
	public void setDocumentType(DocumentType docType) {
		this.docType = docType;
	}

	/**
	 * Get the document type the documentIdentifier is looking for
	 * 
	 * @return
	 */
	public DocumentType getDocumentType() {
		return docType;
	}

	/**
	 * Set the padding that the documentIdentifier should apply to the document
	 * after identification
	 * 
	 * @param width
	 *            The amount of padding applied to the width of the document
	 * @param height
	 *            The amount of padding applied to the height of the document
	 */
	public void setPadding(int width, int height) {
		padding[0] = width;
		padding[1] = height;
	}

	/**
	 * Set the padding that the documentIdentifier should apply to the document
	 * after identification
	 * 
	 * @param padding
	 *            The amount of padding applied to the width and height
	 *            respectively of the document
	 * 
	 */
	public void setPadding(int[] padding) {
		this.padding = padding;
	}

	/**
	 * Returns the padding which the documentIdentifier applies to the document
	 * after identification
	 * 
	 * @return The padding {width, height}
	 */
	public int[] getPadding() {
		return padding;
	}

	public static void main(String[] args) {

		final JFrame f = new JFrame();
		f.setPreferredSize(new Dimension(400, 600));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.pack();

		for (int i = 1; i <= 1; i++) {

			DocumentIdentifier docIdent = new DocumentIdentifier(
					"./src/images/" + i + ".tif");

			try {
				docIdent.load();
			} catch (IOException fnf) {
				fnf.printStackTrace();
			}
			docIdent.identify();
			// TODO Auto-generated method stub
			final BufferedImage croppedImg = docIdent.markDocument(new Color(
					0.5f, 1.0f, 0.0f, 0.4f));

			final int imgNr = i;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					JLabel oldCanvas = DocumentIdentifier.canvas;
					DocumentIdentifier.canvas = ImageUtilities
							.createCanvas(croppedImg);
					f.add(DocumentIdentifier.canvas);
					if (oldCanvas != null)
						f.remove(oldCanvas);
					DocumentIdentifier.canvas.revalidate();
					f.setTitle(imgNr + ".png");
					f.repaint();

				}

			});
		}
	}
}
