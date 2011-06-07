package common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JFrame;

import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

/**
 * Utility class for {@link Point} providing functions as collinear point
 * detection, relative point positioning, convex hull and minimal enclosing
 * rectangle generation.
 * 
 * @author Tomas Toss
 */
public class PointUtilities {

	/**
	 * Metric option for minimal enclosing rectangle generation
	 */
	public static final int METRIC_PERIMETER = 0;
	/**
	 * Metric option for minimal enclosing rectangle generation
	 */
	public static final int METRIC_AREA = 1;

	private PointUtilities() {
	}

	private final static Vector<Point> removeCollinearPoints(
			Vector<Point> points) {
		for (int i = 0; i < points.size(); i++) {
			for (int j = i + 1; j < points.size(); j++) {
				for (int k = j + 1; k < points.size(); k++) {
					Point[] p = { points.get(i), points.get(j), points.get(k) };

					int collinear = collinear(p[0], p[1], p[2]);

					// If not collinear, continue
					if (collinear == -1)
						continue;
					else {
						points.remove(p[collinear]);
						// Check which point that was removed
						switch (collinear) {
						case 0:
							// If "i" point was removed continue at the "j loop"
							j = i + 1;
							k = j;
							break;
						case 1:
							// If "j" point was removed continue at the "k loop"
							k = j;
							break;
						case 2:
							// If "k" point was removed continue with the same k
							// value
							k--;
						}


					}
				}
			}
		}

		return points;
	}

	/**
	 * Check if three points are collinear
	 * 
	 * @param a
	 *            First point
	 * @param b
	 *            Second point
	 * @param c
	 *            Third point
	 * @return -1 if not collinear, 1,2 or 3 if a,b, or c is between the other
	 *         points respectively
	 */
	public static final int collinear(Point a, Point b, Point c) {
		int A = 0, B = 1, C = 2;

		boolean collinear = a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x
				* (a.y - b.y) == 0 ? true : false;

		if (!collinear)
			return -1;

		double distAB = Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2);
		double distAC = Math.pow(a.x - c.x, 2) + Math.pow(a.y - c.y, 2);
		double distBC = Math.pow(b.x - c.x, 2) + Math.pow(b.y - c.y, 2);

