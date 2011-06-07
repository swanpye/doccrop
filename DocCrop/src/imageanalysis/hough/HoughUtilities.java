package imageanalysis.hough;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Arrays;
import java.util.Vector;

import org.ejml.simple.SimpleMatrix;

import common.ImageUtilities;

/**
 * A utility class providing extra functionality to the Hough Transform.
 * 
 * @author Tomas Toss
 */
public class HoughUtilities {

	/**
	 * Get the intersection points of a set of {@link HoughLine}. Only
	 * intersection points of lines deviating with at most the given threshold
	 * from being perpendicular to each other are returned.
	 * 
	 * @param img
	 *            The image that the Hough transform has been used on
	 * @param lines
	 *            The set of hough lines to examine
	 * @param threshold
	 *            Angular threshold, in degrees. Lines crossing each other with
	 *            an angle of 90 - threshold degrees are considered to intersect
	 * @return All intersection points (which pass the threshold filtering)
	 */
	public static Vector<Intersection> getIntersections(BufferedImage img,
			Vector<HoughLine> lines, double threshold) {
		// Compute hough image data, used to transform hough coordinates into
		// image coordinates
		int height = img.getHeight();
		int width = img.getWidth();
		int houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2;
		int centerX = width / 2;
		int centerY = height / 2;

		// Convenience variable
		double piHalf = Math.PI / 2;
		SimpleMatrix A = new SimpleMatrix(2, 2);
		SimpleMatrix b = new SimpleMatrix(2, 1);

		Vector<Intersection> intersections = new Vector<Intersection>();
		// Iterate through all possible intersections (all pairs only considered
		// once)
		for (int i = 0; i < lines.size(); i++) {
			for (int j = i + 1; j < lines.size(); j++) {

				double thetaI = lines.get(i).theta;
				double thetaJ = lines.get(j).theta;
				// Transform rho from local Hough representation to standard
				// Hough representation
				double rhoI = lines.get(i).r - houghHeight;
				double rhoJ = lines.get(j).r - houghHeight;

				// Filter out the lines which intersect each other with an angle
				// within the boundaries of the threshold
				if (Math.abs(thetaI - thetaJ) >= Math.toRadians(90 - threshold)
						&& Math.abs(thetaI - thetaJ) <= Math
								.toRadians(90 + threshold)) {

					double deltaXI;
					double deltaYI;
					// Compute the direction of the two lines, and save one of
					// the (deltaXI and deltaYI) for later computations

					/*
					 * |deltaXI deltaXJ| A = | | |deltaYI deltaYJ|
					 */
					if (thetaI >= 0) {
						deltaXI = -Math.cos(piHalf - thetaI);
						deltaYI = Math.sin(piHalf + thetaI);
						A.set(0, 0, deltaXI);
						A.set(1, 0, deltaYI);
					} else {
						deltaXI = -Math.cos(-piHalf - thetaI);
						deltaYI = Math.sin(-piHalf - thetaI);
						A.set(0, 0, -Math.cos(-piHalf - thetaI));
						A.set(1, 0, Math.sin(-piHalf - thetaI));
					}
					if (thetaJ >= 0) {
						A.set(0, 1, -Math.cos(piHalf - thetaJ));
						A.set(1, 1, Math.sin(piHalf + thetaJ));
					} else {
						A.set(0, 1, -Math.cos(-piHalf - thetaJ));
						A.set(1, 1, Math.sin(-piHalf - thetaJ));
					}

					double xOnLineI = rhoI * Math.cos(thetaI);
					double yOnLineI = rhoI * Math.sin(thetaI);
					double yOnLineJ = rhoJ * Math.sin(thetaJ);
					double xOnLineJ = rhoJ * Math.cos(thetaJ);

					/*
					 * | (xOnLineJ - xOnLineI) | b = | | | (yOnLineJ - yOnLineI)
					 * |
					 */
					b.set(0, 0, xOnLineJ - xOnLineI);
					b.set(1, 0, yOnLineJ - yOnLineI);

					/*
					 * The intersection point is calculated by solving (with
					 * respect to t and u) the following linear system
					 * 
					 * Point on lineI + (direction of lineI)*t = Point on lineJ
					 * + (direction of lineJ)*u
					 * 
					 * Get the intersection point by traveling along lineI the
					 * distance t from the starting point.
					 */
					SimpleMatrix sol = A.solve(b);
					int x = (int) Math
							.round(xOnLineI + sol.get(0, 0) * deltaXI);
					int y = (int) Math
							.round(yOnLineI + sol.get(0, 0) * deltaYI);

					// Change coordinates to image representation (position from
					// upper left corner)
					x += centerX;
					y += centerY;

					intersections.add(new Intersection(new Point(x, y), i, j,
							thetaI, thetaJ));
				}

			}
		}
		return intersections;
	}

