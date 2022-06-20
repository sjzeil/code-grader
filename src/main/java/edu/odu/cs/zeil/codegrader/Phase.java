package edu.odu.cs.zeil.codegrader;

/**
 * A major activity of the autograder.
 * @author zeil
 *
 */
public interface Phase {
	/**
	 * Run the phase.
	 * 
	 * @param settings assignment settings;
	 * @return success  
	 */
	 boolean runPhase();
	

}
