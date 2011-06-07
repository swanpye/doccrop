package common;
/**
 * Simple timer, with intention to be used for debugging
 * @author Tomas
 *
 */
public class SimpleTimer {

	private long start,stop, elapsed;
	
	/**
	 * Start the timer
	 */
	public void start() {
		start = System.currentTimeMillis();
		stop = start;
	}
	
	/**
	 * Stop the timer, and return elapsed time in milliseconds 
	 * @return Milliseconds elapsed since start
	 */
	public long stop() {
		stop = System.currentTimeMillis();
		elapsed = stop - start;
		return elapsed;
	}
	
	/**
	 * Print message to standard out together with elapsed time in milliseconds 
	 * @param message The message to print
	 */
	public void print(String message) {
		System.out.println(message + ": " + elapsed + " ms");
	}
}
