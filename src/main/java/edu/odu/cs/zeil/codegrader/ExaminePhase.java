package edu.odu.cs.zeil.codegrader;

public class ExaminePhase implements Phase {

	/**
	 * Configuration details for this Phase.
	 * 
	 * @author zeil
	 *
	 */
	public static class ExamineProperties extends ReflectiveProperties {
		/**
		 * Where are the student-submitted files obtained from?
		 */
		public PathProperty submissionDir = new PathProperty("Submission");
	}
	
	
	@Override
	public boolean runPhase() {
		// TODO Auto-generated method stub
		return false;
	}

	public ReflectiveProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
