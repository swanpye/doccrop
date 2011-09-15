package mvc.view;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import mvc.control.Action;
import mvc.control.ActionManager;
import mvc.control.ImageBatchController;
import start.DocCrop;

@SuppressWarnings("serial")
public class DocCropWindow extends JFrame {
	private ResourceBundle rBundle = DocCrop.rBundle;
	private ActionManager actionManager = null;
	private ImageBatchController controller;

	static PropertyChangeListener propListener = null;

	public DocCropWindow() {
		// Set basic properties of the DocCropWindow
		super("DocCrop");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		URL url = getClass().getResource("/DocCrop.jar/icons/icon2.gif");
		url = getClass().getResource("icon2.gif");
		System.out.println(url);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
		setMinimumSize(new Dimension(700, 400));
		setPreferredSize(new Dimension(700, 400));
		actionManager = new ActionManager(this, rBundle);
		
		initMenuBar();
		//Set up controller, model and views.
		controller = new ImageBatchController();
		DocCropPane docCropPane = new DocCropPane(controller);
		add(docCropPane, BorderLayout.CENTER);
		pack();
	}

	/**
	 * Initialize the menu bar of the DocCropWindow
	 */
	private void initMenuBar() {
		JMenuBar mb = new JMenuBar();
		//Create file menu, and add items
		JMenu fileMenu = new JMenu(rBundle.getString("mvc.view.jmenu.file"));
		JMenuItem exitItem = new JMenuItem(actionManager.getAction("exit"));
		JMenuItem settingsItem = new JMenuItem(
				actionManager.getAction("settings"));
		fileMenu.add(settingsItem);
		fileMenu.add(exitItem);
		
		//Create help menu, and add items
		JMenu helpMenu = new JMenu(rBundle.getString("mvc.view.jmenu.help"));
		JMenuItem aboutItem = new JMenuItem(actionManager.getAction("about"));
		JMenuItem helpItem = new JMenuItem(actionManager.getAction("help"));
		helpMenu.add(aboutItem);
		helpMenu.add(helpItem);
		
		//Populate the menu bar
		mb.add(fileMenu);
		mb.add(helpMenu);
		setJMenuBar(mb);
	}

	/**
	 * Exit program
	 */
	@Action
	public void exit() {
		System.exit(0);
	}

	/**
	 * Show about window
	 */
	@Action
	public void about() {
		JFrame about = new JFrame("About DocCrop");
		about.setMinimumSize(new Dimension(300, 200));
		about.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		about.setVisible(true);
	}

	/**
	 * Show help window
	 */
	@Action
	public void help() {
		JFrame help = new JFrame("Help");
		help.setMinimumSize(new Dimension(300, 200));
		help.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		help.setVisible(true);
	}

	@Action
	public void settings() {
		SettingsWindow settings = new SettingsWindow();
		settings.setResizable(false);
		settings.setAlwaysOnTop(true);
		settings.setModalityType(ModalityType.APPLICATION_MODAL);
		settings.setVisible(true);
	}
}