		if (distAB >= distAC && distAB >= distBC) {
			return C;
		} else if (distAC >= distAB && distAC >= distBC) {
			return B;
		} else {
			return A;
		}

	}

	/**
	 * Compute a convex hull that contains all supplied points. The hull is
	 * represented as a {@link Vector} <{@link Point}>, and the hull points are
	 * stored in clockwise fashion. Note: Modifying this method may break the
	 * functionality of
	 * {@link PointUtilities #minimalEnclosingRectangle(Vector, int)}
	 * 
	 * @param points
	 *            The point that is supposed to be contained in the convex hull
	 * @return The convex hull
	 */
	public static Vector<Point> convexHull(Vector<Point> points) {
		points = removeCollinearPoints(points);

		if (points == null || points.size() == 0) {
			return null;
		}
		// Find the leftmost point
		int min = Integer.MAX_VALUE;
		int minIndex = 0;
		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			if (point.x < min) {
				min = point.x;
				minIndex = i;
			}
		}
		// Start with the leftmost point
		Point pointOnHull = points.get(minIndex);

		Vector<Point> hull = new Vector<Point>();
		Point endPoint = null;
		do {
			hull.add(pointOnHull);
			endPoint = points.get(0);
			// Find the point that has no more points to the left of the line
			// drawn from the previous point (pointOnHull) and itself
			for (int j = 1; j < points.size(); j++) {
				if (isOnRight(pointOnHull, endPoint, points.get(j))) {
					endPoint = points.get(j);
				}
			}
			pointOnHull = endPoint;
			// Continue until we are back to the starting point (A 360 degree
			// right turn has been made)
		} while (!endPoint.equals(hull.get(0)));

		return hull;
	}

	/**
	 * Check if a point is located on the left side of a line (used in convex
	 * hull computation)
	 * 
	 * @param lineStart
	 *            Line starting point
	 * @param lineEnd
	 *            Line end point
	 * @param point
	 *            The point to check
	 * @return True, if the point is to the left of the line, else False
	 */
	public static final boolean isOnRight(Point lineStart, Point lineEnd,
			Point point) {
		double dir = (lineEnd.x - lineStart.x) * (lineStart.y - point.y)
				- (lineStart.y- lineEnd.y) * (point.x - lineStart.x);
		if (lineStart.x == point.x && lineStart.y == point.y
				|| lineEnd.x == point.x && lineEnd.y == point.y)
			return false;
		if (dir >= 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Compute the minimal,with respect to the given metric, rectangle
	 * containing all the given points. The enclosing rectangle is returned as a
	 * 5 element Point array, with the lower left corner as first value and
	 * following corners in clockwise fashion
	 * 
	 * @param points
	 *            The points to include
	 * @param metric
	 *            Metric that should be used for minimization, either area or
	 *            perimeter can be used. Use METRIC_AREA or METRIC_PERIMETER.
	 * @return The minimal enclosing rectangle
	 */
	public static Point[] minimalEnclosingRectangle(Vector<Point> points,
			int metric) {
		// Rotation matrix
		SimpleMatrix rot = new SimpleMatrix(2, 2);
		Point[] rect = new Point[5];
		Vector<Point> hull;
		// Remove unnecessary points, speeding up the algorithm.
		if (points == null || points.size() == 0) {
			return null;
		} else {
			hull = convexHull(points);
		}
		// Create a matrix with containing all points
		double[][] xy = new double[hull.size()][2];
		for (int i = 0; i < hull.size(); i++) {
			xy[i][0] = hull.get(i).x;
			xy[i][1] = hull.get(i).y;
		}
		SimpleMatrix XY = new SimpleMatrix(xy);

		int numPoints = hull.size();

		// Handle simple cases
		switch (numPoints) {
		case 0:
			return null;
		case 1:
			for (int i = 0; i < 5; i++) {
				rect[i] = hull.get(0);
			}
			return rect;
		case 2:
			rect[0] = hull.get(0);
			rect[1] = hull.get(1);
			rect[2] = hull.get(1);
			rect[3] = hull.get(0);
			rect[4] = hull.get(0);
			return rect;
		}

		double minMetric = Double.MAX_VALUE;
		// Compute all angles for each edge in the convex hull
		Vector<Double> angles = edgeAngles(hull);
		for (int i = 0; i < angles.size(); i++) {
			double mI = Double.MAX_VALUE;
			double aI = Double.MAX_VALUE;
			double pI = Double.MAX_VALUE;
			// Compute the rotation matrix for the current angle, and create a
			// matrix containing all points
			// rotated with the respect to the origin
			rot = rotationMatrix(angles.get(i));
			SimpleMatrix XYRot = XY.mult(rot);

			// Get minimum and maximum x and y values
			double maxX = CommonOps.elementMax(XYRot.extractVector(false, 0)
					.getMatrix());
			double maxY = CommonOps.elementMax(XYRot.extractVector(false, 1)
					.getMatrix());
			double minX = CommonOps.elementMin(XYRot.extractVector(false, 0)
					.getMatrix());
			double minY = CommonOps.elementMin(XYRot.extractVector(false, 1)
					.getMatrix());

			// Compute metric values
			aI = (maxX - minX) * (maxY - minY);
			pI = 2 * ((maxX - minX) + (maxY - minY));

			if (metric == METRIC_PERIMETER) {
				mI = pI;
			} else {
				mI = aI;
			}

			// Check if we have a new minimum
			if (mI < minMetric) {
				minMetric = mI;

				// Create a matrix containing all minimum and maximum x and y
				// values
				SimpleMatrix R = new SimpleMatrix(5, 2);
				R.set(0, 0, minX);
				R.set(0, 1, minY);
				R.set(1, 0, maxX);
				R.set(1, 1, minY);
				R.set(2, 0, maxX);
				R.set(2, 1, maxY);
				R.set(3, 0, minX);
				R.set(3, 1, maxY);
				R.set(4, 0, minX);
				R.set(4, 1, minY);

				// Create minimum enclosing rectangle by doing reversing the
				// rotation
				SimpleMatrix rectM = R.mult(rot.transpose());

				// Transform into vector form
				for (int j = 0; j < 5; j++) {
					rect[j] = new Point((int) rectM.get(j, 0), (int) rectM.get(
							j, 1));
				}
			}

		}
		return rect;
	}

	// Compute rotation matrix
	private static final SimpleMatrix rotationMatrix(double angle) {
		SimpleMatrix M = new SimpleMatrix(2, 2);
		M.set(0, 0, Math.cos(angle));
		M.set(0, 1, Math.sin(angle));
		M.set(1, 0, -Math.sin(angle));
		M.set(1, 1, Math.cos(angle));
		return M;
	}

	// Compute edge angles for a number of subsequent points
	private static final Vector<Double> edgeAngles(Vector<Point> points) {
		Vector<Double> angles = new Vector<Double>();
		for (int i = 0; i < points.size() - 1; i++) {

			double angle = Math.atan2(points.get(i + 1).y - points.get(i).y,
					points.get(i + 1).x - points.get(i).x) % (Math.PI / 2);

			if (!angles.contains(new Double(angle))) {
				angles.add(angle);
			}
		}
		return angles;
	}

	public static void main(String[] args) {

		Point p1 = new Point(100, 200);
		Point p7 = new Point(100, 100);
		Point p2 = new Point(100, 300);
		Point p3 = new Point(100, 200);
		Point p4 = new Point(300, 500);
		Point p5 = new Point(200, 200);
		Point p6 = new Point(400, 300);
		Vector<Point> points = new Vector<Point>();
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		points.add(p5);
		points.add(p6);

		System.out.println(collinear(p1, p7, p2));
		System.out.println(collinear(p1, p2, p7));
		System.out.println(collinear(p2, p7, p1));
		Point[] rect = minimalEnclosingRectangle(points, METRIC_AREA);
		int[] x = new int[rect.length];
		int[] y = new int[rect.length];
		for (int i = 0; i < rect.length; i++) {
			x[i] = rect[i].x;
			y[i] = rect[i].y;
		}
		BufferedImage img = new BufferedImage(600, 600,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.fillPolygon(x, y, rect.length);
		for (Point p : points) {
			g.setColor(Color.RED);
			g.drawRect(p.x, p.y, 5, 5);
		}
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.add(ImageUtilities.createCanvas(img));
		f.pack();
		f.setVisible(true);

	}
}
