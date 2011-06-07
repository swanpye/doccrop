package imageanalysis;

public interface AdjustableDetector {
	int LOW_EDGE_SENSITIVITY = 0;
	int MEDIUM_EDGE_SENSITIVITY = 1;
	int HIGH_EDGE_SENSITIVITY = 2;

	public void setEdgeSensitivity(int edgeSensitivity);

	public int getEdgeSensitivity();

}
