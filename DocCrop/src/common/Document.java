package common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Representation of an Document in an image
 * 
 * @author Tomas Toss 17 maj 2011
 */
public class Document {

	private int x;
	private int y;
	private int width;
	private int height;
	private int rotation;

	public Document() {
	}

	/**
	 * Create document, with the supplied location, size and rotation
	 * 
	 * @param loc
	 *            Center position of the document (x,y >= 0)
	 * @param width
	 *            The width of the document (>=0)
	 * @param height
	 *            The height of the document (>=0)
	 */
	public Document(Point loc, int width, int height) {
		this(loc, width, height, 0);
	}

	/**
	 * Create document, with the supplied location, size and rotation
	 * 
	 * @param loc
	 *            Center position of the document (x,y >= 0)
	 * @param width
	 *            The width of the document (>=0)
	 * @param height
	 *            The height of the document (>=0)
	 * @param rotation
	 *            The rotation of the document in degrees measured from the
	 *            x-axis in clockwise direction. (-90 <= rotation <= 90)
	 */
	public Document(Point loc, int width, int height, int rotation) {
		if (loc.x < 0 || loc.y < 0)
			throw new IllegalArgumentException(
					"The components of the location must be >= 0");
		if (height < 0 || width < 0)
			throw new IllegalArgumentException(
					"The width and the height of the document must be >= 0");
		if (rotation < -90 || rotation > 90)
			throw new IllegalArgumentException(
					"The rotation of the document must be >=-90 & <= 90");
		x = loc.x;
		y = loc.y;
		this.height = height;
		this.width = width;
		this.rotation = rotation;
	}

	public Point getLocation() {
		return new Point(x, y);
	}

	public void setLocation(Point location) {
		x = location.x;
		y = location.y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public int getArea() {
		return height * width;
	}

	/**
	 * Create a Document representation from the set of corner points of a
	 * rectangle. The height of the document is defined by the difference of the
	 * first and second corner points. The width of the document is defined by
	 * the difference of the third and the second corner points. The rotation is
	 * decided by the edge going from the second to the third corner point,
	 * counting clockwise with respect to the x-axis.
	 * 
	 * @param a
	 *            First corner point (x,y >= 0)
	 * @param b
	 *            Second corner point (x,y >= 0)
	 * @param c
	 *            Third corner point (x,y >= 0)
	 * @param d
	 *            Fourth corner point (x,y >= 0)
	 * @return The document represenation of the points
	 */
	public static Document toDocment(Point a, Point b, Point c, Point d) {
		int height = (int) Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y)
				* (a.y - b.y));
		int width = (int) Math.sqrt((b.x - c.x) * (b.x - c.x) + (b.y - c.y)
				* (b.y - c.y));

		int x = a.x + (c.x - a.x) / 2;
		int y = a.y + (c.y - a.y) / 2;
		int rotation;
		if (c.x - b.x == 0)
			rotation = 90;
		else
			rotation = (int) Math.toDegrees(Math
					.atan((c.y - b.y) / (c.x - b.x)));

		return new Document(new Point(x, y), width, height, rotation);
	}

	/**
	 * Translate the document representation to an {@link Point}[]
	 * representation
	 * 
	 * @return The array representation
	 */
	public Point[] toArray() {
		int halfHeight = height / 2;
		int halfWidth = width / 2;

		Point[] rect = new Point[5];

		int wX = (int) (halfWidth * Math.cos(Math.toRadians(-rotation)));
		int hX = (int) (halfHeight * Math.sin(Math.toRadians(-rotation)));
		int wY = (int) (halfWidth * Math.sin(Math.toRadians(-rotation)));
		int hY = (int) (halfHeight * Math.cos(Math.toRadians(-rotation)));

		rect[0] = new Point(x - wX - hX, y + wY - hY);
		rect[1] = new Point(x - wX + hX, y + wY + hY);
		rect[2] = new Point(x + wX + hX, y - wY + hY);
		rect[3] = new Point(x + wX - hX, y - wY - hY);
		rect[4] = new Point(x - wX - hX, y + wY - hY);

		return rect;
	}

	public String toString() {
		String toReturn = "x=" + x + ", y=" + y + "\n";
		toReturn += "width=" + width + ", height=" + height + "\n";
		toReturn += "rotation=" + rotation;
		return toReturn;
	}

	public static void main(String[] args) {
		Document doc = new Document(new Point(200, 200), 100, 150, -10);
		Point[] points = doc.toArray();

		int[] x = new int[points.length];
		int[] y = new int[points.length];
		for (int i = 0; i < points.length; i++) {
			x[i] = points[i].x;
			y[i] = points[i].y;
		}
		System.out.println(Document.toDocment(points[0], points[1], points[2],
				points[3]).rotation);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		BufferedImage img = new BufferedImage(400, 400,
				BufferedImage.TYPE_INT_RGB);
		ImageIcon icon = new ImageIcon(img);
		JLabel l = new JLabel(icon);
		f.add(l);
		f.setVisible(true);
		f.pack();
		Graphics g = img.getGraphics();
		g.setColor(Color.WHITE);
		g.fillPolygon(x, y, x.length);
		g.dispose();

	}
}
