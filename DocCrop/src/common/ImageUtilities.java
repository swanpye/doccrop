package common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
/**
 * A utility class providing common operations such as reading, scaling and painting, for {@link BufferedImage}.
 * @author Tomas Toss 16 maj 2011
 *
 */
public final class ImageUtilities {
	/**
	 * Private constructor, no instance of ImageUtils is needed
	 */
	private ImageUtilities() {
	};

	/**
	 * Read an image from file
	 * 
	 * @param imagePath
	 *            the relative path to the image
	 * @return the read image
	 * @throws IOException 
	 */
	public static BufferedImage readImage(String imagePath) throws IOException {
		BufferedImage buffImg = null;

			File f = new File(imagePath);
			buffImg = ImageIO.read(f);
		System.out.println(f.getPath());
		return buffImg;
	}

	/**
	 * Convert {@link java.awt.image.BufferedImage} to a grayscale counterpart
	 * 
	 * @param img
	 *            the image to convert
	 * @return A grayscale copy of the input image
	 */
	public static BufferedImage convertToGrayScale(BufferedImage img) {
		BufferedImage colorImg = img;
		BufferedImage grayImg = null;

		int w = colorImg.getWidth();
		int h = colorImg.getHeight();

		grayImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = grayImg.getGraphics();
		g.drawImage(colorImg, 0, 0, null);
		g.dispose();
		return grayImg;
	}

	/**
	 * Copy a {@link java.awt.image.BufferedImage} (including all image data)
	 * 
	 * @param img
	 *            the image to copy
	 * @return A copy of the input image
	 */
	public static BufferedImage copyImage(BufferedImage img) {
		BufferedImage clone = null;

		int w = img.getWidth();
		int h = img.getHeight();

		clone = new BufferedImage(w, h, img.getType());
		Graphics g = clone.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return clone;
	}

	/**
	 * Scale the image. The image is scaled by multiplying the height and the
	 * width of the image by the scale factor
	 * 
	 * @param img
	 *            The image to scale
	 * @param scale
	 *            The scale factor, scale > 0. Scale < 1 yields a smaller image.
	 * @return The scaled image
	 */
	public static BufferedImage scaleImage(BufferedImage img, double scale) {
		if (scale <= 0)
			throw (new IllegalArgumentException(
					"Input parameter scale must be > 0: " + scale));
		int w = img.getWidth();
		int h = img.getHeight();
		Image scaledImg = img.getScaledInstance((int) (w * scale),
				(int) (h * scale), Image.SCALE_DEFAULT);
		BufferedImage scaledBuffImg = new BufferedImage((int) (w * scale),
				(int) (h * scale), img.getType());
		Graphics g = scaledBuffImg.getGraphics();
		g.drawImage(scaledImg, 0, 0, null);
		g.dispose();
		return scaledBuffImg;
	}

	public static JLabel createCanvas(BufferedImage img) {
		return new JLabel(new ImageIcon(img));
	}

	/**
	 * Compute the border color of an image. If the color varies along the border, the most dominant color is chosen
	 * @param img The image to extract the border color from
	 * @return The (most likely) border color
	 */
	public static Color getImageBorderColor(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int R = 0, G = 1, B = 2;
		Raster data = img.getData();

		// Get color at the four borders
		int[] top = new int[9 * width];
		int[] low = new int[9 * width];
		int[] left = new int[9 * height];
		int[] right = new int[9 * height];

		data.getPixels(0, 0, width, 3, top);
		data.getPixels(0, height - 4, width, 3, low);
		data.getPixels(0, 0, 3, height, left);
		data.getPixels(width - 4, 0, 3, height, right);

		// Compute mean colors of the four borders
		int[] meanTop = new int[3];
		int[] meanLow = new int[3];
		int[] meanLeft = new int[3];
		int[] meanRight = new int[3];

		for (int i = 0; i < top.length; i = i + 3) {
			meanTop[R] += top[i];
			meanTop[G] += top[i + G];
			meanTop[B] += top[i + B];
			meanLow[R] += low[i];
			meanLow[G] += low[i + G];
			meanLow[B] += low[i + B];
		}
		meanTop[R] = 3 * meanTop[R] / top.length;
		meanTop[G] = 3 * meanTop[G] / top.length;
		meanTop[B] = 3 * meanTop[B] / top.length;
		meanLow[R] = 3 * meanLow[R] / low.length;
		meanLow[G] = 3 * meanLow[G] / low.length;
		meanLow[B] = 3 * meanLow[B] / low.length;

		for (int i = R; i < right.length; i = i + 3) {
			meanLeft[R] += left[i];
			meanLeft[G] += left[i + G];
			meanLeft[B] += left[i + B];
			meanRight[R] += right[i];
			meanRight[G] += right[i + G];
			meanRight[B] += right[i + B];
		}
		meanLeft[R] = 3 * meanLeft[R] / left.length;
		meanLeft[G] = 3 * meanLeft[G] / left.length;
		meanLeft[B] = 3 * meanLeft[B] / left.length;
		meanRight[R] = 3 * meanRight[R] / right.length;
		meanRight[G] = 3 * meanRight[G] / right.length;
		meanRight[B] = 3 * meanRight[B] / right.length;

		
		//Set the border color to be the median color of the four mean colors
		int[] borderColor = new int[3];

		int[] medianR = { meanRight[R], meanLeft[R], meanTop[R], meanLow[R] };
		Arrays.sort(medianR);
		borderColor[R] = medianR[medianR.length / 2];
		int[] medianG = { meanRight[G], meanLeft[G], meanTop[G], meanLow[G] };
		Arrays.sort(medianG);
		borderColor[G] = medianG[medianG.length / 2];
		int[] medianB = { meanRight[B], meanLeft[B], meanTop[B], meanLow[B] };
		Arrays.sort(medianB);
		borderColor[B] = medianB[medianB.length / 2];

		return new Color(borderColor[R], borderColor[G], borderColor[B]);
	}


	/**
	 * Test function
	 * @param args filepath to an image
	 */
	public static void main(String[] args) {
		try {
		if(args == null) {
			BufferedImage img = ImageUtilities.readImage("src/images/1.png");
			System.out.println(getImageBorderColor(img));			
		} else {
			BufferedImage img = ImageUtilities.readImage(args[0]);
			System.out.println(getImageBorderColor(img));			
		}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