	/**
	 * Filter out intersection points that are not close to a pixel with similar
	 * color as the border.
	 * 
	 * @param img
	 *            The image that has been used to find the original
	 *            intersections
	 * @param intersections
	 *            The intersections
	 * @param threshold
	 *            Color threshold value. If there is no neighborhood with color,
	 *            within the boundaries of the threshold, close to the border
	 *            color, the intersection point is remove
	 * @return The filtered intersection points
	 */
	public static Vector<Intersection> filterIntersections(BufferedImage img,
			Vector<Intersection> intersections, int threshold) {
		Color bkgColor = ImageUtilities.getImageBorderColor(img);
		Vector<Intersection> filteredInter = new Vector<Intersection>();
		Raster imgData = img.getData();
		int width = img.getWidth();
		int height = img.getHeight();
		// Convenience constants, for improved readability
		final int R = 0, G = 1, B = 2;
		final int X = 0, Y = 1;
		
		intersection:
		// Go through all intersections
		for (Intersection inter : intersections) {
			// Get the angles of the lines that are intersecting
			double angleOne = Math.abs(inter.angleOne);
			double angleTwo = Math.abs(inter.angleTwo);
			// Intersection point
			Point p = inter.getIntersection();

			// Start creating a cross, centered at the intersection point and
			// with its lines following the hough lines
			// The cross is used to identify the intersection point's
			// neighboring pixel colors in different directions.
			int crossSize = 20;
			int[][] cOne = null, cTwo = null, cThree = null, cFour = null;

			boolean crossDone = false;
			while (!crossDone) {
				cOne = new int[crossSize][2];
				cTwo = new int[crossSize][2];
				cThree = new int[crossSize][2];
				cFour = new int[crossSize][2];

				// Compute all the coordinates for the cross
				for (int i = 0; i < crossSize; i++) {
					cOne[i][X] = (int) (p.x + i * Math.cos(angleOne));
					cOne[i][Y] = (int) (p.y + i * Math.sin(angleOne));
					cTwo[i][X] = (int) (p.x - i * Math.cos(angleOne));
					cTwo[i][Y] = (int) (p.y - i * Math.sin(angleOne));
					cThree[i][X] = (int) (p.x + i * Math.cos(angleTwo));
					cThree[i][Y] = (int) (p.y + i * Math.sin(angleTwo));
					cFour[i][X] = (int) (p.x - i * Math.cos(angleTwo));
					cFour[i][Y] = (int) (p.y - i * Math.sin(angleTwo));
				}

				// Verify that the cross doesn't extends outside the image
				// boundaries. If so, make the cross smaller, and try again.
				if (cOne[crossSize - 1][X] >= width
						|| cOne[crossSize - 1][X] < 0
						|| cOne[crossSize - 1][Y] >= height
						|| cOne[crossSize - 1][Y] < 0
						|| cTwo[crossSize - 1][X] >= width
						|| cTwo[crossSize - 1][X] < 0
						|| cTwo[crossSize - 1][Y] >= height
						|| cTwo[crossSize - 1][Y] < 0
						|| cThree[crossSize - 1][X] >= width
						|| cThree[crossSize - 1][X] < 0
						|| cThree[crossSize - 1][Y] >= height
						|| cThree[crossSize - 1][Y] < 0
						|| cFour[crossSize - 1][X] >= width
						|| cFour[crossSize - 1][X] < 0
						|| cFour[crossSize - 1][Y] >= height
						|| cFour[crossSize - 1][Y] < 0) {
					crossDone = false;
					if (crossSize > 1)
						crossSize--;
					else
						continue intersection;
				} else {
					crossDone = true;
				}
			}

			// Retrieve the cross's colors
			int[][] cOneColor = new int[crossSize][3];
			int[][] cTwoColor = new int[crossSize][3];
			int[][] cThreeColor = new int[crossSize][3];
			int[][] cFourColor = new int[crossSize][3];
			for (int i = 0; i < crossSize; i++) {
				imgData.getPixel(cOne[i][X], cOne[i][Y], cOneColor[i]);
				imgData.getPixel(cTwo[i][X], cTwo[i][Y], cTwoColor[i]);
				imgData.getPixel(cThree[i][X], cThree[i][Y], cThreeColor[i]);
				imgData.getPixel(cFour[i][X], cFour[i][Y], cFourColor[i]);
			}
			// Separate R,G and B color channel into different arrays
			int[] cOneR = new int[crossSize];
			int[] cOneG = new int[crossSize];
			int[] cOneB = new int[crossSize];
			int[] cTwoR = new int[crossSize];
			int[] cTwoG = new int[crossSize];
			int[] cTwoB = new int[crossSize];
			int[] cThreeR = new int[crossSize];
			int[] cThreeG = new int[crossSize];
			int[] cThreeB = new int[crossSize];
			int[] cFourR = new int[crossSize];
			int[] cFourG = new int[crossSize];
			int[] cFourB = new int[crossSize];
			for (int i = 0; i < crossSize; i++) {
				cOneR[i] = cOneColor[i][R];
				cOneG[i] = cOneColor[i][G];
				cOneB[i] = cOneColor[i][B];
				cTwoR[i] = cTwoColor[i][R];
				cTwoG[i] = cTwoColor[i][G];
				cTwoB[i] = cTwoColor[i][B];
				cThreeR[i] = cThreeColor[i][R];
				cThreeG[i] = cThreeColor[i][G];
				cThreeB[i] = cThreeColor[i][B];
				cFourR[i] = cFourColor[i][R];
				cFourG[i] = cFourColor[i][G];
				cFourB[i] = cFourColor[i][B];
			}
			// Compute the median of the four cross's lines
			int[] medianOne = { median(cOneR), median(cOneG), median(cOneB) };
			int[] medianTwo = { median(cTwoR), median(cTwoG), median(cTwoB) };
			int[] medianThree = { median(cThreeR), median(cThreeG),
					median(cThreeB) };
			int[] medianFour = { median(cFourR), median(cFourG), median(cFourB) };

			int bkgRed = bkgColor.getRed();
			int bkgBlue = bkgColor.getBlue();
			int bkgGreen = bkgColor.getGreen();

			// If not at least one of the lines aren't within the color
			// threshold, it is filtered out
			int isNotDocumentcorner = 0;
			if (Math.abs(medianOne[R] - bkgRed) > threshold
					|| Math.abs(medianOne[G] - bkgGreen) > threshold
					|| Math.abs(medianOne[B] - bkgBlue) > threshold) {
				isNotDocumentcorner++;
			}
			if (Math.abs(medianTwo[R] - bkgRed) > threshold
					|| Math.abs(medianTwo[G] - bkgGreen) > threshold
					|| Math.abs(medianTwo[B] - bkgBlue) > threshold) {
				isNotDocumentcorner++;
			}
			if (Math.abs(medianThree[R] - bkgRed) > threshold
					|| Math.abs(medianThree[G] - bkgGreen) > threshold
					|| Math.abs(medianThree[B] - bkgBlue) > threshold) {
				isNotDocumentcorner++;
			}
			if (Math.abs(medianFour[R] - bkgRed) > threshold
					|| Math.abs(medianFour[G] - bkgGreen) > threshold
					|| Math.abs(medianFour[B] - bkgBlue) > threshold) {
				isNotDocumentcorner++;
			}
			if (isNotDocumentcorner < 4)
				filteredInter.add(inter);
		}
		return filteredInter;
	}

