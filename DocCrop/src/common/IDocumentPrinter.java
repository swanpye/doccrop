package common;


/**
 * Interface intended to be used for printing <code>Document</code>s to file. 
 * @author Tomas
 */
public interface IDocumentPrinter {

	/**
	 * The method returns a formated string giving a suitable representation of the document 
	 * @param doc The document to be printed
	 * @return The <code>String</code> representation of the document
	 */
	public String printDocument(Document doc);
}
