package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSuite {

	private static final int MAX_SCORE = 100;
	private TestSuitePropertiesBase properties;
	private Assignment assignment;
	private Path testSuiteDirectory;
	private Set<String> testsToPerform;
	private Set<String> submissionsToRun;
	private Stage goldStage;
	private Stage submitterStage;

	/**
	 * Error logging.
	 */
	private static Logger logger = LoggerFactory.getLogger(
			MethodHandles.lookup().lookupClass());

	/**
	 * Create a new test suite.
	 * 
	 * @param asst the assignment that this test suite is intended to assess.
	 */
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
		goldStage = null;
		submitterStage = null;
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
	 * Limits the set of submissions that will actually be run by
	 * performTests.
	 * 
	 * @param submissionList list of submission names
	 */
	public void setSelectedSubmissions(List<String> submissionList) {
		submissionsToRun.clear();
		submissionsToRun.addAll(submissionList);
	}

	/**
	 * Perform all selected tests for all selected submissions.
	 * 
	 * The entire sequence is performed:
	 * 1) build gold version (if available)
	 * for each submission,
	 * 2) Copy the test suite into the reporting area.
	 * 3) set up submission code in staging area
	 * 4) build code in staging area
	 * for all selected test cases
	 * 5) if a gold version is available, run the test on it and collect the
	 * expected output and expected time limit.
	 * 6) run and evaluate the test on the submission , putting results in
	 * the recording area
	 * 7) Prepare summary reports of test results
	 */
	public void performTests() {
		if (assignment.getGoldDirectory() != null) {
			goldStage = new Stage(assignment, properties);
			goldStage.setupStage();
			tryToBuildGoldVersion();
		}

		Path submissionsDir = assignment.getSubmissionsDirectory();
		File[] submissions = submissionsDir.toFile().listFiles();
		if (submissions == null || submissions.length == 0) {
			throw new TestConfigurationError(
					"No submission directories in "
							+ submissionsDir.toString());
		}
		for (File submissionFile : submissions) {
			if (isAValidSubmission(submissionFile)) {
				String submissionName = submissionFile.getName();
				if (submissionsToRun.size() == 0
						|| submissionsToRun.contains(submissionName)) {
					Submission submission = new Submission(assignment,
							submissionName);
					processThisSubmission(submission);
				}
			}
		}
		if (submissions != null && submissions.length > 0) {
			if (goldStage != null) {
				goldStage.clear();
			}
			submitterStage.clear();
		}
	}

	/**
	 * 
	 * @param submissionFile possible directory containing a submission
	 * @return true if this is a directory and it contains a submission that
	 *         we want tested.
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
	 * 1) Copy the test suite into the reporting area.
	 * 2) set up submission code in staging area
	 * 3) build code in staging area
	 * 
	 * for all selected test cases
	 * run and evaluate the test case.
	 * 
	 * 4) Prepare summary reports of test results
	 * 
	 * @param submission the submission to process
	 */
	public void processThisSubmission(Submission submission) {
		submitterStage = new Stage(assignment, submission, properties);
		submitterStage.clear();
		Path recordAt = submission.getRecordingDir();
		recordAt.toFile().mkdirs();
		copyTestSuiteToRecordingArea(submission);
		submitterStage.setupStage();
		Stage.BuildResult buildResults = submitterStage.buildCode();
		int buildScore = (buildResults.getStatusCode() == 0) ? MAX_SCORE : 0;
		FileUtils.writeTextFile(recordAt.resolve("build.score"),
				Integer.toString(buildScore) + "\n");
		FileUtils.writeTextFile(recordAt.resolve("build.message"),
				buildResults.getMessage() + "\n");
		runTests(submission, buildResults.getStatusCode());
	}

	/**
	 * Delete the entire staging area after use.
	 * 
	 * @param stage the staging area
	 */
	void clearTheStage(Path stage) {
		try {
			FileUtils.deleteDirectory(stage);
		} catch (IOException ex) {
			logger.warn("Problem clearing the staging directory "
					+ stage.toString(), ex);
		}
	}

	/**
	 * Make a copy of the test suite in the student's recording area.
	 * 
	 * @param submission the submission being graded
	 */
	private void copyTestSuiteToRecordingArea(Submission submission) {
		Path studentGradingArea = submission.getTestSuiteDir();
		try {
			FileUtils.copyDirectory(assignment.getTestSuiteDirectory(),
					studentGradingArea, null, null);
		} catch (IOException ex) {
			logger.warn("Problem copying the suite to the recording area "
					+ studentGradingArea.toString(), ex);
		}
	}

	public static class BuildResult {
		public int statusCode;
		public String message;

		/**
		 * Create a build result.
		 * 
		 * @param code status code from the build process
		 * @param msg  output printed by the build process
		 */
		public BuildResult(int code, String msg) {
			statusCode = code;
			message = msg;
		}
	}

	/**
	 * If the instructor has provided a gold version of the program,
	 * build the code.
	 */
	private void tryToBuildGoldVersion() {
		Stage.BuildResult result = goldStage.buildCode();
		if (result.getStatusCode() != 0) {
			throw new TestConfigurationError(
					"Gold code does not build\n"
							+ result.getMessage());
		}
	}

	/**
	 * Runs all selected tests for a given submission. Assumes that code
	 * for gold version and submission have been built. Test results are
	 * recorded in the recording area.
	 * 
	 * @param submission  submission to be tested
	 * @param buildStatus 0 if build succeeded
	 */
	public void runTests(Submission submission, int buildStatus) {
		Path suiteDir = submission.getTestSuiteDir();
		File[] testCases = suiteDir.toFile().listFiles();
		if (testCases == null || testCases.length == 0) {
			throw new TestConfigurationError(
					"No test case directories in " + suiteDir.toString());
		}
		for (File testCase : testCases) {
			if (isAValidTestCase(testCase)) {
				String testName = testCase.getName();

				TestCaseProperties tcProperties = new TestCaseProperties(
						assignment, testName);
				TestCase tc = new TestCase(tcProperties);
				goldStage = new Stage(assignment, properties);
				if (assignment.getGoldDirectory() != null) {
					tc.performTest(submission, true,
							goldStage, 0);
				}
				submitterStage = new Stage(assignment, submission, properties);
				tc.performTest(submission, false,
						submitterStage, buildStatus);
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
	 * Adds a directory that is expected to hold Java source code.
	 * An assignment can hold multiple such source roots (e.g. the Maven &
	 * Gradle conventions place Java source code in src/main/java and
	 * src/test/java).
	 * 
	 * *.java files that are found out-of-place according to their
	 * package declarations will be eventually moved into the appropriate
	 * location within the file tree of the first such source directory added.
	 * 
	 * @param relativePath a Java source directory root.
	 */
	public void addJavaSrcDir(String relativePath) {
		properties.build.javaSrcDir.add(relativePath);
	}

}
