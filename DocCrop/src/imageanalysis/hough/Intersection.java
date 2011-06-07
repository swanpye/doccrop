package imageanalysis.hough;

import java.awt.Point;

public class Intersection {
	/**
	 * Index of the first line that is intersecting
	 */
	protected int lineIndexOne;
	/**
	 * Index of the second line that is intersecting
	 */
	protected int lineIndexTwo;
	/**
	 * The angle of the first intersecting line in radians, with respect to the x-axis,
	 */
	protected double angleOne;
	/**
	 * The angle of the first intersecting line in radians, with respect to the x-axis,
	 */
	protected double angleTwo;
	private Point intersection;

	public Intersection(Point intersection, int lineIndexOne, int lineIndexTwo) {
		this(intersection, lineIndexOne, lineIndexTwo, 0, 90);
	}

	public Intersection(Point intersection, int lineIndexOne, int lineIndexTwo,
			double angleOne, double angleTwo) {
		this.intersection = intersection;
		this.lineIndexOne = lineIndexOne;
		this.lineIndexTwo = lineIndexTwo;
		this.angleOne = angleOne;
		this.angleTwo = angleTwo;
	}

	/**
	 * Get the intersection point (of two lines)
	 * @return The intersection point
	 */
	public Point getIntersection() {
		return intersection;
	}

}
