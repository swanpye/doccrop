package mvc.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import mvc.control.Action;
import mvc.control.ActionManager;
import mvc.control.ImageBatchController;
import mvc.model.ImageBatches;
import start.DocCrop;


@SuppressWarnings("serial")
public class DocCropWindow extends JFrame {
	private ResourceBundle rBundle = DocCrop.rBundle;
	private ActionManager actionManager = null;
	private ImageBatchController controller;
	private ImageBatches model = new ImageBatches();
	private CenterPane centerPane = null;
	private LeftPane leftPane = null;
	static PropertyChangeListener propListener = null;

	public DocCropWindow() {
		// Set basic properties of the DocCropWindow
		super("DocCrop");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(600, 400));
		setPreferredSize(new Dimension(600,400));
		actionManager = new ActionManager(this, rBundle);
		controller = new ImageBatchController();		
		initMenuBar();
		initContentPane();
		controller.addModel(model);
		controller.addView(leftPane);
		controller.addView(centerPane);
		pack();
	}

	/**
	 * Initialize the menu bar of the DocCropWindow
	 */
	private void initMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu fileMenu = new JMenu(rBundle.getString("mvc.view.jmenu.file"));
		JMenuItem exitItem = new JMenuItem(actionManager.getAction("exit"));
		fileMenu.add(exitItem);
		JMenu editMenu = new JMenu(rBundle.getString("mvc.view.jmenu.edit"));
		JMenu helpMenu = new JMenu(rBundle.getString("mvc.view.jmenu.help"));
		JMenuItem aboutItem = new JMenuItem(actionManager.getAction("about"));
		JMenuItem helpItem = new JMenuItem(actionManager.getAction("help"));
		helpMenu.add(aboutItem);
		helpMenu.add(helpItem);

		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(helpMenu);
		this.setJMenuBar(mb);
	}

	/**
	 * Initialize the content pane of the DocCropWindow
	 */
	private void initContentPane() {
		leftPane = new LeftPane(controller);
		centerPane = new CenterPane(controller,model);
		add(leftPane, BorderLayout.LINE_START);
		add(centerPane, BorderLayout.CENTER);
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
}