package mvc.model;

import javax.swing.table.AbstractTableModel;


/**
 * This table model is used to illustrate {@link #common.ImageBatches} information in table
 * form
 * 
 * @author Tomas Toss
 */
@SuppressWarnings("serial")
public class BatchTableModel extends AbstractTableModel {
	private ImageBatches batches;
	private String[] columnNames = { "Serie", "Antal bilder", "Filformat","Status" };
	private ImageBatch[] batch;

	public BatchTableModel(ImageBatches batches) {
		this.batches = batches;
		batch = this.batches.getBatches();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return batch.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return batch[rowIndex].getBatchPath();
		case 1:
			return batch[rowIndex].getNumberOfFiles();
		case 2:
			return ".png";
		case 3: return batch[rowIndex].getProgressBar();
		default:
			return null;
		}
	}

	public ImageBatch getBatchAt(int rowIndex) {
		if (batch == null || rowIndex < 0 || rowIndex > batch.length)
			return null;
		else
			return batch[rowIndex];
	}

	/**
	 * Update the batch information in the table model
	 * 
	 * @param batches
	 */
	public void updateBatches(ImageBatches batches) {
		this.batches = batches;
		this.batch = batches.getBatches();
		fireTableDataChanged();
	}

	/**
	 * Update the progress information in the table model
	 * 
	 * @param batches
	 */
	public void updateProgress() {
		fireTableDataChanged();
	}

}
