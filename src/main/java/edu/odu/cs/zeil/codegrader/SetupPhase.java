package edu.odu.cs.zeil.codegrader;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.Grader.AssignmentProperties;

/**
 * The Setup phase combines instructor-supplied and student-submitted code
 * in a staging area.
 * 
 * @author zeil
 *
 */
public class SetupPhase implements Phase {

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    	
	/**
	 * Assignment configuration info.
	 */
	final private AssignmentProperties properties;

	/**
	 * Configuration details for the Setup Phase.
	 * 
	 * @author zeil
	 *
	 */
	public static class SetupProperties extends ReflectiveProperties {
		/**
		 * Where are the student-submitted files obtained from?
		 */
		public PathProperty submissionDir = new PathProperty("Submission");
		
		/**
		 * Where are instructor-supplied filed obtained from?
		 */
		public PathProperty instructorDir = new PathProperty("Instructor");
		
		/**
		 * Where should the combined files be collected?
		 */
		public PathProperty stageDir = new PathProperty("Stage");
		
		/**
		 * What type of engine will be used in the Build and Test phases?
		 *    local: work on this machine.
		 *    docker: work in a docker container
		 *    vm: work in a virtual machine
		 */
		public String workEngine = "local";
		
		/**
		 * Where on the engine will the code be built and tested?
		 */
		public PathProperty workDir = new PathProperty("Work");
	}
	

	/**
	 * Create a setup phase.
	 * 
	 * @param properties the assignment configuration info
	 */
	public SetupPhase(final AssignmentProperties properties) {
		this.properties = properties;
	}

	@Override
	public boolean runPhase() {
		logger.info("Set Up");
		try {
			copyStudentFiles();
			copyInstructorFiles();
		} catch (IOException ex) {
			logger.error(ex.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Copy all files from the Instructor directory to
	 * the Stage directory.
	 * @throws IOException if files cannot be copied
	 */
	private void copyInstructorFiles() throws IOException {
		Path sourcePath = getInstructorDir();
		Path destPath = getStageDir();
		try {
			FileUtils.copyDirectory(sourcePath, destPath);
		} catch (IOException ex) {
			throw new IOException("Problem copying instructor files\n"
					+ "  from " + sourcePath.toString() + "\n"
					+ "  to " + destPath.toString() + "\n"
					+ ex.getMessage()
					);
		}
	}

	/**
	 * Copy all files from the Submission directory to
	 * the Stage directory.
	 * @throws IOException if files cannot be copied
	 */
	private void copyStudentFiles() throws IOException {
		Path sourcePath = getSubmissionDir();
		Path destPath = getStageDir();
		try {
			FileUtils.copyDirectory(sourcePath, destPath);
		} catch (IOException ex) {
			throw new IOException("Problem copying student files\n"
					+ "  from " + sourcePath.toString() + "\n"
					+ "  to " + destPath.toString() + "\n"
					+ ex.getMessage()
					);
		}
	}

	/**
	 * @return path to the student-submitted files
	 */
	public Path getSubmissionDir() {
		final Path anchor = properties.assignmentRoot.getPath(); 
		return anchor.resolve(properties.setup.submissionDir.getPath());
	}

	/**
	 * @return path to the instructor-supplied files
	 */
	public Path getInstructorDir() {
		final Path anchor = properties.assignmentRoot.getPath(); 
		return anchor.resolve(properties.setup.instructorDir.getPath());
	}

	/**
	 * @return path to the directory where all files will be assembled
	 */
	public Path getStageDir() {
		final Path anchor = properties.assignmentRoot.getPath(); 
		return anchor.resolve(properties.setup.stageDir.getPath());
	}

	/**
	 * @return type of engine to be used for building and testing
	 */
	public String getWorkEngine() {
		return properties.setup.workEngine;
	}

	/**
	 * @return path within engine where build and test will take place
	 */
	public Path getWorkDir() {
		final Path anchor = properties.assignmentRoot.getPath(); 
		return anchor.resolve(properties.setup.workDir.getPath());
	}
}
