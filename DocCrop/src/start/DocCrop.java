package start;

import java.util.Locale;
import java.util.ResourceBundle;

import mvc.view.DocCropWindow;

/**
 * Main class for the DocCrop project.
 * 
 * DocCrop is a project developed by Tomas Toss as a part of his master thesis
 * at Uppsala university. The aim of this project is to supply an easy to use
 * and robust program for automated document cropping. The program relies on
 * assumption of rectangular shaped documents preferably lying on preferably an
 * homogeneously colored surface. For documents not complying with this
 * restrictions, a correct cropping will probably not be performed.
 * 
 * The document identification process can be divided into two parts, one
 * qualitative part and one quantitative part.
 * 
 * Qualitative algorithmic overview: - Adjust size and convert image to
 * grayscale - Apply morphological filter - Compute edge map - Hough transform -
 * Identify (fairly) perpendicular lines - Filter intersection points, and
 * construct minimal bounding cropping rectangle
 * 
 * 
 * Quantitative algorithmic overview: - Collect size and positions of all the
 * images in a batch - Use an upper quantile to determine true document size
 * 
 * If the position of the documents can be well approximated by a linear curve -
 * Construct linear curve and fit the positions of the documents to this curve.
 * Else - Use median filtering to determine the position of the documents
 * 
 */
public class DocCrop {
	public static ResourceBundle rBundle;

	public static void main(String[] args) {
		DocCropWindow dcWindow = null;
		// Handle localization, for now, only support Swedish and
		// American English
		String lang_code = Locale.getDefault().toString();
		if (lang_code.equals("sv_SE")) {
			rBundle = ResourceBundle.getBundle("resource_sv_SE");
		} else {
			rBundle = ResourceBundle.getBundle("resource_en_US");
		}
		dcWindow = new DocCropWindow();
		dcWindow.setVisible(true);

	}

}
