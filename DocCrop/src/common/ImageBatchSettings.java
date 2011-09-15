package common;

import mvc.model.ImageBatch.DocumentBehavior;

import common.Document.DocumentType;


/**
 * A collection class containing miscellaneous settings defining an <code>ImageBatch</code>
 * @author Tomas
 *
 */
public class ImageBatchSettings {

	
	private int paddingWidth, paddingHeight;
	private DocumentBehavior behaviour;
	private DocumentType type;
	private String[] fileFilter;

	public ImageBatchSettings() {
		this.paddingHeight = 0;
		this.paddingWidth = 0;
		this.behaviour = DocumentBehavior.SIMPLE;
		this.type = DocumentType.SINGLE_PAGE;
		this.fileFilter = new String[] {".png",".jpg","jpeg",".tif"};
	}
	public ImageBatchSettings(int paddingWidth,int paddingHeight, DocumentBehavior behaviour, DocumentType type, String[] fileFilter) {
		this.paddingHeight = paddingHeight;
		this.paddingWidth = paddingWidth;
		this.behaviour = behaviour;
		this.type = type;
		this.fileFilter = fileFilter;
	}
	
	public DocumentBehavior getBehaviour() {
		return behaviour;
	}
	public void setBehaviour(DocumentBehavior behaviour) {
		this.behaviour = behaviour;
	}
	public int getPadding_height() {
		return paddingHeight;
	}
	public void setPadding_height(int paddingHeight) {
		this.paddingHeight = paddingHeight;
	}
	public int getPaddingWidth() {
		return paddingWidth;
	}
	public void setPaddingWidth(int paddingWidth) {
		this.paddingWidth = paddingWidth;
	}
	public void setType(DocumentType type) {
		this.type = type;
	}
	public DocumentType getType() {
		return type;
	}

	public int getPaddingHeight() {
		return paddingHeight;
	}

	public void setPaddingHeight(int paddingHeight) {
		this.paddingHeight = paddingHeight;
	}

	public String[] getFileFilter() {
		return fileFilter;
	}

	public void setFileFilter(String[] fileFilter) {
		this.fileFilter = fileFilter;
	}
	
}
