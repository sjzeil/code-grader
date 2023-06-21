package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSuite implements Iterable<TestCase> {

	private static final int MAX_MESSAGE_LENGTH = 5000;
	private static final int MAX_SCORE = 100;

	private TestSuiteProperties properties;
	private Assignment assignment;
	private Path testSuiteDirectory;
	private Set<String> testsToPerform;
	private Set<String> submissionsToRun;
	private Set<String> activeTags;
	private Stage goldStage;
	private Stage submitterStage;
	private List<TestCase> completedCases;
	private Queue<TestCaseProperties> casesToBeRun;

	private String contentHash;

	/**
	 * Collection of test cases.
	 */
	private List<TestCaseProperties> cases;

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
		properties = new TestSuiteProperties();
		Optional<File> propsFile = FileUtils.findFile(
				testSuiteDirectory,
				".yaml");
		if (propsFile.isPresent()) {
			properties = TestSuiteProperties.loadYAML(propsFile.get());
		}
		goldStage = null;
		submitterStage = null;
		contentHash = "";
		activeTags = new HashSet<String>();
		cases = new ArrayList<>();
		completedCases = new ArrayList<>();
		casesToBeRun = new LinkedList<>();
		initializeTestCases();
	}

	private void initializeTestCases() {
		Path suiteDir = assignment.getTestSuiteDirectory();
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
				cases.add(tcProperties);
			}
		}
		if (cases.size() == 0) {
			throw new TestConfigurationError(
					"No test case directories in " + suiteDir.toString());
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
	 * Set the due date for this suite.
	 * 
	 * @param date the new date
	 */
	public void setDueDate(String date) {
		properties.dueDate = date;
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
	 * Submission dates will be determined by the last modification
	 * date of a file.
	 * 
	 * @param filePath path to submission file
	 */
	public void setSubmissionDateMod(String filePath) {
		properties.dateSubmitted.mod = filePath;
		properties.dateSubmitted.in = "";
		properties.dateSubmitted.git = false;
	}

	/**
	 * Submission dates will be read from a file.
	 * 
	 * @param filePath path to submission file
	 */
	public void setSubmissionDateIn(String filePath) {
		properties.dateSubmitted.in = filePath;
		properties.dateSubmitted.mod = "";
		properties.dateSubmitted.git = false;
	}

	/**
	 * Submission dates will be read from a file.
	 * 
	 * @param setByGit true means that if a submission directory is a
	 *                 git repository, the last commit date will be used as the
	 *                 submission
	 *                 date.
	 */
	public void setSubmissionDateByGit(boolean setByGit) {
		properties.dateSubmitted.in = "";
		properties.dateSubmitted.mod = "";
		properties.dateSubmitted.git = setByGit;
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
			System.out.println("Setting up gold stage.");
			goldStage = new Stage(assignment, properties);
			goldStage.setupStage();
		}
		
		SubmissionSet submissions = new SubmissionSet(assignment);
		if (submissionsToRun.size() > 0) {
			submissions.setSelectedSubmissions(submissionsToRun);
		}

		for (Submission submission : submissions) {
			processThisSubmission(submission);
		}
		if (!assignment.getInPlace()) {
			StringBuilder classSummary = new StringBuilder();
			classSummary.append("student," + getAssignmentName() + "\n");

			submissions.setSelectedSubmissions(new HashSet<String>());
			for (Submission submission : submissions) {
				File submissionFile = submission.getRecordingDir().toFile();
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

		if (submissionsToRun.size() == 0 && !assignment.getInPlace()) {
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
				if (!proceedWithGrading) {
					System.out.println("  Has already been graded - skipping.");
				}
			}
			if (proceedWithGrading) {
				if (properties.submissionLock != null) {
					LocalDateTime lockDate 
						= parseDateTime(getLockDate(submission));
					LocalDateTime submissionDateTime 
						= parseDateTime(getSubmissionDate(submission));
					proceedWithGrading 
						= submissionDateTime.compareTo(lockDate) <= 0;
					if (!proceedWithGrading) {
						String msg = "Submitted by " 
							+ submission.getSubmittedBy()
							+ " at " + submissionDateTime
							+ " but locked at " + lockDate;
						logger.warn(msg);
						System.out.println(msg);
					}
				}
			}

			if (proceedWithGrading) {
				if (!recordAt.toFile().exists()) {
					boolean ok = recordAt.toFile().mkdirs();
					if (!ok) {
						logger.warn("Unable to create directory " + recordAt);
					}
				}
				copyTestSuiteToRecordingArea(submission);
				if (!assignment.getInPlace()) {
					submitterStage.setupStage();
				}
			}
		} else {
			recordAt = assignment.getTestSuiteDirectory();
		}
		if (proceedWithGrading) {
			runTests(submission);
			generateReports(submission);
			recordContentHash(recordAt, submission);
		}
	}

	/**
	 * Record a hash based on the contents of the submission directory
	 * so that future runs of the same test suite can tell whether the
	 * submitted code has changed sint it was last graded.
	 * 
	 * @param recordAt   directory where grade results should be recorded.
	 * @param submission the submission to check
	 */
	private void recordContentHash(Path recordAt, Submission submission) {
		if (!assignment.getInPlace()) {
			if (contentHash == null || contentHash.equals("")) {
				contentHash = computeContentHash(
						submission.getSubmissionDirectory());
			}
			Path hashFile = recordAt.resolve(submission.getSubmittedBy()
					+ ".hash");
			FileUtils.writeTextFile(hashFile, contentHash + "\n");
		}
	}

	/**
	 * Check to see if a submission needs to be (re)graded.
	 * 
	 * @param recordAt   directory where grade results should be recorded.
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
					for (File within : contents) {
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
		private String name;
		private int weight;
		private int score;
		private String message;

		Detail(String aName, int aWeight, int aScore, String aMessage) {
			name = aName;
			weight = aWeight;
			score = aScore;
			message = aMessage;
		}

		public String toString() {
			Message nameMsg = new Message(name);
			Message oracleMsg = new Message(message.trim());
			return "<tr><td><i>" + nameMsg.toHTML()
					+ "</i></td><td>" + score
					+ "</td><td>" + weight
					+ "</td><td><pre>" + oracleMsg.toHTML()
					+ "</pre></td></tr>\n";
		}

	}

	private void generateReports(Submission submission) {
		System.out.println("  Generating reports...");
		Path gradeReport = submission.getRecordingDir()
				.resolve(submission.getSubmittedBy() + ".html");

		ArrayList<Detail> details = new ArrayList<>();
		writeTestCaseSummary(submission, details);

		int studentSubtotalScore = computeSubTotal(details);
		int daysLate = computeDaysLate(submission);
		int penalty = computeLatePenalty(daysLate);
		int studentTotalScore = (MAX_SCORE - penalty) * studentSubtotalScore;
		studentTotalScore = (int) Math.round(
				((float) studentTotalScore) / ((float) MAX_SCORE));

		writeHTMLReport(submission, gradeReport,
				details, studentSubtotalScore, daysLate,
				penalty, studentTotalScore);

		System.out.println("  Total for " + submission.getSubmittedBy()
				+ " is " + studentTotalScore);
		FileUtils.writeTextFile(
				submission.getRecordingDir()
						.resolve(submission.getSubmittedBy() + ".total"),
				"" + studentTotalScore + "\n");

		Path gradeLogFile = submission.getRecordingDir()
				.resolve("gradeLog.csv");
		recordInGradeLog(gradeLogFile, submission, studentTotalScore);
	}

	private void recordInGradeLog(Path gradeLogFile, 
	  Submission submission, int studentTotalScore) {
		if (!Files.exists(gradeLogFile)) {
			try (FileWriter gradeLog = new FileWriter(gradeLogFile.toFile())) {
				gradeLog.write("Student,Date,Grade\n");
			} catch (IOException ex) {
				logger.error("Cannot write to grade log " + gradeLogFile, ex);
			}
		}
		try (FileWriter gradeLog = new FileWriter(
				gradeLogFile.toFile(), true)) {
			gradeLog.write("\"" + submission.getSubmittedBy() + "\",\"" 
				+ getSubmissionDate(submission) + "\"," 
				+ studentTotalScore + "\n");
		} catch (IOException ex) {
			logger.error("Cannot append to grade log " + gradeLogFile, ex);
		}
	}

	/**
	 * If both a due date and a submission date are available, compute
	 * how many days late this submission is.
	 * 
	 * @param submission a submission
	 * @return number of days late (0 if on time)
	 */
	public int computeDaysLate(Submission submission) {
		String dueDateStr = properties.dueDate;
		String submissionDateStr = getSubmissionDate(submission);

		if (dueDateStr.equals("") || submissionDateStr.equals("")) {
			return 0;
		}

		try {
			LocalDateTime dueDateTime = parseDateTime(dueDateStr);
			LocalDateTime submissionDateTime = parseDateTime(submissionDateStr);
			if (submissionDateTime.isAfter(dueDateTime)) {
				// submission is late
				LocalDateTime latePeriodStart = dueDateTime.plusSeconds(1);
				long days = 1
					+ ChronoUnit.DAYS.between(latePeriodStart,
						submissionDateTime);
				return (int) days;
			} else {
				return 0;
			}
		} catch (DateTimeParseException e) {
			return 0;
		}
	}

	static final Pattern MM_DD_YYYY 
		= Pattern.compile("([0-9]+)/([0-9]+)/([0-9]+)");
	static final Pattern YYYY_MM_DD
		= Pattern.compile("([0-9]+)-([0-9]+)-([0-9]+)");
	static final Pattern HH_MM_SS
		= Pattern.compile("([0-9]+)[^0-9]([0-9]+)[^0-9]([0-9]+)");
	static final Pattern HH_MM = Pattern.compile("([0-9]+)[^0-9]([0-9]+)");

	/**
	 * Flexible parsing of dates and time.
	 * 
	 * @param dateTimeString a date or date and time
	 * @return equivalent date-time
	 * @throws DateTimeParseException
	 */
	public LocalDateTime parseDateTime(String dateTimeString)
			throws DateTimeParseException {

		final int lastHour = 23;
		final int lastMinuteOrSecond = 59;

		try {
			LocalDateTime dateTime = LocalDateTime.parse(dateTimeString,
				DateTimeFormatter.ofPattern("EEE LLL [d][dd] HH:mm:ss yyyy"));
			return dateTime;
		} catch (DateTimeParseException ex) {
			// Continue on
		}

		int year = 0;
		int month = 0;
		int day = 0;
		int hour = lastHour;
		int minute = lastMinuteOrSecond;
		int second = lastMinuteOrSecond;
		int offset = 0;

		Matcher m = MM_DD_YYYY.matcher(dateTimeString);
		final int group3 = 3;

		if (m.lookingAt()) {
			month = Integer.parseInt(m.group(1));
			day = Integer.parseInt(m.group(2));
			year = Integer.parseInt(m.group(group3));
			offset = m.end(group3);
		} else {
			m = YYYY_MM_DD.matcher(dateTimeString);
			if (m.lookingAt()) {
				year = Integer.parseInt(m.group(1));
				month = Integer.parseInt(m.group(2));
				day = Integer.parseInt(m.group(group3));
				offset = m.end(group3);
			} else {
				throw new DateTimeParseException("Could not parse ",
						dateTimeString, 0);
			}
		}
		if (offset > 0 && offset < dateTimeString.length()) {
			m = HH_MM_SS.matcher(dateTimeString);
			if (m.find(offset + 1)) {
				hour = Integer.parseInt(m.group(1));
				minute = Integer.parseInt(m.group(2));
				second = Integer.parseInt(m.group(group3));
			} else {
				m = HH_MM.matcher(dateTimeString);
				if (m.find(offset + 1)) {
					hour = Integer.parseInt(m.group(1));
					minute = Integer.parseInt(m.group(2));
				}
			}
		}
		return LocalDateTime.of(year, month, day, hour, minute, second);
	}

	private int computeLatePenalty(int daysLate) {
		if (daysLate <= 0) {
			return 0;
		}

		final int latePenaltiesLen = properties.latePenalties.length;
		if (daysLate < latePenaltiesLen) {
			return properties.latePenalties[daysLate - 1];
		} else {
			return properties.latePenalties[latePenaltiesLen - 1];
		}
	}

	private int computeSubTotal(ArrayList<Detail> details) {
		int weightedSum = 0;
		int weights = 0;
		for (Detail detail : details) {
			weightedSum += detail.score * detail.weight;
			weights += detail.weight;
		}
		float score = ((float) weightedSum) / ((float) weights);
		return (int) Math.round(score);
	}

	private static final String CSS = "<style>\n.expected {background-color: green;}\n.observed {background-color: red;} </style>";

	private void writeHTMLReport(Submission submission, Path gradeReport,
			ArrayList<Detail> details,
			int studentSubtotalScore, int daysLate, int penalty,
			int studentTotalScore) {

		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append("<html><head>\n");
		htmlContent.append(CSS);
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

		Path reportFile = submission.getRecordingDir()
						.resolve(submission.getSubmittedBy() + ".html");
		FileUtils.writeTextFile(
				reportFile,
				htmlContent.toString());
		if (assignment.getInPlace()) {
			System.err.println("Grade report written to " 
				+ reportFile.toString());
		}
	}

	private void addAssignmentDetails(StringBuilder htmlContent,
			ArrayList<Detail> details) {

		htmlContent.append("<table border='1'>\n");
		htmlContent.append("<tr><th>Test</th><th>Score</th>"
				+ "<th>Weight</th><th>Details</th></tr>\n");
		for (Detail detail : details) {
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
		String submissionDate = getSubmissionDate(submission);

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
			content.append(row("Subtotal:", "" + studentSubtotalScore));
			content.append(row("Penalties:", "" + -penalty + "%"));
		}
		content.append(row("Total:", "" + studentTotalScore));
		content.append("</table>\n");
	}

	private String row(String title, String value) {
		Message titleMsg = new Message(title);
		Message valueMessage = new Message(value);
		return "<tr><td><i>" + titleMsg.toHTML()
				+ "</i></td><td>" + valueMessage.toHTML()
				+ "</td></tr>\n";
	}

	private Object element(String tagName, String content) {
		Message contentMsg = new Message(content);
		return "<" + tagName + ">" + contentMsg.toHTML() + "</" + tagName + ">";
	}

	private void writeTestCaseSummary(Submission submission,
			ArrayList<Detail> details) {
		// Write out the tests summary.
		Path testsSummaryFile = submission.getRecordingDir()
				.resolve("testsSummary.csv");
		StringBuilder testsSummary = new StringBuilder();
		testsSummary.append("Test,Score,Weight,Msgs\n");

		for (TestCase tc : completedCases) {
			String testName = tc.getProperties().name;
			String description = tc.getProperties().getDescription();
			if (!description.equals("")) {
				description = testName + ": " + description;
			} else {
				description = testName;
			}
			int score = submission.getScore(testName);
			int weight = tc.getProperties().getWeight();
			String message = submission.getMessage(testName);
			details.add(new Detail(testName, weight, score, message));
			testsSummary.append("\"" + description + "\",");
			testsSummary.append("" + score + ",");
			testsSummary.append("" + weight + ",");
			testsSummary.append("\"" + csvEncode(message) + "\"\n");
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
			if (parent != null) {
				return parent.toFile().getName();
			} else {
				return "---";
			}
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
		if (!assignment.getInPlace()) {
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
	}

	public static class BuildResult {
		private int statusCode;
		private String message;

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

		/**
		 * @return the statusCode
		 */
		public int getStatusCode() {
			return statusCode;
		}

		/**
		 * @param aStatusCode the statusCode to set
		 */
		public void setStatusCode(int aStatusCode) {
			this.statusCode = aStatusCode;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @param aMessage the message to set
		 */
		public void setMessage(String aMessage) {
			this.message = aMessage;
		}

	}


	/**
	 * Runs all selected tests for a given submission. Assumes that code
	 * for gold version and submission have been built. Test results are
	 * recorded in the recording area.
	 * 
	 * @param submission  submission to be tested
	 */
	private void runTests(Submission submission) {
		clearTags();
		completedCases.clear();
		setTag("build");
		if (casesToBeRun.isEmpty()) {
			TestCaseProperties builder = new 
				DefaultBuildCase(properties, assignment).generate();
			casesToBeRun.add(builder);
		}
		while (!casesToBeRun.isEmpty()) {
			TestCaseProperties tcp = casesToBeRun.remove();
			TestCase tc = new TestCase(tcp);
			performAndScoreTest(submission, tc);
			completedCases.add(tc);
		}
		/*
		setTag("test");  // If a build task activated "test", then this
		                         // has no effect.
		while (!casesToBeRun.isEmpty()) {
			TestCaseProperties tcp = casesToBeRun.remove();
			TestCase tc = new TestCase(tcp);
			performAndScoreTest(submission, tc);
			completedCases.add(tc);
		}
		*/
	}

	private void performAndScoreTest(Submission submission, TestCase tc) {
		String testName = tc.getProperties().name;
		String failIf = tc.getProperties().getFailIf();
		int score = 0;
		if ((!failIf.equals("")) && isTagActive(failIf)) {
			// Immediately fail this case
			tc.failTest(submission, "Case was not run (" + failIf + ")");
		} else { 
			goldStage = new Stage(assignment, properties);
			if (assignment.getGoldDirectory() != null) {
				tc.performTest(submission, true, goldStage);
			}
			submitterStage = new Stage(assignment, submission, properties);
			score = tc.performTest(submission, false,
					submitterStage);
		}
		if (score == MAX_SCORE) {
			for (String tagName: tc.getProperties().onSuccess) {
				setTag(tagName);
			}
		} else {
			for (String tagName: tc.getProperties().onFail) {
				setTag(tagName);
			}
		}
		System.out.println("  Test case " + testName
				+ ": " + score + "%.");
	}

	private boolean isAValidTestCase(File testCase) {
		if (!testCase.isDirectory()) {
			return false;
		}
		String testName = testCase.getName();
		if (testName.startsWith(".") || testName.startsWith("__")) {
			return false;
		}
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

	private class TestCaseIterator implements Iterator<TestCase> {

		private Iterator<TestCaseProperties> caseIterator;

		TestCaseIterator() {
			caseIterator = cases.iterator();
		}

		@Override
		public boolean hasNext() {
			return caseIterator.hasNext();
		}

		@Override
		public TestCase next() {
			TestCaseProperties tc = caseIterator.next();
			return new TestCase(tc);
		}

	}

	/**
	 * Provides access to the list of test cases in this suite.
	 */
	@Override
	public Iterator<TestCase> iterator() {
		return new TestCaseIterator();
	}

	/**
	 * Attempt to determine when this submission was turned in.
	 * 
	 * @param sub the submission
	 * @return a string representing a date and/or time, or "".
	 */
	public String getSubmissionDate(Submission sub) {
		if (!properties.dateSubmitted.in.equals("")) {
			return getSubmissionDateIn(sub.getSubmissionDirectory(),
					properties.dateSubmitted.in);
		} else if (!properties.dateSubmitted.mod.equals("")) {
			return getSubmissionDateMod(sub.getSubmissionDirectory(),
					properties.dateSubmitted.mod);
		} else if (properties.dateSubmitted.git) {
			return getSubmissionDateByGit(sub.getSubmissionDirectory());
		} else {
			return "2020-01-01 00:00:00";
		}
	}

	/**
	 * Attempt to determine when this submission was turned in.
	 * 
	 * @param sub the submission
	 * @return a string representing a date and/or time, or "".
	 */
	public String getLockDate(Submission sub) {
		ParameterHandling ph = new ParameterHandling(assignment, 
			null, null, sub, null, null);
		if (!properties.submissionLock.in.equals("")) {
			return getLockDateIn(ph.parameterSubstitution(
				properties.submissionLock.in));
		} else if (!properties.submissionLock.mod.equals("")) {
			return getLockDateMod(ph.parameterSubstitution(
				properties.submissionLock.mod));
		} else {
			return "2999-12-31 23:59:59";
		}
	}


	private String getSubmissionDateByGit(Path submissionDir) {
		Path potentialGitDir = submissionDir.resolve(".git");
		if (potentialGitDir.toFile().isDirectory()) {
			String gitCmd = "git log -1 --date=format:%Y-%m-%d_%T --format=%ad";
			return getSubmissionDateByCommand(gitCmd, potentialGitDir);
		} else {
			return "2021-01-01 00:00:00";
		}
	}

	private String getSubmissionDateByCommand(String getDateCommand,
			Path context) {
		final int oneMinute = 60;
		ExternalProcess commandRunner = new ExternalProcess(
				context, getDateCommand, oneMinute,
				null, getDateCommand);
		commandRunner.execute();
		String output = commandRunner.getOutput();
		return output.trim();
	}

	private String getSubmissionDateIn(Path submissionDir,
			String getDateFile) {
		getDateFile = getDateFile.replace("@I",
				submissionDir.toAbsolutePath().toString());
		Path fileName = submissionDir.getFileName();
		String submitFileName = (fileName == null) ? "" : fileName.toString();
		getDateFile = getDateFile.replace("@i",
				submitFileName);

		// Use contents of the file.
		String dateStr = FileUtils
				.readTextFile(Paths.get(getDateFile).toFile()).trim();
		return dateStr;
	}

	private String getSubmissionDateMod(Path submissionDir,
			String getDateFile) {
		getDateFile = getDateFile.replace("@I",
				submissionDir.toAbsolutePath().toString());
		Path fileName = submissionDir.getFileName();
		String submitFileName = (fileName == null) ? "" : fileName.toString();
		getDateFile = getDateFile.replace("@i",
				submitFileName);

		// Use modification date of the file
		Path criticalFile = Paths.get(getDateFile);
		try {
			FileTime fileTime = Files.getLastModifiedTime(criticalFile);
			Instant instant = fileTime.toInstant();
			LocalDateTime modDateTime = instant
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(modDateTime);
		} catch (IOException e) {
			return "";
		}
	}

	private String getLockDateIn(String getDateFile) {
		Path fileName = Paths.get(getDateFile);
		if (fileName != null && fileName.toFile().exists()) {
			// Use contents of the file.
			String dateStr = FileUtils
					.readTextFile(fileName.toFile()).trim();
			return dateStr;
		} else {
			return "2099-12-31 23:59:59";
		}
	}


	private String getLockDateMod(String getDateFile) {
		Path fileName = Paths.get(getDateFile);
		if (fileName != null && fileName.toFile().exists()) {
			// Use modification date of the file
			try {
				FileTime fileTime = Files.getLastModifiedTime(fileName);
				Instant instant = fileTime.toInstant();
				LocalDateTime modDateTime = instant
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
					modDateTime);
			} catch (IOException e) {
				return "";
			}
		} else {
			return "2099-12-31 23:59:59";
		}
	}

	/**
	 * 
	 * @return the number of test cases currently queued up to be run.
	 */
	public int getNumTestsToPerform() {
		return casesToBeRun.size();
	}

	/**
	 * @param tagName tag name
	 * @return true iff this tag has been set
	 */
    public boolean isTagActive(String tagName) {
        return activeTags.contains(tagName);
    }

	/**
	 * Set the named tag and queue up any unperformed test cases with
	 * that tag name as its kind.
	 * @param tagName a tag name
	 */
    public void setTag(String tagName) {
		if (!activeTags.contains(tagName)) {
			activeTags.add(tagName);
			ArrayList<TestCaseProperties> toBeAdded = new ArrayList<>();
			for (TestCaseProperties tc: cases) {
				if (tc.getKind().equals(tagName)) {
					toBeAdded.add(tc);
				}
			}
			Collections.sort(toBeAdded);
			casesToBeRun.addAll(toBeAdded);
		}
    }

	/**
	 * Clear the named tag and remove any unperformed test cases with
	 * that tag name as its kind from the queue of tests to be performed.
	 * @param tagName a tag name
	 */
    public void clearTag(String tagName) {
		activeTags.remove(tagName);
		Queue<TestCaseProperties> stillToRun = new LinkedList<>();
		for (TestCaseProperties tc: casesToBeRun) {
			if (!tc.getKind().equals(tagName)) {
				stillToRun.add(tc);
			}
		}
		casesToBeRun = stillToRun;
    }

	/**
	 * Clear all tags and clear the queue of tests to be performed.
	 */
    public void clearTags() {
		activeTags.clear();
		casesToBeRun.clear();
	}

	/**
	 * 
	 * @return the suite properties (for unit testing purposes)
	 */
	TestSuiteProperties getProperties() {
		return properties;
	}
}
