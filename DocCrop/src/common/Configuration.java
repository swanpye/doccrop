package common;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for the docCrop project, supplying functionality to load
 * and store application properties to file
 * 
 * @author Tomas
 */
public class Configuration {

	/**
	 * Application properties, can be loaded from properties file
	 */
	static private Properties appProperties;
	/**
	 * Standard setttings for the application if no other properties are loaded
	 */
	static private Properties defaultProperties;
	static private String appPropertiesPath = System.getProperty("user.home") + File.separator
			+ "doccrop.properties";

	static {
		defaultProperties = new Properties();

		// Default width and height
		int width = 650;
		int height = 300;

		// Get screen size and center of screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - width) / 2;
		int y = (dim.height - height) / 2;

		defaultProperties.setProperty("window.x", Integer.toString(x));
		defaultProperties.setProperty("window.y", Integer.toString(y));
		defaultProperties.setProperty("window.width", Integer.toString(width));
		defaultProperties
				.setProperty("window.height", Integer.toString(height));
		defaultProperties.setProperty(
				"settings.program.cropping.userDefinedPath.", "FALSE");
		defaultProperties.setProperty("settings.program.cropping.definedPath",
				"./");
		defaultProperties
				.setProperty("settings.program.documentType", "SINGLE");
		defaultProperties.setProperty("settings.program.documentBehaviour",
				"SIMPLE");
		defaultProperties.setProperty("settings.program.padding.width", "0");
		defaultProperties.setProperty("settings.program.padding.height", "0");
		appProperties = new Properties(defaultProperties);
	}

	/**
	 * Loads properties from application properties file.
	 */
	public static void loadApplicationProperties() {

		appProperties = new Properties();
		try {
			FileInputStream in = new FileInputStream(appPropertiesPath);
			appProperties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save application properties to the file.
	 */
	public static void saveApplicationProperties() {
		try {

			FileOutputStream out = new FileOutputStream(appPropertiesPath);
			appProperties.store(out, "---No Comment---");
		} catch (Exception e) {
			System.err.println("Error saving properties: " + e);
		}
	}

	/**
	 * Get the application properties
	 * 
	 * @return properties from file (or default properties)
	 */
	public static Properties getApplicationProperties() {
		return new Properties(appProperties);
	}

	/**
	 * Returns the value of a property matching given key or a default value if
	 * the property is not found.
	 * 
	 * @param key
	 *            Key of the property to find
	 * @return value of the property
	 */
	public static String getProperty(String key) {
		return appProperties.getProperty(key);
	}

	/**
	 * Sets a given property. An already existing property will be overwritten.
	 * 
	 * @param key
	 *            Key of the property to set
	 * @param value
	 *            Value of the property
	 */
	public static void setProperty(String key, String value) {
		appProperties.setProperty(key, value);
	}

	// Window specialized functions

	/**
	 * Stores the window dimension in the properties
	 * 
	 * @param size
	 *            The dimension of the window
	 */
	private static void setWindowDimension(Dimension size) {
		if (size != null) {
			setProperty("window.width", Integer.toString(size.width));
			setProperty("window.height", Integer.toString(size.height));
		}
	}

	/**
	 * Returns the window dimension stored in the properties file.
	 * 
	 * @return Size of the window
	 */
	public static Dimension getWindowDimension() {
		Dimension size = null;
		try {
			String w = getProperty("window.width");
			String h = getProperty("window.height");
			if ((w != null) && (h != null)) {
				int width = Integer.parseInt(w);
				int height = Integer.parseInt(h);
				size = new Dimension(width, height);
			}
		} catch (Exception e) {
			// The conversion might have failed. Just catch this and return
			// null.
		}
		return size;
	}

	/**
	 * Stores the window position in the properties
	 * 
	 * @param position
	 *            The position of the window
	 */
	public static void setWindowPosition(Point position) {
		if (position != null) {
			setProperty("window.x", Integer.toString(position.x));
			setProperty("window.y", Integer.toString(position.y));
		}
	}

	/**
	 * Returns the window position stored in the properties file.
	 * 
	 * @return Position of the window
	 */
	public static Point getWindowPosition() {
		Point position = null;
		try {
			String x = getProperty("window.x");
			String y = getProperty("window.y");
			if ((x != null) && (y != null)) {
				int xPos = Integer.parseInt(x);
				int yPos = Integer.parseInt(y);
				position = new Point(xPos, yPos);
			}
		} catch (Exception e) {
			// The conversion might have failed. Just catch this and return
			// null.
		}
		return position;
	}

	/**
	 * Stores the window state (size, location) in the properties file.
	 * 
	 * @param window
	 *            The window holding information about size and location.
	 */
	public static void setWindowState(Component window) {
		if (window != null) {
			setWindowDimension(window.getSize());
			setWindowPosition(window.getLocation());
		}
	}

	public static void main(String[] args) {

		loadApplicationProperties();
		Properties appProp = getApplicationProperties();
		System.out.println(appProp
				.getProperty("settings.program.documentBehaviour"));
		setProperty("settings.program.documentBehaviour", "COMPLEX");
		saveApplicationProperties();
	}
}
