package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
		Path stage = assignment.getStagingDirectory();
		buildGoldVersionIfAvailable();
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
				Submission submission = new Submission(assignment,
						submissionName);
				processThisSubmission(submission);
			}
		}
		clearTheStage(stage);
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
		Path stage = assignment.getSubmitterStage();
		if (stage.toFile().exists()) {
			try {
				FileUtils.deleteDirectory(stage);
			} catch (IOException e) {
				logger.warn("Problem clearing the staging area "
						+ stage.toString(), e);
			}
		}
		Path recordAt = assignment.getRecordingDirectory()
				.resolve(submission.getSubmittedBy());
		recordAt.toFile().mkdirs();
		copyTestSuiteToRecordingArea(submission);
		File submissionFile = assignment.getSubmissionsDirectory()
				.resolve(submission.getSubmittedBy()).toFile();
		setupStage(submissionFile, stage);
		buildStagedCode(stage, submission);
		runTests(submission);
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
		Path studentRecordingArea = getStudentRecordingDir(submission);
		try {
			FileUtils.copyDirectory(assignment.getTestSuiteDirectory(),
					studentRecordingArea, null, null);
		} catch (IOException ex) {
			logger.warn("Problem copying the suite to the recordingArea "
					+ studentRecordingArea.toString(), ex);
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
	void buildGoldVersionIfAvailable() {
		Path goldDir = assignment.getGoldDirectory();
		if (goldDir != null) {
			if (goldDir.toFile().exists() && goldDir.toFile().isDirectory()) {
				Path goldStage = assignment.getGoldStage();
				if (!goldStage.toFile().exists()) {
					try {
						FileUtils.copyDirectory(goldDir, goldStage, null, null);
					} catch (IOException e) {
						throw new TestConfigurationError(
							"Cannot create staging directory for gold version\n"
							+ e.getMessage());
					}
					BuildResult result = buildCode(goldStage);
					if (result.statusCode != 0) {
						throw new TestConfigurationError(
								"Gold code does not build\n"
										+ result.message);
					}
				}
			}
		}
	}

	/**
	 * Build the submitted code (in the staging area).
	 * 
	 * @param stage      staging area containing submitted code &
	 *                   instructor-provided code
	 * @param submission submission being graded
	 */
	private void buildStagedCode(Path stage, Submission submission) {
		BuildResult result = buildCode(stage);
		String buildScore = (result.statusCode == 0) ? "100" : "0";
		Path recordAt = assignment.getRecordingDirectory()
				.resolve(submission.getSubmittedBy());
		FileUtils.writeTextFile(
				recordAt.resolve("build.score"),
				buildScore);
		FileUtils.writeTextFile(
				recordAt.resolve("build.message"),
				result.message);
	}

	private BuildResult buildCode(Path stage) {
		String buildCommand = getBuildCommand(stage);
		if (buildCommand == null || buildCommand.equals("")) {
			throw new TestConfigurationError(
					"Could not deduce build command in "
							+ stage.toString());
		}
		buildCommand = assignment.parameterSubstitution(buildCommand, "");

		ExternalProcess process = new ExternalProcess(
				stage,
				buildCommand,
				properties.build.timeLimit,
				null,
				"build process (" + buildCommand + ")");
		process.execute();
		String buildInfo = process.getOutput() + "\n\n" + process.getErr();
		if (process.getOnTime()) {
			if (process.getStatusCode() == 0) {
				return new BuildResult(0, buildInfo);
			} else {
				return new BuildResult(process.getStatusCode(),
					"Build failed with status code " + process.getStatusCode()
					+ ".\n" + buildInfo);
			}
		} else {
			return new BuildResult(-1,
					"Build exceeded " + properties.build.timeLimit
							+ " seconds.\n" + buildInfo);
		}

	}

	/**
	 * Determine the command used to build the code.
	 * Can be set as a suite property or will attempt to infer the command
	 * from the build directory contents.
	 * 
	 * @param buildDir the directory containing the code to be built.
	 * @return a build command
	 * @throws TestConfigurationError if no build command can be determined.
	 */
	private String getBuildCommand(Path buildDir) {
		String command = properties.build.command;
		if (command == null || command.equals("")) {
			// Try to infer the command from the contents of the
			// build directory.
			if (buildDir.resolve("makefile").toFile().exists()) {
				command = "make";
			} else if (buildDir.resolve("Makefile").toFile().exists()) {
				command = "make";
			} else if (buildDir.resolve("build.gradle").toFile().exists()) {
				if (buildDir.resolve("gradlew").toFile().exists()) {
					command = "." + File.separator + "gradlew build";
				} else {
					command = "gradle build";
				}
			} else if (buildDir.resolve("pom.xml").toFile().exists()) {
				command = "mvn compile";
			} else if (buildDir.resolve("pom.xml").toFile().exists()) {
				command = "ant";
			} else {
				List<File> javaDirs = FileUtils.findDirectoriesContaining(
						buildDir, ".java");
				if (javaDirs.size() > 0) {
					StringBuilder commandStr = new StringBuilder();
					commandStr.append("java ");
					if (buildDir.resolve("lib").toFile().isDirectory()) {
						List<File> jars = FileUtils.findAllFiles(
								buildDir.resolve("lib"), ".jar");
						if (jars.size() > 0) {
							commandStr.append("-cp ." + File.pathSeparator
									+ "'lib/*.java' ");
						}
					}
					for (File srcDir : javaDirs) {
						commandStr.append(srcDir.toString()
								+ File.separator + "*.java ");
					}
					command = commandStr.toString();
				} else {
					StringBuilder commandStr = new StringBuilder();
					commandStr.append("g++ -g -std=c++17 ");
					List<File> cppDirs = FileUtils.findDirectoriesContaining(
							buildDir, ".cpp");
					for (File srcDir : cppDirs) {
						commandStr.append(srcDir.toString()
								+ File.separator + "*.cpp ");
					}
					command = commandStr.toString();
				}
			}
		}
		if (command == null || command.equals("")) {
			throw new TestConfigurationError(
					"Could not infer a build command for "
							+ buildDir.toString());
		}
		return command;
	}

	private void setupStage(File submission, Path stage) {
		stage.toFile().mkdirs();
		List<String> requiredStudentFiles = listRequiredStudentFiles();
		try {
			FileUtils.copyDirectory(
				assignment.getInstructorCodeDirectory(), 
				stage, 
				null, 
				requiredStudentFiles);
		} catch (IOException e) {
			throw new TestConfigurationError(
				"Could not copy instructor files from "
				+ assignment.getInstructorCodeDirectory().toString()
				+ " into " + stage.toString() + "\n"
				+ e.getMessage()
				);
		}
		try {
			FileUtils.copyDirectory(
				submission.toPath(), 
				stage, 
				properties.build.studentFiles, 
				null,
				StandardCopyOption.REPLACE_EXISTING
				);
		} catch (IOException e) {
			throw new TestConfigurationError(
				"Could not copy student files from "
				+ submission.toString()
				+ " into " + stage.toString() + "\n"
				+ e.getMessage()
				);
		}
		arrangeJavaFiles(stage);
	}

	/**
	 * Look for Java files that are out of position according to their
	 * package name and move them to the proper location.
	 * @param stage the staging area.
	 */
	private void arrangeJavaFiles(Path stage) {
	}

	private List<String> listRequiredStudentFiles() {
		List<String> results = new ArrayList<>();
		for (String pattern: properties.build.studentFiles) {
			if (!pattern.contains("*")) {
				results.add(pattern);
			}
		}
		return results;
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
				TestCase tc = new TestCase(tcProperties);
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
	 * by modifying the contents of the test suite. It also means that a
	 * self-contained record is available of each student's test results.
	 * 
	 * @param submission
	 * @return a new Assignment with the test suite set to the student's
	 *         recording area.
	 */
	private Assignment getRevisedAssignmentSettings(Submission submission) {
		Path studentRecordingArea = getStudentRecordingDir(submission);
		Assignment testAssignment = assignment.clone();
		testAssignment.setTestSuiteDirectory(studentRecordingArea);
		return testAssignment;
	}

	private Path getStudentRecordingDir(Submission submission) {
		Path studentRecordingArea = assignment.getRecordingDirectory()
				.resolve(submission.getSubmittedBy())
				.resolve("Grading");
		return studentRecordingArea;
	}

	private void runGoldVersion(TestCaseProperties tcProperties) {
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
		// TODO
	}

}
