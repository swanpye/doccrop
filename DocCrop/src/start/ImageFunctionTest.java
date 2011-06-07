package start;

import imageanalysis.canny.CannyEdgeDetector;
import imageanalysis.hough.HoughData;
import imageanalysis.hough.HoughLine;
import imageanalysis.hough.HoughUtilities;
import imageanalysis.hough.Intersection;
import imageanalysis.morphology.Closing;
import imageanalysis.morphology.MorphologicalOperation;
import imageanalysis.morphology.MorphologicalOperation.STRUCTURING_ELEMENT_SHAPE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import common.ImageUtilities;
import common.PointUtilities;
import common.SimpleTimer;

public class ImageFunctionTest {
	/**
	 * Test method, showing functionality of the ImageUtilities file and image
	 * analysis files
	 * 
	 * @param args
	 *            path to an image file
	 */
	public static void main(String[] args) {

		String imgPath = "src/images/10.png";
		
		// Timer variables

		SimpleTimer programTimer = new SimpleTimer();
		SimpleTimer eventTimer = new SimpleTimer();

		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(650, 700));
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		programTimer.start(); // Test read image and scale functionality
		eventTimer.start();
		BufferedImage colorImg = null; 
		try {
		colorImg = ImageUtilities.scaleImage(
					ImageUtilities.readImage(imgPath), 0.2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		eventTimer.stop();
		eventTimer.print("Read image and downscale it");
		tabPane.addTab("Color image",
				new JScrollPane(ImageUtilities.createCanvas(colorImg)));

		// Test grayscale functionality eventTimer.start(); BufferedImage
		BufferedImage grayImg = ImageUtilities.convertToGrayScale(colorImg);
		eventTimer.stop();
		eventTimer.print("Convert image to grayscale");
		tabPane.addTab("Grayscale image",
				new JScrollPane(ImageUtilities.createCanvas(grayImg)));

		// Test morphological operation MorphologicalOperation closing = new
		MorphologicalOperation closing = new Closing(STRUCTURING_ELEMENT_SHAPE.SQUARE,2);
		eventTimer.start();
		BufferedImage closedImg = closing.execute(grayImg);
		eventTimer.stop();
		eventTimer.print("Closing image");
		tabPane.addTab("Morphological",
				new JScrollPane(ImageUtilities.createCanvas(closedImg)));

		// Test Canny edge detector CannyEdgeDetector canny = new
		CannyEdgeDetector canny = new CannyEdgeDetector();
		canny.setSourceImage(closedImg);
		canny.setEdgeSensitivity(CannyEdgeDetector.LOW_EDGE_SENSITIVITY);
		eventTimer.start();
		canny.process();
		eventTimer.stop();
		eventTimer.print("Creating edge image");
		BufferedImage edgeImg = canny.getEdgesImage();
		tabPane.addTab("Edge image",
				new JScrollPane(ImageUtilities.createCanvas(edgeImg)));

		BufferedImage houghImg = ImageUtilities.copyImage(colorImg);

		// Test Hough transfrom 
		eventTimer.start();
		HoughData houghData = HoughUtilities
				.runHoughIterations(edgeImg, 10, 1, 0.1);

		eventTimer.stop();
		eventTimer.print("Computing Hough transform");

		Vector<Intersection> intersections = houghData.getIntersections();
		Vector<HoughLine> lines = houghData.getLines();
		System.out.println(lines.size());
		HoughUtilities.drawHoughLines(houghImg, lines, Color.RED);

		tabPane.addTab("Hough image",
				new JScrollPane(ImageUtilities.createCanvas(houghImg)));

		// Visualize end result BufferedImage
		BufferedImage resultImg = ImageUtilities.copyImage(colorImg);
		eventTimer.start();
		Vector<Intersection> filterInter = HoughUtilities.filterIntersections(
				colorImg, intersections, 20);
		eventTimer.stop();
		eventTimer.print("Filtering intersection points");
		Vector<Point> filterPoints = new Vector<Point>();
		for (Intersection inter : filterInter) {
			filterPoints.add(inter.getIntersection());
		}

		Point[] minimalRect = PointUtilities.minimalEnclosingRectangle(
				filterPoints, PointUtilities.METRIC_AREA);
		int[] x = new int[minimalRect.length];
		int[] y = new int[minimalRect.length];

		for (int i = 0; i < minimalRect.length; i++) {
			x[i] = minimalRect[i].x;
			y[i] = minimalRect[i].y;
		}

		Graphics g = resultImg.getGraphics();
		g.setColor(new Color(100, 0, 0, 100));
		g.fillPolygon(x, y, x.length);
		g.dispose();

		HoughUtilities.markIntersections(resultImg, intersections);
		HoughUtilities.markIntersections(resultImg, filterInter, Color.GREEN);

		tabPane.addTab("Result image",
				new JScrollPane(ImageUtilities.createCanvas(resultImg)));
		programTimer.stop();
		programTimer.print("TOTAL RUNTIME");
		frame.add(tabPane);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();

	}
}
