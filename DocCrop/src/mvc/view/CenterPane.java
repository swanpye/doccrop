package mvc.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.TableCellRenderer;

import mvc.AbstractViewPanel;
import mvc.control.Action;
import mvc.control.ActionManager;
import mvc.control.ImageBatchController;
import mvc.model.BatchTableModel;
import mvc.model.ImageBatch;
import mvc.model.ImageBatch.BatchProgressBar;
import mvc.model.ImageBatch.DocumentBehavior;
import mvc.model.ImageBatches;
import start.DocCrop;

import common.Document.DocumentType;

@SuppressWarnings("serial")
public class CenterPane extends AbstractViewPanel {

	private ResourceBundle rBundle = DocCrop.rBundle;
	private ActionManager actionManager = null;
	private JTable batchTable = null;
	private BatchTableModel tableModel;
	private JTabbedPane tabPane = null;
	private JPanel overviewCard = null;
	private JPanel settingsPanel = null;
	private JRadioButton singleButton, doubleButton, simpleButton,
			complexButton;
	private JButton settingsButton = null;
	private IntegerField paddingFieldX, paddingFieldY;
	private JPanel statisticsCard = null;
	private JLabel batchLabel = null;
	private CollapsablePanel colPanel = null;

	/**
	 * Create CenterPane that visualizes the data stored in the models connected
	 * to the supplied controller. The controller is automatically connected to
	 * this view, so explicit code for connecting this view is neccesary
	 * 
	 * @param controller
	 *            A controller connected with a suitable model (
	 *            <code>ImageBatches</code>, or a subclass of it.)
	 */
	public CenterPane(ImageBatchController controller) {
		super();
		controller.addView(this);
		tableModel = new BatchTableModel();
		setLayout(new BorderLayout());
		actionManager = new ActionManager(this, rBundle);
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

		initOverviewCard();
		initStatisticsCard();
		add(tabPane, BorderLayout.CENTER);
	}

