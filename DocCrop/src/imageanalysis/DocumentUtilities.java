package imageanalysis;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import common.Document;

/**
 * @author Tomas Toss 2 jun 2011
 */
public class DocumentUtilities {

	public static final int MEDIAN_POSITION = 0;
	public static final int LINEAR_POSITION = 1;
	private static int medianWindowSize = 3;

	public static Vector<Document> correctDocuments(Vector<Document> documents) {
		if (documents == null)
			return null;
		return correctDocuments(documents, MEDIAN_POSITION);
	}

	public static Vector<Document> correctDocuments(Vector<Document> documents,
			int positionMode) {
		if (documents == null)
			return null;
		if (documents.size() < medianWindowSize
				&& positionMode == MEDIAN_POSITION) {
			throw new IllegalArgumentException(
					"The median window size must be smaller or equal to the size of the document vector");
		}
		return correctDocuments(documents, MEDIAN_POSITION, 0.5);
	}

	public static Vector<Document> correctDocuments(Vector<Document> documents,
			int positionMode, double dimensionQuantile) {
		if (documents == null)
			return null;
		if (documents.size() < medianWindowSize
				&& positionMode == MEDIAN_POSITION) {
			throw new IllegalArgumentException(
					"The median window size must be smaller or equal to the size of the document vector");
		}
		if (dimensionQuantile < 0 || dimensionQuantile > 1)
			throw new IllegalArgumentException(
					"The quantile must be 0 <= quantile <= 1");

		int size = documents.size();
		Vector<Document> toReturn = new Vector<Document>();

		// Compute quantile dimension of the documents
		int[][] dim = new int[2][size];
		for (int i = 0; i < size; i++) {
			dim[0][i] = documents.get(i).getWidth();
			dim[1][i] = documents.get(i).getHeight();
		}
		Arrays.sort(dim[0]);
		Arrays.sort(dim[1]);
		Point correctedDim = new Point(
				dim[0][(int) (size * dimensionQuantile)],
				dim[1][(int) (size * dimensionQuantile)]);

		switch (positionMode) {
		case MEDIAN_POSITION:
			for (int i = 0; i < size; i++) {
				toReturn.add(new Document(correctPosition(documents, i),
						correctedDim.x, correctedDim.y));
			}
			break;
		case LINEAR_POSITION:
			break;
		}
		return toReturn;
	}

	private static Point correctPosition(Vector<Document> documents,
			int correctIndex) {
		int size = documents.size();
		Point newPosition = new Point();
		if (correctIndex < medianWindowSize / 2) {
			newPosition = medianPosition(documents.subList(0,
					medianWindowSize - 1));
		} else if (correctIndex > size - medianWindowSize / 2) {
			newPosition = medianPosition(documents.subList(size
					- medianWindowSize / 2 - 1, size - 1));
		} else {
			newPosition = medianPosition(documents
					.subList(correctIndex - medianWindowSize / 2, correctIndex
							+ medianWindowSize / 2));
		}
		return newPosition;
	}

	private final static Point medianPosition(List<Document> documentWindow) {
		int size = documentWindow.size();
		int[][] pos = new int[2][size];
		for (int i = 0; i < size; i++) {
			pos[0][i] = documentWindow.get(i).getX();
			pos[1][i] = documentWindow.get(i).getY();
		}
		Arrays.sort(pos[0]);
		Arrays.sort(pos[1]);

		return new Point(pos[0][size / 2], pos[1][size / 2]);
	}

	public static void main(String[] args) {
		Vector<Document> docs = new Vector<Document>();
		docs.add(new Document(new Point(50, 20), 0, 10));
		docs.add(new Document(new Point(40, 40), 10, 40));
		docs.add(new Document(new Point(75, 60), 100, 30));
		docs.add(new Document(new Point(60, 30), 20, 199));
		docs.add(new Document(new Point(100, 90), 50, 40));

		System.out.println(correctDocuments(docs));
	}
}
