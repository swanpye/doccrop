package mvc.model;

import javax.swing.table.AbstractTableModel;

import start.DocCrop;

/**
 * This table model is used to illustrate {@link #common.ImageBatches} information in table
 * form
 * 
 * @author Tomas Toss
 */
@SuppressWarnings("serial")
public class BatchTableModel extends AbstractTableModel {
	private ImageBatches batches;
	private String[] columnNames = {
			DocCrop.rBundle
					.getString("mvc.model.BatchTableModel.columns.series"),
			DocCrop.rBundle
					.getString("mvc.model.BatchTableModel.columns.number"),
			DocCrop.rBundle
					.getString("mvc.model.BatchTableModel.columns.fileformat"),
			DocCrop.rBundle
					.getString("mvc.model.BatchTableModel.columns.status") };
	private ImageBatch[] batch;

	public BatchTableModel() {
		batches = new ImageBatches();
		batch = new ImageBatch[0];
	}

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
			 String[] fileSuffixes = batch[rowIndex].getFileSuffixes();
			 String suffixes = "";
			 for(int i = 0; i < fileSuffixes.length-1; i++) {
				 suffixes += fileSuffixes[i] + ", ";
			 }
			 return suffixes + fileSuffixes[fileSuffixes.length-1];
		case 3:
			return batch[rowIndex].getProgressBar();
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
		if (batches == null) {
			this.batch = new ImageBatch[0];
		} else {
			this.batch = batches.getBatches();
		}
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
