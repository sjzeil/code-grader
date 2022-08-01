package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSuite {

	private TestSuitePropertiesBase properties;
	private Assignment assignment;
	private Path testSuiteDirectory;
	private Set<String> testsToPerform;
	private Set<String> submissionsToRun;

	/**
	 * Error logging.
	 */
	private static Logger logger = LoggerFactory.getLogger(
			MethodHandles.lookup().lookupClass());

	public TestSuite(Assignment asst) {
		assignment = asst;
		testsToPerform = new HashSet<String>();
		submissionsToRun = new HashSet<String>();
		testSuiteDirectory = asst.getTestSuiteDirectory();
		properties = new TestSuitePropertiesBase();
		Optional<File> propsFile = FileUtils.findFile(
				testSuiteDirectory,
				".yaml");
		if (propsFile.isPresent()) {
			properties = TestSuitePropertiesBase.loadYAML(propsFile.get());
		}
	}

	/**
	 * Set the default launch command for all test cases in this suite.
	 * 
	 * @param launcher launcher command
	 */
	public void setLaunch(String launcher) {
		properties.test.launch = Optional.of(launcher);
	}

	/**
	 * Limits the set of tests that will actually be run by
	 * performTests.
	 * 
	 * @param testList list of test case names
	 */
	public void setSelectedTests(List<String> testList) {
		testsToPerform.clear();
		testsToPerform.addAll(testList);
	}

	/**
	 * Perform all selected tests for all selected submissions.
	 * 
	 * The entire sequence is performed:
	 * 1) build gold version (if available)
	 * for each submission,
	 *   2) Copy the test suite into the reporting area.
	 *   3) set up submission code in staging area
	 *   4) build code in staging area
	 *   for all selected test cases
	 *     5) if a gold version is available, run the test on it and collect the
	 *       expected output and expected time limit.
	 *     6) run and evaluate the test on the submission , putting results in
	 *       the recording area
	 *   7) Prepare summary reports of test results
	 */
	public void performTests() {
		buildGoldVersionIfAvailable();
		Path submissionsDir = assignment.getSubmissionsDirectory();
		File[] submissions = submissionsDir.toFile().listFiles();
		if (submissions == null || submissions.length == 0) {
			throw new TestConfigurationError(
					"No submission directories in " + submissionsDir.toString());
		}
		Path stage = assignment.getStagingDirectory();
		clearTheStage(stage);
		for (File submissionFile : submissions) {
			if (isAValidSubmission(submissionFile)) {
				String submissionName = submissionFile.getName();
				Submission submission = new Submission(assignment, 
					submissionName);
				processThisSubmission(stage, submission);
			}
		}

	}

	/**
	 * 
	 * @param submissionFile possible directory containing a submission
	 * @return true if this is a directory and it contains a submission that
	 * 		we want tested.
	 */
	private boolean isAValidSubmission(File submissionFile) {
		if (!submissionFile.isDirectory()) {
			return false;
		}
		String submissionName = submissionFile.getName();
		return (submissionsToRun.size() == 0
				|| submissionsToRun.contains(submissionName));

	}

	/**
	 * Process a submission.
	 *   1) Copy the test suite into the reporting area.
	 *   2) set up submission code in staging area
	 *   3) build code in staging area
	 * 
	 *   for all selected test cases
	 *     run and evaluate the test case.
	 * 
	 *   4) Prepare summary reports of test results
	 * 
	 * @param stage the staging area
	 * @param submission the submission to process
	 */
	private void processThisSubmission(Path stage, Submission submission) {
		copyTestSuiteToRecordingArea(submission);
		File submissionFile = assignment.getSubmissionsDirectory()
			.resolve(submission.getSubmittedBy()).toFile();
		setupStage(submissionFile, stage);
		buildStagedCode(stage);
		runTests(submission);
		clearTheStage(stage);
	}

	/**
	 * Delete the entire staging area after use.
	 * @param stage the staging area
	 */
	private void clearTheStage(Path stage) {
		try {
			FileUtils.deleteDirectory(stage);
		} catch (IOException ex) {
			logger.warn("Problem clearing the staging directory "
					+ stage.toString(), ex);
		}
	}

	/**
	 * Make a copy of the test suite in the student's recording area.
	 * @param submission the submission being graded
	 */
	private void copyTestSuiteToRecordingArea(Submission submission) {
		Path studentRecordingArea = getStudentRecordingDir(submission);
		try {
		FileUtils.copyDirectory(assignment.getTestSuiteDirectory(),
			studentRecordingArea);
		} catch (IOException ex) {
			logger.warn("Problem copying the suite to the recordingArea "
					+ studentRecordingArea.toString(), ex);
		}
	}

	static public class BuildResult {
		public int statusCode;
		public String message;

		public BuildResult(int code, String msg) {
			statusCode = code;
			message = msg;
		}
	}

	/**
	 * If the instructor has provided a gold version of the program,
	 * build the code.
	 */
	private void buildGoldVersionIfAvailable() {
		Path goldDir = assignment.getGoldDirectory();
		if (goldDir != null) {
			if (goldDir.toFile().exists() && goldDir.toFile().isDirectory()) {
				 BuildResult result = buildCode(goldDir);
				 if (result.statusCode != 0) {
					throw new TestConfigurationError(
						"Gold code does not build\n"
						+ result.message);
				 }
			}
		}
	}

	/**
	 * Build the submitted code (in the staging area).
	 * @param stage staging area containing submitted code & 
	 * 		instructor-provided code
	 */
	private void buildStagedCode(Path stage) {
		BuildResult result = buildCode(stage);
		String buildScore = (result.statusCode == 0) ? "100" : "0";
		FileUtils.writeTextFile(
			assignment.getRecordingDirectory().resolve("build.score"), 
			buildScore);
		FileUtils.writeTextFile(
			assignment.getRecordingDirectory().resolve("build.message"), 
			result.message);			
	}

	private BuildResult buildCode(Path stage) {
		String buildCommand = getBuildCommand(stage);
		if (buildCommand == null || buildCommand.equals("")) {
			throw new TestConfigurationError("Could not deduce build command in "
				+ stage.toString());
		}
		buildCommand = assignment.parameterSubstitution(buildCommand, "");

		ExternalProcess process = new ExternalProcess(
			stage, 
			buildCommand, 
			5*60,
			null,
			"build process (" + buildCommand + ")");
		process.execute();
		String buildInfo = process.getOutput() + "\n\n" + process.getErr();
		if (process.getOnTime()) {
			if (process.getStatusCode() == 0) {
				return new BuildResult(100, buildInfo);
			} else {
				return new BuildResult(0, 
				"Build failed with status code " + process.getStatusCode()
					+ ".\n" + buildInfo);
			}
		} else {
			return new BuildResult(0, 
				"Build exceeded 5 minutes.\n" + buildInfo);
		}

	}

	private String getBuildCommand(Path buildDir) {
		// TODO
	}

	private void setupStage(File submission, Path stage) {
		stage.toFile().mkdirs();
	}


	/**
	 * Runs all selected tests for a given submission. Assumes that code
	 * for gold version and submission have been built. Test results are
	 * recorded in the recording area.
	 * 
	 * @param submission submission to be tested
	 */
	public void runTests(Submission submission) {
		Assignment testAssignment = getRevisedAssignmentSettings(submission);

		Path suiteDir = testAssignment.getTestSuiteDirectory();
		File[] testCases = suiteDir.toFile().listFiles();
		if (testCases == null || testCases.length == 0) {
			throw new TestConfigurationError(
					"No test case directories in " + suiteDir.toString());
		}
		for (File testCase : testCases) {
			if (isAValidTestCase(testCase)) {
				String testName = testCase.getName();
				
				TestCaseProperties tcProperties = new TestCaseProperties(
						testAssignment, testName);
				ExternalProcess tc = new ExternalProcess(tcProperties);
				if (testAssignment.getGoldDirectory() != null) {
					runGoldVersion(tcProperties);
				}
				tc.performTest(submission, false);
			}
		}
	}

	private boolean isAValidTestCase(File testCase) {
		if (!testCase.isDirectory()) {
			return false;
		}
		String testName = testCase.getName();
		return (testsToPerform.size() == 0
				|| testsToPerform.contains(testName));
	}

	/**
	 * The original test suite will have been copied into the student's
	 * recording area. We use that copy as the test suite when running
	 * both the gold program and the student's submission.
	 * 
	 * This helps prevent student submissions from interfering with one another
	 * by modifying the contents of the test suite.  It also means that a
	 * self-contained record is available of each student's test results.
	 * 
	 * @param submission
	 * @return a new Assignment with the test suite set to the student's
	 * 		     recording area.
	 */
	private Assignment getRevisedAssignmentSettings(Submission submission) {
		Path studentRecordingArea = getStudentRecordingDir(submission);
		Assignment testAssignment = assignment.clone();
		testAssignment.setTestSuiteDirectory(studentRecordingArea);
		return testAssignment;
	}

	private Path getStudentRecordingDir(Submission submission) {
		Path studentRecordingArea = getStudentRecordingDir(submission);
		return studentRecordingArea;
	}

	private void runGoldVersion(TestCaseProperties tcProperties) {
	}

}
