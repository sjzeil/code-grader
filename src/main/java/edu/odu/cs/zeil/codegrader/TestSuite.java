package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSuite {

	private static final int MAX_MESSAGE_LENGTH = 5000;
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

	private String contentHash;

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
		asst.setDateCommand(properties.findDateSubmitted);
		contentHash = "";
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
		StringBuilder classSummary = new StringBuilder();
		classSummary.append("student," + getAssignmentName() + "\n");
		File[] recordedGrades = assignment.getRecordingDirectory()
			.toFile().listFiles();
		if (recordedGrades != null) {
			for (File submissionFile : recordedGrades) {
				if (submissionFile.isDirectory()) {
					Optional<File> scoreFile = FileUtils.findFile(
						submissionFile.toPath(), ".total");
					if (scoreFile.isPresent()) {
						String score = FileUtils.readTextFile(scoreFile.get())
								.trim();
						classSummary.append(submissionFile.getName());
						classSummary.append(",");
						classSummary.append(score);
						classSummary.append("\n");
					}
				}
			}
			Path classSummaryFile = assignment.getRecordingDirectory()
					.resolve("classSummary.csv");
			FileUtils.writeTextFile(classSummaryFile, classSummary.toString());
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
		if ((!assignment.getInPlace()) 
			&& submitterStage.getStageDir().toFile().exists()) {
			submitterStage.clear();
		}
		boolean proceedWithGrading = true;
		Path recordAt;
		if (!assignment.getInPlace()) {
			recordAt = submission.getRecordingDir();
			if (recordAt.toFile().isDirectory()) {
				if (!needsRegrading(recordAt, submission)) {
					proceedWithGrading = false;
				}
			}
			if (proceedWithGrading) {
				recordAt.toFile().mkdirs();
				copyTestSuiteToRecordingArea(submission);
				submitterStage.setupStage();
			} else {
				System.out.println("  Has already been graded - skipping.");
			}
		} else {
			recordAt = assignment.getTestSuiteDirectory();
		}
		if (proceedWithGrading) {
			Stage.BuildResult buildResults = submitterStage.buildCode();
			buildScore = (buildResults.getStatusCode() == 0) ? MAX_SCORE : 0;
			buildMessage = buildResults.getMessage();
			System.out.println("  Building submitted code: " 
				+ buildScore + "%.");
			FileUtils.writeTextFile(recordAt.resolve("build.score"),
					Integer.toString(buildScore) + "\n");
			FileUtils.writeTextFile(recordAt.resolve("build.message"),
					buildResults.getMessage() + "\n");
			runTests(submission, buildResults.getStatusCode());
			generateReports(submission);
			recordContentHash(recordAt, submission);
		}
	}

	/**
	 * Record a hash based on the contents of the submission directory
	 * so that future runs of the same test suite can tell whether the
	 * submitted code has changed sint it was last graded.
	 * 
	 * @param recordAt directory where grade results should be recorded.
	 * @param submission the submission to check
	 */
	private void recordContentHash(Path recordAt, Submission submission) {
		if (contentHash == null || contentHash.equals("")) {
			contentHash = computeContentHash(
				submission.getSubmissionDirectory());
		}
		Path hashFile = recordAt.resolve(submission.getSubmittedBy() 
			+ ".hash");
		FileUtils.writeTextFile(hashFile, contentHash + "\n");
	}

	/**
	 * Check to see if a submission needs to be (re)graded.
	 * @param recordAt directory where grade results should be recorded.
	 * @param submission the submission to check
	 * @return true if submission should be regraded.
	 */
	public boolean needsRegrading(Path recordAt, Submission submission) {
		Path totalFile = recordAt.resolve(submission.getSubmittedBy() 
			+ ".total");
		if (!totalFile.toFile().exists()) {
			// has not been graded yet.
			return true;
		}
		Path hashFile = recordAt.resolve(submission.getSubmittedBy() 
			+ ".hash");
		if (!hashFile.toFile().exists()) {
			// no record of previous content
			return true;
		}
		// See if content has changed since the submission was last graded.
		String oldHash = FileUtils.readTextFile(hashFile.toFile()).trim();
		contentHash = computeContentHash(submission.getSubmissionDirectory());
		return !contentHash.equals(oldHash);
	}

	private String computeContentHash(Path submissionDirectory) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new TestConfigurationError(e.getMessage());
		}
		digestAllFiles(submissionDirectory.toFile(), digest);
		byte[] digested = digest.digest();
		StringBuffer result = new StringBuffer();
		final int byte255 = 0xff;
        for (byte aByte : digested) {
            result.append(Integer.toHexString(aByte & byte255));
        }
		return result.toString();
	}

	private void digestAllFiles(File dirOrFile, MessageDigest digest) {
		if (dirOrFile.isDirectory()) {
			if (!dirOrFile.getName().equals(".git")) {
				File[] contents = dirOrFile.listFiles();
				if (contents != null) {
					Arrays.sort(contents);
					for (File within: contents) {
						digestAllFiles(within, digest);
					}
				}
			}
		} else {
			try {
				byte[] bytes = Files.readAllBytes(dirOrFile.toPath());
				digest.update(bytes);
			} catch (IOException e) {
				logger.warn("Problem computing digest of " + dirOrFile, e);
			}
		}
	}

	private class Detail {
		public String name;
		public int weight;
		public int score;
		public String message;

		Detail(String aName, int aWeight, int aScore, String aMessage) {
			name = aName;
			weight = aWeight;
			score = aScore;
			message = aMessage;
		}

		public String toString() {
			return "<tr><td><i>" + htmlEncode(name)
				+ "</i></td><td>" + score
				+ "</td><td>" + weight
				+ "</td><td><pre>" + htmlEncode(message.trim())
				+ "</pre></td></tr>\n";
		}
	}

	private void generateReports(Submission submission) {
		System.out.println("  Generating reports...");
		Path gradeReport = submission.getRecordingDir()
				.resolve(submission.getSubmittedBy() + ".html");

		ArrayList<Detail> details = new ArrayList<>();
		details.add(new Detail("Build", 
			properties.build.weight,
			buildScore, buildMessage));
		
		writeTestCaseSummary(submission, details);

		int studentSubtotalScore = computeSubTotal(details);
		int daysLate = computeDaysLate(submission);
		int penalty = computeLatePenalty(daysLate);
		int studentTotalScore = (100 - penalty) * studentSubtotalScore;
		studentTotalScore = (int) Math.round(
			((float) studentTotalScore) / 100.0);

		writeHTMLReport(submission, gradeReport,
			 details, studentSubtotalScore, daysLate, 
			 penalty, studentTotalScore);
	
		System.out.println("  Total for " + submission.getSubmittedBy()
		+ " is " + studentTotalScore);
   		FileUtils.writeTextFile(
		   submission.getRecordingDir()
				   .resolve(submission.getSubmittedBy() + ".total"),
		   "" + studentTotalScore + "\n");

	}


	private int computeDaysLate(Submission submission) {
		// TODO
		return 0;
	}

	private int computeLatePenalty(int daysLate) {
		//TODO
		return 0;
	}

	private int computeSubTotal(ArrayList<Detail> details) {
		int weightedSum = 0;
		int weights = 0;
		for (Detail detail: details) {
			weightedSum += detail.score * detail.weight;
			weights += detail.weight;
		}
		float score = ((float) weightedSum) / ((float) weights);
		return (int) Math.round(score);
	}

	private void writeHTMLReport(Submission submission, Path gradeReport, 
		ArrayList<Detail> details,
		int studentSubtotalScore, int daysLate, int penalty, 
		int studentTotalScore) {

		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append("<html><head>\n");
		htmlContent.append(element("title", 
			"Grade report for " + getAssignmentName() 
			+ ": " + submission.getSubmittedBy()));
		htmlContent.append("\n</head><body>\n");
		htmlContent.append(element("h1", 
			"Grade report for " + getAssignmentName() 
			+ ": " + submission.getSubmittedBy()));

		addAssignmentInfo(htmlContent,
			submission, studentSubtotalScore, daysLate, penalty, 
			studentTotalScore);

		htmlContent.append(element("h2", "Details"));
		addAssignmentDetails(htmlContent, details);

		htmlContent.append("</body></html>\n");
		
		FileUtils.writeTextFile(
			submission.getRecordingDir()
				.resolve(submission.getSubmittedBy() + ".html"),
			htmlContent.toString());
	}

	private void addAssignmentDetails(StringBuilder htmlContent, 
		ArrayList<Detail> details) {
		
		htmlContent.append("<table border='1'>\n");
		for (Detail detail: details) {
			htmlContent.append(detail.toString());
		}
		htmlContent.append("</table>\n");
	}

	private void addAssignmentInfo(StringBuilder content,
		Submission submission, 
		int studentSubtotalScore, int daysLate, int penalty,
		int studentTotalScore) {

		content.append("<table border='1'>\n");
		String dueDate = properties.dueDate;
		String submissionDate = submission.getSubmissionDate();

		if (!submissionDate.equals("")) {
			content.append(row("Submitted:", submissionDate));
			if (!dueDate.equals("")) {
				content.append(row("Due:", dueDate));
				if (daysLate > 0) {
					content.append(row("Days late:", "" + daysLate));
				}
			}
		}
		if (penalty > 0) {
			content.append(row("Subtotal:", "" + studentSubtotalScore
			+ "%"));
			content.append(row("Penalties:", "" + -penalty + "%"));
		}
		content.append(row("Total:", "" + studentTotalScore + "%"));
		content.append("</table>\n");
	}

	private String row(String title, String value) {
		return "<tr><td><i>" + htmlEncode(title) 
			+ "</i></td><td>" + htmlEncode(value)
			+ "</td></tr>\n";
	}

	private Object element(String tagname, String content) {
		return "<" + tagname + ">" + htmlEncode(content) + "</" + tagname + ">";
	}

	private void writeTestCaseSummary(Submission submission, 
		ArrayList<Detail> details) {
		// Write out the tests summary.
		Path testsSummaryFile = submission.getRecordingDir()
				.resolve("testsSummary.csv");
		StringBuilder testsSummary = new StringBuilder();
		testsSummary.append("Test,Score,Weight,Msgs\n");

		File[] testCases = submission.getTestSuiteDir().toFile().listFiles();
		if (testCases != null) {
			Arrays.sort(testCases);
			for (File testCase : testCases) {
				if (testCase.isDirectory()) {
					TestCaseProperties tcProps = new TestCaseProperties(
							assignment, testCase.getName());
					TestCase tc = new TestCase(tcProps);
					String testName = tc.getProperties().getName();
					int score = submission.getScore(testName);
					int weight = tc.getProperties().getWeight();
					String message = submission.getMessage(testName);
					details.add(new Detail(testName, weight, score, message));
					testsSummary.append("\"" + testName + "\",");
					testsSummary.append(""
						+ score + ",");
					testsSummary.append("" + weight
						+ ",");
					testsSummary.append("\""
						+ csvEncode(message) + "\"\n");
				}
			}
		}
		FileUtils.writeTextFile(testsSummaryFile, testsSummary.toString());
	}

	private String csvEncode(String msg) {
		if (msg.length() > MAX_MESSAGE_LENGTH) {
			msg = msg.substring(0, MAX_MESSAGE_LENGTH - 1) 
				+ "\n[message clipped after " 
				+ MAX_MESSAGE_LENGTH + " characters]";
		}
		return msg.replace("\"", "'");
	}

	private String htmlEncode(String msg) {
		if (msg.length() > MAX_MESSAGE_LENGTH) {
			msg = msg.substring(0, MAX_MESSAGE_LENGTH - 1) 
				+ "\n[message clipped after " 
				+ MAX_MESSAGE_LENGTH + " characters]";
		}
		msg = msg.replace("&", "&amp;");
		msg = msg.replace("<", "&lt;");
		msg = msg.replace(">", "&gt;");
		return msg;
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
			Path parent = suite.toAbsolutePath().getParent();
			return parent.toFile().getName();
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
	private void runTests(Submission submission, int buildStatus) {
		Path suiteDir = submission.getTestSuiteDir();
		File[] testCases = suiteDir.toFile().listFiles();
		if (testCases == null || testCases.length == 0) {
			throw new TestConfigurationError(
					"No test case directories in " + suiteDir.toString());
		}
		Arrays.sort(testCases);
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
