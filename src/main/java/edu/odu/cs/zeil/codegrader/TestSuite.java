package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	 * 2) set up submission code in staging area
	 * 3) build code in staging area
	 * for all selected test cases
	 * 4) if a gold version is available, run the test on it and collect the
	 *    expected output and expected time limit.
	 * 5) run and evaluate the test on the submission , putting results in
	 *    the recording area
	 * 
	 */
	public void performTests() {
		Path goldDir = assignment.getGoldDirectory();
		if (goldDir != null) {
			if (goldDir.toFile().exists() && goldDir.toFile().isDirectory()) {
				buildGoldCode();
			}
		}
		Path submissionsDir = assignment.getSubmissionsDirectory();
		File[] submissions = submissionsDir.toFile().listFiles();
		if (submissions == null || submissions.length == 0) {
			throw new TestConfigurationError(
					"No submission directories in " + submissionsDir.toString());
		}
		Path stage = assignment.getStagingDirectory();
		try {
			FileUtils.deleteDirectory(stage);
		} catch (IOException ex) {
			logger.warn("Problem clearing the staging directory "
					+ stage.toString(), ex);
		}
		for (File submissionFile : submissions) {
			if (!submissionFile.isDirectory()) {
				continue;
			}
			String submissionName = submissionFile.getName();
			if (submissionsToRun.size() == 0
					|| submissionsToRun.contains(submissionName)) {
				Submission submission = new Submission(assignment, submissionName);
				stage.toFile().mkdirs();
				setup(submissionFile, stage);
				buildStagedCode(stage);
				runTests(submission);
				try {
					FileUtils.deleteDirectory(stage);
				} catch (IOException ex) {
					logger.warn("Problem clearing the staging directory "
							+ stage.toString(), ex);
				}
			}
		}

	}

	private void buildStagedCode(Path stage) {
	}

	private void setup(File submission, Path stage) {
	}

	private void buildGoldCode() {
	}

	/**
	 * Runs all selected tests for a given submission. Assumes that code
	 * for gold version and submission have been built. Test results are
	 * recorded in the recording area.
	 * 
	 * @param submission submission to be tested
	 */
	public void runTests(Submission submission) {
		String submissionName = submission.getSubmittedBy();
		Path suiteDir = assignment.getTestSuiteDirectory();
		File[] testCases = suiteDir.toFile().listFiles();
		if (testCases == null || testCases.length == 0) {
			throw new TestConfigurationError(
					"No test case directories in " + suiteDir.toString());
		}
		for (File testCase : testCases) {
			if (!testCase.isDirectory()) {
				continue;
			}
			String testName = testCase.getName();
			if (testsToPerform.size() == 0
					|| testsToPerform.contains(testName)) {
				Path recordingBase = assignment.getRecordingDirectory();
				Path recordingDir = recordingBase.resolve(submissionName)
						.resolve(testName);
				if (recordingDir.toFile().exists()) {
					try {
						FileUtils.deleteDirectory(recordingDir);
					} catch (IOException e) {
						logger.warn("Problem clearing "
								+ recordingDir.toString(), e);
					}
				}
				recordingDir.toFile().mkdirs();
				Path tcDir = suiteDir.resolve(testName);
				try {
					FileUtils.copyDirectory(tcDir, recordingDir);
				} catch (IOException e) {
					throw new TestConfigurationError("Unable to "
							+ "record test info from " + tcDir.toString()
							+ " to " + recordingDir.toString() + "\n"
							+ e.getMessage());
				}
				TestCaseProperties tcProperties = new TestCaseProperties(
						assignment, testName);
				TestCase tc = new TestCase(tcProperties);
				if (assignment.getGoldDirectory() != null) {
					runGoldVersion(tcProperties);
				}
				tc.performTest(submission, false);
			}
		}
	}

	private void runGoldVersion(TestCaseProperties tcProperties) {
	}

}
