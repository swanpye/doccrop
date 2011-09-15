package common;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import mvc.model.IDocumentPrinter;
import mvc.model.ImageBatch;
import mvc.model.ImageBatch.DocumentBehavior;
import mvc.model.ImageBatches;

import common.Document.DocumentType;

public abstract class FileUtilities {
	/**
	 * @author Tomas Toss 2 sep 2011
	 * @throws IOException
	 *             , FileNotFoundException
	 */

	public static int printToFile(ImageBatches batch, IDocumentPrinter printer)
			throws IOException, FileNotFoundException {
		Configuration.loadApplicationProperties();
		String path;

		ImageBatch[] batches = batch.getBatches();
		for (int i = 0; i < batches.length; i++) {
			if (Configuration.getProperty(
					"settings.program.cropping.isDefinedPath").equals("TRUE")) {
				path = Configuration
						.getProperty("settings.program.cropping.definedPath") + System.getProperty("file.separator") + "DocCrop_out.txt";
			} else {
				path = batches[i].getBatchPath()  + File.separator + "DocCrop_out.txt";
			}
			Writer writer = new BufferedWriter(new FileWriter(path));
			Document[] docs = batches[i].getDocuments();
			for (int j = 0; j < docs.length; j++) {
				System.out.println(printer.printDocument(docs[j]));
				writer.write(printer.printDocument(docs[j]));
			}
			writer.close();
		}

		return 0;
	}

	public static void main(String[] args) {


		String[] fileFilter = {"properties"}; 
		ImageBatches batches = new ImageBatches();
		ImageBatch[] batch = {new ImageBatch(batches,new File("./src/doccrop.properties"),new ImageBatchSettings(0, 0, DocumentBehavior.SIMPLE, DocumentType.SINGLE_PAGE, fileFilter))};
		Document[] docs = {new Document(new Point(100,100), 100, 200, 0, "./test"), new Document(new Point(100,100), 100, 200, 0, "./test2")};
		batch[0].setDocuments(docs);
		try {
			printToFile(batches, new SimplePrinter());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
