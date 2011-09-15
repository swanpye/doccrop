package mvc.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import mvc.control.Action;
import mvc.control.ActionManager;
import start.DocCrop;

import common.Configuration;

public class SettingsWindow extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5966768841958727097L;
	private static ResourceBundle rBundle = DocCrop.rBundle;
	/**
	 * @author Tomas Toss 31 aug 2011
	 */

	private JTabbedPane tabPane;
	private JPanel programPanel;
	private JPanel algorithmPanel;

	private ActionManager am;
	// Components for program panel
	private JRadioButton imgDirButton;
	private JRadioButton specDirButton;
	private JFileChooser fc;
	private JTextField saveFolderTextField;
	private JButton chooseFolderButton;
	private JRadioButton singleButton;
	private JRadioButton doubleButton;
	private JRadioButton simpleButton;
	private JRadioButton complexButton;
	private JSpinner paddingFieldWidth;
	private JSpinner paddingFieldHeight;
	private JCheckBox previewCheckBox;
	private JCheckBox previewFolderCheckBox;

	// Components for algorithm panel
	private JRadioButton fastButton, standardButton, customButton;
	private JSpinner maxIterations;
	private JSpinner colorSensitivity;
	private JSpinner processImgSize;
	private JSpinner minDocSize;
	private JSpinner filterSize;
	private JSpinner maxLines;
	private JSpinner maxLinesToIntersections;
	private JSpinner maxIntersections;
	private JSpinner houghIterations;
	private JSpinner houghHigh;
	private JSpinner houghLow;

	// Components for the save panel
	private JButton saveButton;
	private JButton saveAndExitButton;
	private JButton cancelButton;
	private SettingsListener listener;

	private Insets spacer = new Insets(2, 5, 2, 5);

	public SettingsWindow() {
		super();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle(rBundle.getString("mvc.view.SettingsWindow.Settings"));
		am = new ActionManager(this, rBundle);
		tabPane = new JTabbedPane();
		tabPane.setLayout(new BorderLayout());
		tabPane.setUI(new BasicTabbedPaneUI() {
			@Override
			protected void installDefaults() {
				super.installDefaults();
				UIManager.put("TabbedPane.contentBorderInsets", new Insets(0,
						0, 0, 0));
			}
		});
		add(tabPane, BorderLayout.CENTER);
		listener = new SettingsListener();
		initProgramPanel();
		initAlgorithmPanel();
		initSavePanel();
		tabPane.addTab(
				rBundle.getString("mvc.view.SettingsWindow.programSettings"),
				programPanel);
		tabPane.addTab(
				rBundle.getString("mvc.view.SettingsWindow.algorithmSettings"),
				algorithmPanel);
		pack();
	}

	private void initProgramPanel() {
		programPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = spacer;
		gc.anchor = GridBagConstraints.LINE_START;

		JPanel cropFilePanel = new JPanel(new GridBagLayout());
		cropFilePanel.setBorder(BorderFactory.createTitledBorder(rBundle
				.getString("mvc.view.SettingsWindow.cropping.folder")));

		imgDirButton = new JRadioButton(am.getAction("setPresetDirectory"));
		attachSettingsListener(imgDirButton, listener);
		specDirButton = new JRadioButton(am.getAction("setUserDirectory"));
		attachSettingsListener(specDirButton, listener);

		JPanel specFolderPanel = new JPanel();
		saveFolderTextField = new JTextField(25);
		attachSettingsListener(saveFolderTextField, listener);
		specFolderPanel.add(saveFolderTextField);
		chooseFolderButton = new JButton(am.getAction("chooseUserDirectory"));
		specFolderPanel.add(chooseFolderButton);

		ButtonGroup folderGroup = new ButtonGroup();
		folderGroup.add(imgDirButton);
		folderGroup.add(specDirButton);

		JPanel previewPanel = new JPanel(new GridBagLayout());
		previewPanel.setBorder(BorderFactory.createTitledBorder(rBundle
				.getString("mvc.view.SettingsWindow.preview")));
		previewCheckBox = new JCheckBox(
				rBundle.getString("mvc.view.SettingsWindow.preview.show"));
		attachSettingsListener(previewCheckBox, listener);
		previewFolderCheckBox = new JCheckBox(
				rBundle.getString("mvc.view.SettingsWindow.preview.save"));
		attachSettingsListener(previewFolderCheckBox, listener);

		JPanel settingsPanel = new JPanel(new GridBagLayout());
		settingsPanel.setBorder(BorderFactory.createTitledBorder(rBundle
				.getString("mvc.view.SettingsWindow.documentSettings")));
		Border border = BorderFactory.createTitledBorder(rBundle
				.getString("mvc.view.CenterPane.documentType"));
		JPanel documentTypePanel = new JPanel();
		documentTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		documentTypePanel.setBorder(border);

		singleButton = new JRadioButton(
				rBundle.getString("mvc.view.CenterPane.single"));
		attachSettingsListener(singleButton, listener);
		doubleButton = new JRadioButton(
				rBundle.getString("mvc.view.CenterPane.double"));
		attachSettingsListener(doubleButton, listener);
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(singleButton);
		buttonGroup1.add(doubleButton);
		documentTypePanel.add(singleButton);
		documentTypePanel.add(doubleButton);

		border = BorderFactory.createTitledBorder(rBundle
				.getString("mvc.view.CenterPane.documentBehavior"));

		JPanel documentBehaviorPanel = new JPanel();
		documentBehaviorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		documentBehaviorPanel.setBorder(border);
		simpleButton = new JRadioButton(
				rBundle.getString("mvc.view.CenterPane.simple"));
		attachSettingsListener(simpleButton, listener);
		complexButton = new JRadioButton(
				rBundle.getString("mvc.view.CenterPane.complex"));
		attachSettingsListener(complexButton, listener);
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(simpleButton);
		buttonGroup2.add(complexButton);
		documentBehaviorPanel.add(simpleButton);
		documentBehaviorPanel.add(complexButton);

		border = BorderFactory.createTitledBorder(rBundle
				.getString("mvc.view.CenterPane.padding"));
		JPanel paddingPanel = new JPanel(new GridBagLayout());
		paddingPanel.setBorder(border);
		paddingFieldWidth = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
		attachSettingsListener(paddingFieldWidth, listener);

		paddingFieldHeight = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
		attachSettingsListener(paddingFieldHeight, listener);
		JLabel xLabel = new JLabel(
				rBundle.getString("mvc.view.CenterPane.width"));
		xLabel.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel yLabel = new JLabel(
				rBundle.getString("mvc.view.CenterPane.height"));
		yLabel.setHorizontalAlignment(SwingConstants.CENTER);

		gc.gridx = 0;
		gc.gridy = 0;
		paddingPanel.add(xLabel, gc);
		gc.gridx = 1;
		paddingPanel.add(yLabel, gc);
		gc.gridy = 1;
		gc.gridx = 0;
		paddingPanel.add(paddingFieldWidth, gc);
		gc.gridx = 1;
		paddingPanel.add(paddingFieldHeight, gc);

		gc.gridx = 0;
		gc.gridy = 0;
		settingsPanel.add(documentTypePanel, gc);
		gc.gridx = 1;
		settingsPanel.add(documentBehaviorPanel, gc);
		gc.gridx = 2;
		gc.gridheight = 2;
		settingsPanel.add(paddingPanel, gc);
		gc.gridheight = 1;

		gc.gridx = 0;
		gc.gridy = 0;
		cropFilePanel.add(imgDirButton, gc);
		gc.gridy = 1;
		cropFilePanel.add(specDirButton, gc);
		gc.gridy = 2;
		cropFilePanel.add(specFolderPanel, gc);
		gc.gridx = 0;
		gc.gridy = 0;
		programPanel.add(cropFilePanel, gc);
		previewPanel.add(previewCheckBox);
		gc.gridy = 1;
		previewPanel.add(previewFolderCheckBox, gc);
		programPanel.add(previewPanel, gc);
		gc.gridy = 2;
		programPanel.add(settingsPanel, gc);

		gc.gridy = 3;
		gc.gridx = 1;
		gc.weightx = 1;
		gc.weighty = 1;
		programPanel.add(Box.createGlue(), gc);
		loadProgramSettings();
	}

	private void initAlgorithmPanel() {
		algorithmPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();

		gc.insets = spacer;
		gc.anchor = GridBagConstraints.LINE_START;
		JPanel radioButtonsPanel = new JPanel(new GridBagLayout());
		fastButton = new JRadioButton("Snabb exekvering");
		standardButton = new JRadioButton("Normal exekvering");
		customButton = new JRadioButton("Anpassad exekvering");
		ButtonGroup algSettingsGroup = new ButtonGroup();
		algSettingsGroup.add(fastButton);
		algSettingsGroup.add(standardButton);
		algSettingsGroup.add(customButton);
		radioButtonsPanel.add(fastButton, gc);
		gc.gridx = 1;
		radioButtonsPanel.add(standardButton, gc);
		gc.gridx = 2;
		radioButtonsPanel.add(customButton, gc);

		// Container for the different setting types
		JPanel settingsPanel = new JPanel(new GridBagLayout());

		// General algorithm settings
		JPanel generalSettingsPanel = new JPanel(new GridBagLayout());
		generalSettingsPanel.setBorder(BorderFactory
				.createTitledBorder("Generella algoritminställningar"));
		JLabel maxIterationsLabel = new JLabel(
				"Maximalt antal iterationer (1-100)");
		maxIterations = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
		// maxIterations.setMinMaxValues(1, 100);
		gc.gridx = 0;
		gc.gridy = 0;
		generalSettingsPanel.add(maxIterationsLabel, gc);
		gc.gridx = 1;
		generalSettingsPanel.add(maxIterations, gc);
		JLabel filterSizeLabel = new JLabel(
				"Storlek på morfologiskt filter (1-100 pixlar)");
		filterSize = new JSpinner(new SpinnerNumberModel(1,1,100,1));
		gc.gridx = 2;
		generalSettingsPanel.add(filterSizeLabel, gc);
		gc.gridx = 3;
		generalSettingsPanel.add(filterSize, gc);
		JLabel processImgSizeLabel = new JLabel(
				"Bildens arbetsstorlek (1-999 pixlar)");
		processImgSize = new JSpinner(new SpinnerNumberModel(100,100,999,1));
		gc.gridx = 0;
		gc.gridy = 1;
		generalSettingsPanel.add(processImgSizeLabel, gc);
		gc.gridx = 1;
		generalSettingsPanel.add(processImgSize, gc);
		JLabel minDocSizeLabel = new JLabel(
				"Minsta dokumentstorlek (1-100% av bildyta)");
		minDocSize = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
		gc.gridx = 2;
		generalSettingsPanel.add(minDocSizeLabel, gc);
		gc.gridx = 3;
		generalSettingsPanel.add(minDocSize, gc);

		// Linjedetektionsinställningar
		JPanel lineDetectionPanel = new JPanel(new GridBagLayout());
		lineDetectionPanel.setBorder(BorderFactory
				.createTitledBorder("Linjeinställningar"));
		JLabel maxLinesLabel = new JLabel("Maximalt antal linjer (1-999)");
		maxLines = new JSpinner(new SpinnerNumberModel(1,1,999,1));
		gc.gridx = 0;
		gc.gridy = 0;
		lineDetectionPanel.add(maxLinesLabel, gc);
		gc.gridx = 1;
		lineDetectionPanel.add(maxLines, gc);
		JLabel houghIterationsLabel = new JLabel(
				"Antal Hough-iterationer (1-100)");
		houghIterations = new JSpinner(new SpinnerNumberModel(1,1,100,1));
		gc.gridx = 2;
		lineDetectionPanel.add(houghIterationsLabel, gc);
		gc.gridx = 3;
		lineDetectionPanel.add(houghIterations, gc);
		JLabel houghHighLabel = new JLabel("Övre linjestyrka (1-100)");
		houghHigh = new JSpinner(new SpinnerNumberModel(1,1,100,1));
		gc.gridx = 0;
		gc.gridy = 1;
		lineDetectionPanel.add(houghHighLabel, gc);
		gc.gridx = 1;
		lineDetectionPanel.add(houghHigh, gc);
		JLabel houghLowLabel = new JLabel("Undre linjestyrka (1-100)");
		houghLow = new JSpinner(new SpinnerNumberModel(1,1,100,1));
		gc.gridx = 2;
		lineDetectionPanel.add(houghLowLabel, gc);
		gc.gridx = 3;
		lineDetectionPanel.add(houghLow, gc);

		JPanel intersectionPanel = new JPanel(new GridBagLayout());
		intersectionPanel.setBorder(BorderFactory
				.createTitledBorder("Skärningspunktinställningar"));
		JLabel maxLinesToIntersectionLabel = new JLabel(
				"Linjer/skärningspunkter (1-999)");
		maxLinesToIntersections = new JSpinner(new SpinnerNumberModel(1,1,999,1));;
		gc.gridx = 0;
		gc.gridy = 0;
		intersectionPanel.add(maxLinesToIntersectionLabel, gc);
		gc.gridx = 1;
		intersectionPanel.add(maxLinesToIntersections, gc);
		JLabel maxIntersectionsLabel = new JLabel(
				"Maximalt antal skärningspunkter (1-999)");
		maxIntersections = new JSpinner(new SpinnerNumberModel(1,1,999,1));
		gc.gridx = 2;
		intersectionPanel.add(maxIntersectionsLabel, gc);
		gc.gridx = 3;
		intersectionPanel.add(maxIntersections, gc);
		JLabel colorSensitivityLabel = new JLabel("Färgkänslighet (0-255)");
		colorSensitivity = new JSpinner(new SpinnerNumberModel(0,0,255,1));
		gc.gridx = 0;
		gc.gridy = 1;
		intersectionPanel.add(colorSensitivityLabel, gc);
		gc.gridx = 1;
		intersectionPanel.add(colorSensitivity, gc);

		
		//Attach all listeners
		attachSettingsListener(fastButton, listener);
		attachSettingsListener(standardButton, listener);
		attachSettingsListener(customButton, listener);
		attachSettingsListener(maxIterations, listener);
		attachSettingsListener(colorSensitivity, listener);
		attachSettingsListener(processImgSize, listener);
		attachSettingsListener(minDocSize, listener);
		attachSettingsListener(filterSize, listener);
		attachSettingsListener(maxLines, listener);
		attachSettingsListener(maxLinesToIntersections, listener);
		attachSettingsListener(houghLow, listener);
		attachSettingsListener(houghHigh, listener);
		attachSettingsListener(houghIterations, listener);
		attachSettingsListener(maxIntersections, listener);
		
	
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridheight = 2;
		settingsPanel.add(generalSettingsPanel, gc);
		gc.gridy = 4;
		settingsPanel.add(lineDetectionPanel, gc);
		gc.gridy = 8;
		settingsPanel.add(intersectionPanel, gc);

		gc.anchor = GridBagConstraints.NORTHWEST;
		gc.gridx = 0;
		gc.gridy = 0;
		algorithmPanel.add(radioButtonsPanel, gc);
		gc.gridx = 0;
		gc.gridy = 2;
		algorithmPanel.add(settingsPanel, gc);
		gc.gridx = 1;
		gc.gridy = 3;
		gc.weighty = 1;
		gc.weightx = 1;
		algorithmPanel.add(Box.createGlue(), gc);
	}

	private void initSavePanel() {
		JPanel savePanel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = spacer;
		gc.anchor = GridBagConstraints.NORTHWEST;
		saveButton = new JButton(am.getAction("saveSettings"));
		saveAndExitButton = new JButton(am.getAction("saveAndExitSettings"));
		cancelButton = new JButton(am.getAction("cancelAndExitSettings"));
		savePanel.add(saveButton, gc);
		gc.gridx = 1;
		savePanel.add(saveAndExitButton, gc);
		gc.gridx = 2;
		savePanel.add(cancelButton, gc);
		gc.gridx = 3;
		gc.weightx = 1;
		savePanel.add(Box.createGlue(), gc);
		add(savePanel, BorderLayout.PAGE_END);
	}

	public void loadProgramSettings() {
		Configuration.loadApplicationProperties();
		if (Configuration
				.getProperty("settings.program.cropping.isDefinedPath").equals(
						"FALSE")) {
			imgDirButton.setSelected(true);
			am.getAction("chooseUserDirectory").setEnabled(false);
			saveFolderTextField.setEnabled(false);
		} else {
			specDirButton.setSelected(true);
			saveFolderTextField.setText(Configuration
					.getProperty("settings.program.cropping.definedPath"));
		}

		if (Configuration.getProperty("settings.program.documentType").equals(
				"SINGLE")) {
			singleButton.setSelected(true);
		} else {
			doubleButton.setSelected(true);
		}

		if (Configuration.getProperty("settings.program.documentBehaviour")
				.equals("SIMPLE")) {
			simpleButton.setSelected(true);
		} else {
			complexButton.setSelected(true);
		}
		if (Configuration.getProperty("settings.program.preview.show").equals(
				"TRUE"))
			previewCheckBox.setSelected(true);
		else
			previewCheckBox.setSelected(false);
		if (Configuration.getProperty("settings.program.preview.folder")
				.equals("TRUE"))
			previewFolderCheckBox.setSelected(true);
		else
			previewFolderCheckBox.setSelected(false);

		paddingFieldWidth.setValue(Integer.parseInt(Configuration
				.getProperty("settings.program.padding.width")));
		paddingFieldHeight.setValue(Integer.parseInt(Configuration
				.getProperty("settings.program.padding.height")));

		am.getAction("saveSettings").setEnabled(false);
		am.getAction("saveAndExitSettings").setEnabled(false);
	}

	@Action
	public void saveSettings() {

		if (imgDirButton.isSelected())
			Configuration.setProperty(
					"settings.program.cropping.isDefinedPath", "FALSE");
		else
			Configuration.setProperty(
					"settings.program.cropping.isDefinedPath", "TRUE");

		Configuration.setProperty("settings.program.cropping.definedPath",
				saveFolderTextField.getText());
		Configuration.setProperty("settings.program.cropping.definedPath",
				saveFolderTextField.getText());
		Configuration.setProperty("settings.program.cropping.definedPath",
				saveFolderTextField.getText());

		if (simpleButton.isSelected())
			Configuration.setProperty("settings.program.documentBehaviour",
					"SIMPLE");
		else
			Configuration.setProperty("settings.program.documentBehaviour",
					"COMPLEX");

		if (singleButton.isSelected())
			Configuration
					.setProperty("settings.program.documentType", "SINGLE");
		else
			Configuration
					.setProperty("settings.program.documentType", "DOUBLE");

		if (previewCheckBox.isSelected())
			Configuration.setProperty("settings.program.preview.show", "TRUE");
		else
			Configuration.setProperty("settings.program.preview.show", "FALSE");

		if (previewFolderCheckBox.isSelected())
			Configuration
					.setProperty("settings.program.preview.folder", "TRUE");
		else
			Configuration.setProperty("settings.program.preview.folder",
					"FALSE");

		Configuration.setProperty("settings.program.padding.width",
				paddingFieldWidth.getValue().toString());
		Configuration.setProperty("settings.program.padding.height",
				paddingFieldHeight.getValue().toString());

		Configuration.saveApplicationProperties();

		am.getAction("saveSettings").setEnabled(false);
		am.getAction("saveAndExitSettings").setEnabled(false);
	}

	@Action
	public void saveAndExitSettings() {
		saveSettings();
		dispose();
	}

	@Action
	public void cancelAndExitSettings() {
		dispose();
	}

	@Action
	public void setPresetDirectory() {
		am.getAction("chooseUserDirectory").setEnabled(false);
		saveFolderTextField.setEnabled(false);
	}

	@Action
	public void chooseUserDirectory() {
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int retValue = fc.showOpenDialog(this);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			String directoryPath = fc.getSelectedFile().toString();
			saveFolderTextField.setText(directoryPath);
			listener.actionPerformed(null);
		}

	}

	@Action
	public void setUserDirectory() {
		am.getAction("chooseUserDirectory").setEnabled(true);
		saveFolderTextField.setEnabled(true);
	}

	@SuppressWarnings("serial")
	private class SettingsListener extends AbstractAction implements
			KeyListener, ChangeListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			am.getAction("saveSettings").setEnabled(true);
			am.getAction("saveAndExitSettings").setEnabled(true);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			am.getAction("saveSettings").setEnabled(true);
			am.getAction("saveAndExitSettings").setEnabled(true);

		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			int key = arg0.getKeyChar();
			System.out.println(key);
			if ((key >= '0' && key <= '9') || key == KeyEvent.VK_BACK_SPACE
					|| key == KeyEvent.VK_DELETE) {
				am.getAction("saveSettings").setEnabled(true);
				am.getAction("saveAndExitSettings").setEnabled(true);
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			int key = arg0.getKeyChar();
			System.out.println(key);
			if ((key >= '0' && key <= '9') || key == KeyEvent.VK_BACK_SPACE
					|| key == KeyEvent.VK_DELETE) {
				am.getAction("saveSettings").setEnabled(true);
				am.getAction("saveAndExitSettings").setEnabled(true);
			}
		}

		@Override
		public void keyPressed(KeyEvent arg0) {
			int key = arg0.getKeyChar();
			System.out.println(key);
			if ((key >= '0' && key <= '9') || key == KeyEvent.VK_BACK_SPACE
					|| key == KeyEvent.VK_DELETE) {
				am.getAction("saveSettings").setEnabled(true);
				am.getAction("saveAndExitSettings").setEnabled(true);
			}
		}
	};

	private void attachSettingsListener(JComponent component,
			SettingsListener listener) {
		if (component instanceof JButton) {
			((JButton) component).addActionListener(listener);
		} else if (component instanceof JTextField) {
			component.addKeyListener(listener);
			((JTextField) component).addActionListener(listener);
		} else if (component instanceof JRadioButton) {
			((JRadioButton) component).addActionListener(listener);
		} else if (component instanceof JCheckBox) {
			((JCheckBox) component).addActionListener(listener);
		} else if (component instanceof JSpinner) {
			((JSpinner) component).addChangeListener(listener);
			JComponent editor = ((JSpinner) component).getEditor();
			if (editor instanceof JSpinner.DefaultEditor) {
				((JSpinner.DefaultEditor) editor).getTextField()
						.addKeyListener(listener);
			}
		}
	}

	public static void main(String[] args) {
		String lang_code = Locale.getDefault().toString();
		if (lang_code.equals("sv_SE")) {
			rBundle = ResourceBundle.getBundle("resource_sv_SE");
		} else {
			rBundle = ResourceBundle.getBundle("resource_en_US");
		}
		SettingsWindow win = new SettingsWindow();
		win.pack();
		win.setVisible(true);
	}

}