	/**
	 * Show the intersections (as squares) on the supplied image
	 * 
	 * @param img
	 *            The image to mark
	 * @param intersections
	 *            The intersections to mark
	 */
	public static void markIntersections(BufferedImage img,
			Vector<Intersection> intersections) {
		markIntersections(img, intersections, Color.blue);
	}

	/**
	 * Show the intersections (as squares) on the supplied image
	 * 
	 * @param img
	 *            The image to mark
	 * @param intersections
	 *            The intersections to mark
	 * @param color
	 *            The color of the squares
	 */
	public static void markIntersections(BufferedImage img,
			Vector<Intersection> intersections, Color color) {
		Graphics g = img.getGraphics();
		g.setColor(color);
		for (Intersection intersection : intersections) {
			Point p = intersection.getIntersection();
			g.drawRect(p.x - 2, p.y - 2, 5, 5);

		}
		g.dispose();
	}

	/**
	 * Run Hough Transformation, decreasing the line extraction threshold every
	 * iteration. Return either when all iterations have been executed, or when
	 * there is enough intersection points to define a rectangle.
	 * 
	 * @param img
	 *            The (edge) image that the Hough transformation will be applied
	 *            to
	 * @param iters
	 *            Number of Hough transformation iterations (>= 1)
	 * @param hThresh
	 *            High threshold, (percentile of highest hough peak value)
	 * @param lThresh
	 *            Low threshold, (percentile of highest hough peak value)
	 * @return An HashMap containing intersection points and hough lines. The
	 *         intersection points have the key "intersections" and the hough
	 *         lines the key "lines"
	 */
	public static HoughData runHoughIterations(
			BufferedImage img, int iters, double hThresh, double lThresh) {
	
		if (iters <= 0 || hThresh <= 0 || hThresh > 1 || lThresh < 0
				|| lThresh > 1 || hThresh < lThresh)
			throw new IllegalArgumentException(
					"Invalid arguments, iters, hThresh (> lThresh) must all be greater than zero");

		Vector<HoughLine> lines = null;
		Vector<Intersection> inters = null;

		HoughTransform h = new HoughTransform(img.getWidth(), img.getHeight());
		h.addPoints(img);

		hThresh = hThresh * h.getHighestValue();
		lThresh = lThresh * h.getHighestValue();

		double threshStep;
		if (iters > 1)
			threshStep = (hThresh - lThresh) / (iters - 1);
		else
			threshStep = hThresh + 1;
		for (double threshold = hThresh; threshold >= lThresh; threshold = threshold
				- threshStep) {

			lines = h.getLines((int) threshold);
			inters = getIntersections(img, lines, 2);

			// If there are less than 4 intersections, go to next the iteration
			if (inters.size() < 4) {
				continue;
			}

			// Check how many lines that gets intersected by at least 2 unique
			// lines
			int[] distinctLines = new int[lines.size()];
			for (Intersection inter : inters) {
				distinctLines[inter.lineIndexOne]++;
				distinctLines[inter.lineIndexTwo]++;
			}
			int numDistinctLines = 0;
			for (int i = 0; i < distinctLines.length; i++) {
				if (distinctLines[i] >= 2)
					numDistinctLines++;
			}
			// If not enough distinct line pairs have been found go to next
			// iteration, else return
			if (numDistinctLines < 4) {
				continue;
			} else
				break;
		}


		return new HoughData(lines, inters);
	}

	/**
	 * Draw the hough lines on the supplied image
	 * 
	 * @param img
	 *            The image to draw on
	 * @param lines
	 *            The hough lines to draw
	 * @param color
	 *            The color of the lines
	 */
	public static void drawHoughLines(BufferedImage img,
			Vector<HoughLine> lines, Color color) {
		for (HoughLine line : lines) {
			line.draw(img, color.getRGB());
		}
	}

	private static int median(int[] array) {
		Arrays.sort(array);
		return array[array.length / 2];
	}
}
