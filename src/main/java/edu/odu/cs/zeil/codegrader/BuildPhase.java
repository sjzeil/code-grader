package edu.odu.cs.zeil.codegrader;

import java.util.logging.Logger;



/**
 * The Build phase compiles or syntax checks the student's code.
 *   
 * @author zeil
 */
public class BuildPhase implements Phase {
	
	private final static Logger log = Logger.getLogger(BuildPhase.class.getName());


	/**
	 * Configuration details for this Phase.
	 * 
	 * @author zeil
	 *
	 */
	public static class BuildProperties extends ReflectiveProperties {
		/**
		 * What is the build command?
		 */
		public String buildCommand = "";  // default - derive from examining source code
		
		/**
		 * What files should a successful build produce?
		 */
		public String[] buildTargets = {};  // default - derive from examining source code
		
	}

	@Override
	public boolean runPhase() {
		try {
			String buildCommand = getBuildCommand();
			String messages = doTheBuild(buildCommand);
			scoreTheBuild();
			return true;
		} catch (Exception ex) {
			log.severe ("Unable to complete build phase:\n" + ex.getMessage());
			return false;
		}
	}

	private void scoreTheBuild() {
		// TODO Auto-generated method stub
		
	}

	private String doTheBuild(String buildCommand) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBuildCommand() {
		return "make -k";
	}

	public String getCppOptions() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getCppCommand() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getJavaCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJavaOptions() {
		// TODO Auto-generated method stub
		return null;
	}


}