	private void initOverviewCard() {
		overviewCard = new JPanel();
		overviewCard.setLayout(new BorderLayout());
		batchLabel = new JLabel(
				rBundle.getString("mvc.view.CenterPane.batchLabel"));
		batchLabel.setHorizontalAlignment(SwingConstants.CENTER);
		batchTable = new JTable(tableModel);
		batchTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						ImageBatch selectedBatch = tableModel
								.getBatchAt(batchTable.getSelectedRow());
						if (selectedBatch != null) {
							actionManager.getAction("apply").setEnabled(true);
							switch (selectedBatch.getDocBehavior()) {
							case SIMPLE:
								simpleButton.setSelected(true);
								break;
							case COMPLEX:
								complexButton.setSelected(true);
								break;
							default:
								System.out
										.println("Document behavior should be set");
							}

							switch (selectedBatch.getDocType()) {
							case SINGLE_PAGE:
								singleButton.setSelected(true);
								break;
							case DOUBLE_PAGE:
								doubleButton.setSelected(true);
								break;
							default:
								System.out
										.println("Document type should be set");
							}
							paddingFieldX.setText(selectedBatch.getPadding()[0]
									+ "");
							paddingFieldY.setText(selectedBatch.getPadding()[1]
									+ "");
						}

					}
				});

		batchTable.getColumnModel().getColumn(0)
				.setCellRenderer(new BatchRenderer());
		batchTable.getColumnModel().getColumn(3)
				.setCellRenderer(new TableCellRenderer() {

					@Override
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						BatchProgressBar b = (BatchProgressBar) value;

						return b;
					}
				});
		batchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tableScroll = new JScrollPane(batchTable);
		batchTable.setFillsViewportHeight(true);
		overviewCard.add(batchLabel, BorderLayout.PAGE_START);
		overviewCard.add(tableScroll, BorderLayout.CENTER);
		initSettingsPanel();
		tabPane.addTab(rBundle.getString("mvc.view.CenterPane.tabPane.tab1"),
				overviewCard);
	}

	private void initSettingsPanel() {
		settingsPanel = new JPanel();
		settingsPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		Border border = BorderFactory.createTitledBorder(rBundle
				.getString("mvc.view.CenterPane.documentType"));
		JPanel documentTypePanel = new JPanel();
		documentTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		documentTypePanel.setBorder(border);

		class MyActionListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.getAction("apply").setEnabled(true);
			}
		}
		;
		singleButton = new JRadioButton(
				rBundle.getString("mvc.view.CenterPane.single"));
		singleButton.addActionListener(new MyActionListener());
		doubleButton = new JRadioButton(
				rBundle.getString("mvc.view.CenterPane.double"));
		doubleButton.addActionListener(new MyActionListener());
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
		simpleButton.addActionListener(new MyActionListener());
		complexButton = new JRadioButton(
				rBundle.getString("mvc.view.CenterPane.complex"));
		complexButton.addActionListener(new MyActionListener());
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(simpleButton);
		buttonGroup2.add(complexButton);
		documentBehaviorPanel.add(simpleButton);
		documentBehaviorPanel.add(complexButton);

		border = BorderFactory.createTitledBorder(rBundle
				.getString("mvc.view.CenterPane.padding"));
		JPanel paddingPanel = new JPanel(new GridBagLayout());
		paddingPanel.setBorder(border);
		paddingFieldX = new IntegerField();
		paddingFieldY = new IntegerField();
		paddingFieldX.addActionListener(new MyActionListener());
		paddingFieldY.addActionListener(new MyActionListener());

		paddingFieldX.setPreferredSize(new Dimension(30, 23));
		paddingFieldX.setColumns(3);
		paddingFieldY.setPreferredSize(new Dimension(30, 23));
		paddingFieldY.setColumns(3);
		JLabel xLabel = new JLabel(
				rBundle.getString("mvc.view.CenterPane.width"));
		xLabel.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel yLabel = new JLabel(
				rBundle.getString("mvc.view.CenterPane.height"));
		yLabel.setHorizontalAlignment(SwingConstants.CENTER);

		paddingPanel.add(xLabel, c);
		c.gridx = 1;
		paddingPanel.add(yLabel, c);
		c.gridy = 1;
		c.gridx = 0;
		paddingPanel.add(paddingFieldX, c);
		c.gridx = 1;
		paddingPanel.add(paddingFieldY, c);

		c.gridx = 0;
		c.gridy = 0;
		settingsPanel.add(documentTypePanel, c);
		c.gridx = 1;
		settingsPanel.add(documentBehaviorPanel, c);
		c.gridx = 2;
		settingsPanel.add(paddingPanel, c);

		settingsButton = new JButton(actionManager.getAction("apply"));

		c.gridy = 1;
		c.gridx = 0;
		settingsPanel.add(settingsButton, c);
		actionManager.getAction("apply").setEnabled(false);

		JPanel placeHolderWest = new JPanel(new BorderLayout());
		placeHolderWest.add(settingsPanel, BorderLayout.WEST);
		colPanel = new CollapsablePanel(CollapsablePanel.NORTH);
		colPanel.add(placeHolderWest);
		colPanel.setCollapsButtonSize(7);
		add(colPanel, BorderLayout.PAGE_END);
	}

	private void initStatisticsCard() {
		statisticsCard = new JPanel();
		tabPane.addTab(rBundle.getString("mvc.view.CenterPane.tabPane.tab3"),
				statisticsCard);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		String eventName = evt.getPropertyName();
		if (eventName.equals(ImageBatchController.NEW_BATCHES)) {
			tableModel.updateBatches((ImageBatches) evt.getNewValue());
		} else if (eventName.equals(ImageBatchController.PROGRESS)) {
			tableModel.updateProgress();
		}
	}

	@Action
	public void apply() {
		ImageBatch b = tableModel.getBatchAt(batchTable.getSelectedRow());
		if (simpleButton.isSelected()) {
			b.setDocBehavior(DocumentBehavior.SIMPLE);
		} else {
			b.setDocBehavior(DocumentBehavior.COMPLEX);
		}
		if (singleButton.isSelected()) {
			b.setDocType(DocumentType.SINGLE_PAGE);
		} else {
			b.setDocType(DocumentType.DOUBLE_PAGE);
		}
		int xPad, yPad;
		try {
			xPad = Integer.parseInt(paddingFieldX.getText());
		} catch (NumberFormatException ne) {
			xPad = 0;
			paddingFieldX.setInt(0);
		}
		try {
			yPad = Integer.parseInt(paddingFieldY.getText());
		} catch (NumberFormatException ne) {
			yPad = 0;
			paddingFieldY.setInt(0);
		}

		b.setPadding(new int[] { xPad, yPad });
		actionManager.getAction("apply").setEnabled(false);
	}
}
