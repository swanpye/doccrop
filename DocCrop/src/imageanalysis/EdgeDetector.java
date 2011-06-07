package imageanalysis;

import java.awt.image.BufferedImage;

public interface EdgeDetector {

	public void process();
	public BufferedImage getEdgesImage();
	public void setSourceImage(BufferedImage img);

}
