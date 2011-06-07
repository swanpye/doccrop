package mvc.view;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A simple table cell renderer for Image batches, showing tooltip for the model
 * using it. Note: only works for models using {@link String} data.
 * 
 * @author Tomas
 * 
 */
@SuppressWarnings("serial")
class BatchRenderer extends JLabel implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object batchPath, boolean isSelected, boolean hasFocus, int row,
			int col) {
		setToolTipText((String) batchPath);
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setOpaque(true);
		} else {
			setBackground(table.getBackground());
			setOpaque(true);
		}
		setText((String) batchPath);
		return this;
	}

}
