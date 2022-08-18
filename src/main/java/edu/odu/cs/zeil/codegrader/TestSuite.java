package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.sheetManip.Spreadsheet;

public class TestSuite {

	private static final int MAX_SCORE = 100;
	private TestSuitePropertiesBase properties;
	private Assignment assignment;
	private Path testSuiteDirectory;
	private Set<String> testsToPerform;
	private Set<String> submissionsToRun;
	private Stage goldStage;
	private Stage submitterStage;

	private int buildScore;
	private String buildMessage;

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
			System.out.println("Building gold code.");
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
					System.out.println("Testing submission from "
							+ submissionName);
					Submission submission = new Submission(assignment,
							submissionName);
					processThisSubmission(submission);
				}
			}
		}
		if (submissionsToRun.size() == 0) {
			try {
				FileUtils.deleteDirectory(assignment.getStagingDirectory());
			} catch (IOException e) {
				logger.warn("Unable to clear stages "
						+ assignment.getStagingDirectory()
						+ " at end of processing: \n", e);
			}
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
		if (submitterStage.getStageDir().toFile().exists()) {
			submitterStage.clear();
		}
		Path recordAt = submission.getRecordingDir();
		recordAt.toFile().mkdirs();
		copyTestSuiteToRecordingArea(submission);
		submitterStage.setupStage();
		Stage.BuildResult buildResults = submitterStage.buildCode();
		buildScore = (buildResults.getStatusCode() == 0) ? MAX_SCORE : 0;
		buildMessage = buildResults.getMessage();
		System.out.println("  Building submitted code: " + buildScore + "%.");
		FileUtils.writeTextFile(recordAt.resolve("build.score"),
				Integer.toString(buildScore) + "\n");
		FileUtils.writeTextFile(recordAt.resolve("build.message"),
				buildResults.getMessage() + "\n");
		runTests(submission, buildResults.getStatusCode());
		generateReports(submission);
	}

	private void generateReports(Submission submission) {
		Path gradeReport = submission.getRecordingDir()
				.resolve(submission.getSubmittedBy() + ".xlsx");
		if (!gradeReport.toFile().exists()) {
			prepareGradingTemplate(gradeReport);
		}
		// Write out general information about the assignment and submission.
		Path testInfoFile = submission.getRecordingDir()
				.resolve("testInfo.csv");
		StringBuilder testInfo = new StringBuilder();
		testInfo.append("assignment name,\"" + getAssignmentName() + "\"\n");
		testInfo.append("submitted by,\""
				+ submission.getSubmittedBy() + "\"\n");
		testInfo.append("built successfully?,\"" + buildScore + "\"\n");
		testInfo.append("build weight,\"" + properties.build.weight + "\"\n");
		testInfo.append("build message,\"" + csvEncode(buildMessage) + "\"\n");
		testInfo.append("due date,\"" + properties.dueDate + "\"\n");
		testInfo.append("submission date,\"" + submission.getSubmissionDate()
				+ "\"\n");
		FileUtils.writeTextFile(testInfoFile, testInfo.toString());

		// Write out the tests summary.
		Path testsSummaryFile = submission.getRecordingDir()
				.resolve("testsSummary.csv");
		StringBuilder testsSummary = new StringBuilder();
		testsSummary.append("Test,Score,Weight,Msgs\n");

		File[] testCases = submission.getTestSuiteDir().toFile().listFiles();
		if (testCases != null) {
			for (File testCase : testCases) {
				if (testCase.isDirectory()) {
					TestCaseProperties tcProps = new TestCaseProperties(
							assignment, testCase.getName());
					TestCase tc = new TestCase(tcProps);
					String testName = tc.getProperties().getName();
					testsSummary.append("\"" + testName + "\",");
					testsSummary.append(""
							+ submission.getScore(testName) + ",");
					testsSummary.append("" + tc.getProperties().getWeight()
							+ ",");
					testsSummary.append("\""
							+ csvEncode(submission.getMessage(testName)) + "\"\n");
				}
			}
		}
		FileUtils.writeTextFile(testsSummaryFile, testsSummary.toString());

		// Merge the .csv files into the grade template.
		try {
			Spreadsheet ss = new Spreadsheet(gradeReport.toFile());
			ss.loadCSV(testInfoFile.toFile(), "info");
			ss.loadCSV(testsSummaryFile.toFile(), "tests");
			String htmlContent = ss.toHTML(getAssignmentName(), true,
					"<b>", "</b>", "<i>", "</i>");
			FileUtils.writeTextFile(
					submission.getRecordingDir()
							.resolve(submission.getSubmittedBy() + ".html"),
					htmlContent);
			List<String[]> rows = ss.evaluateSheet("results", true);
			int studentTotalScore = -1;
			for (String[] row : rows) {
				// Hunt for a row whose first non-empty cell is "Total:"
				// or "Total".
				if (row != null) {
					for (int col = 0; col < row.length; ++col) {
						String v = row[col];
						if (v != null) {
							v = v.toLowerCase();
							if (v.equals("total") || v.equals("total:")) {
								// Next non-empty cell is the score
								for (int col2 = col + 1; col2 < row.length;
										++col2) {
									String v2 = row[col2];
									if (v2 != null && !v2.equals("")) {
										try {
											double d
												= Double.parseDouble(v2.trim());
											studentTotalScore = (int)(d + 0.5);
										} catch (NumberFormatException e) {
											studentTotalScore = -1;
										}
										break;
									}
								}
							} else {
								break;
							}
							
						}
						if (studentTotalScore >= 0) {
							break;
						}
					}
					
				}
			}
			FileUtils.writeTextFile(
					submission.getRecordingDir()
							.resolve(submission.getSubmittedBy() + ".total"),
					"" + studentTotalScore + "\n");
			ss.close();
		} catch (EncryptedDocumentException | InvalidFormatException
				| IOException e) {
			throw new TestConfigurationError(
					"Unable to update grades in " + gradeReport.toString()
							+ "\n" + e.getMessage());
		}
	}

	private String csvEncode(String msg) {
		return msg.replace("\"", "'");
	}

	/**
	 * Get a name for the assignment. If not given in the .yaml file, a default
	 * value is taken from the directory name containing the test suite.
	 * 
	 * @return an assignment name
	 */
	public String getAssignmentName() {
		if (!properties.assignment.equals("")) {
			return properties.assignment;
		} else {
			Path suite = assignment.getTestSuiteDirectory();
			Path parent = suite.getParent();
			return parent.toFile().getName();
		}
	}

	private static final int BUFFER_SIZE = 4096; // 4KB

	private void prepareGradingTemplate(Path gradeReport) {
		Path gradeTemplate = null;
		if (!properties.reportTemplate.equals("")) {
			try {
				gradeTemplate = Paths.get(properties.reportTemplate);
				if (!gradeTemplate.endsWith(".xlsx")
						|| !gradeTemplate.toFile().exists()) {
					gradeTemplate = null;
				}
			} catch (InvalidPathException ex) {
				gradeTemplate = null;
			}
			if (gradeTemplate == null) {
				logger.error("Invalid path to grade template spreadsheet "
						+ properties.reportTemplate);
			}
		}
		String description = "";
		try {
			InputStream template = null;
			if (gradeTemplate == null) {
				template = TestSuite.class.getResourceAsStream(
						"/edu/odu/cs/zeil/codegrader/gradeTemplate.xlsx");
				description = "default grading spreadsheet";
			} else {
				template = new FileInputStream(gradeTemplate.toFile());
				description = gradeTemplate.toString();
			}
			if (template == null) {
				throw new TestConfigurationError(
						"Could not find default grading template.");
			}
			OutputStream outputStream = new FileOutputStream(
				gradeReport.toFile());
			byte[] buffer = new byte[BUFFER_SIZE];
			int totalBytesRead = 0;
			int bytesRead = -1;

			while ((bytesRead = template.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
				totalBytesRead += bytesRead;
			}
			outputStream.close();
			template.close();
			if (totalBytesRead == 0) {
				throw new TestConfigurationError(
						"Could not find default grading template.");
			}
		} catch (IOException ex) {
			throw new TestConfigurationError(
					"Unable to copy " + description + " to "
							+ gradeReport.toString());
		}
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
					studentGradingArea, null, null,
					StandardCopyOption.REPLACE_EXISTING);
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
				int score = tc.performTest(submission, false,
						submitterStage, buildStatus);
				System.out.println("  Test case " + testName
						+ ": " + score + "%.");
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
