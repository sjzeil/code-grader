package edu.odu.cs.zeil.codegrader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;

public class TestTestSuite2 {

	// CHECKSTYLE:OFF

	private Path asstSrcPath = Paths.get("src", "test", "data",
			"explicitBuild");

	private Path asstDestPath = Paths.get("build", "test-data",
			"explicit-build");

	private Path testSuitePath = asstDestPath.resolve("tests");
	private Path stagingPath = Paths.get("build", "test-data", "stage");
	private Path submissionsPath = asstDestPath.resolve("submissions");
	private Path recordingPath = asstDestPath.resolve("Grades");
	private String studentName = "good";
	private Path submissionPath = submissionsPath.resolve(studentName);
	private Path instructorPath = asstDestPath.resolve("gold");
	private Submission submission;

	private Assignment asst;

	/**
	 * Set up assignment2 params test.
	 * 
	 * @throws IOException
	 * @throws TestConfigurationError
	 */
	@BeforeEach
	public void setup() throws IOException, TestConfigurationError {
		Path testData = Paths.get("build", "test-data");
		if (testData.toFile().exists()) {
			FileUtils.deleteDirectory(testData);
		}

		asstDestPath.toFile().getParentFile().mkdirs();
		stagingPath.toFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, asstDestPath, null, null,
				StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath);
		asst.setStagingDirectory(stagingPath);
		asst.setSubmissionsDirectory(submissionsPath);
		asst.setRecordingDirectory(recordingPath);
		asst.setInstructorCodeDirectory(instructorPath);

		submission = new Submission(asst, studentName, submissionPath);
	}

	@Test
	void testRegularSubmission() {
		TestSuite suite = new TestSuite(asst);

		suite.clearTheStage(stagingPath);

		suite.processThisSubmission(submission);

		// Were reports generated?

		assertTrue(submission.getRecordingDir()
				.resolve("testsSummary.csv")
				.toFile().exists());
		assertTrue(submission.getRecordingDir()
				.resolve(studentName + ".html")
				.toFile().exists());
		Path totalFile = submission.getRecordingDir()
				.resolve(studentName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("100\n", total); // 10% penalty
	}

	@Test
	void testClassReportingDefault() throws CsvValidationException, IOException {
		TestSuite suite = new TestSuite(asst);

		suite.clearTheStage(stagingPath);

		// Were reports generated?

		Submission jones1 = new Submission(asst, "jones", Paths.get("build"), "2025-01-01T12:00:00");
		suite.recordInGradeLog(jones1, 100);

		Submission smith1 = new Submission(asst, "smith", Paths.get("build"), "2025-01-03T12:00:00");
		suite.recordInGradeLog(smith1, 90);

		Submission jones2 = new Submission(asst, "jones", Paths.get("build"), "2025-01-05T12:00:00");
		suite.recordInGradeLog(jones2, 85);

		SubmissionSet submissions = new SubmissionSet(asst);
		submissions.add(jones1);
		submissions.add(jones2);
		submissions.add(smith1);
		suite.prepareClassSummary(submissions);

		Path summaryFile = suite.getClassGradeSummaryFile();
		assertTrue(summaryFile.toFile().exists());

		String[] found = searchGradeSummaryFor(summaryFile, "jones");
		assertNotNull(found, "No grade recorded for jones");
		assertEquals(found[1], "100");

		found = searchGradeSummaryFor(summaryFile, "smith");
		assertNotNull(found, "No grade recorded for smith");
		assertEquals(found[1], "90");

	}

	@Test
	void testClassReportingLastScore() throws CsvValidationException, IOException {
		TestSuite suite = new TestSuite(asst);

		suite.clearTheStage(stagingPath);
		suite.getProperties().bestScores = false;

		// Were reports generated?

		Submission jones1 = new Submission(asst, "jones", Paths.get("build"), "2025-01-01T12:00:00");
		suite.recordInGradeLog(jones1, 100);

		Submission smith1 = new Submission(asst, "smith", Paths.get("build"), "2025-01-03T12:00:00");
		suite.recordInGradeLog(smith1, 90);

		Submission jones2 = new Submission(asst, "jones", Paths.get("build"), "2025-01-05T12:00:00");
		suite.recordInGradeLog(jones2, 85);

		SubmissionSet submissions = new SubmissionSet(asst);
		submissions.add(jones1);
		submissions.add(jones2);
		submissions.add(smith1);
		suite.prepareClassSummary(submissions);

		Path summaryFile = suite.getClassGradeSummaryFile();
		assertTrue(summaryFile.toFile().exists());

		String[] found = searchGradeSummaryFor(summaryFile, "jones");
		assertNotNull(found, "No grade recorded for jones");
		assertEquals(found[1], "85");

		found = searchGradeSummaryFor(summaryFile, "smith");
		assertNotNull(found, "No grade recorded for smith");
		assertEquals(found[1], "90");

	}

	@Test
	void testClassReportingCutoff() throws CsvValidationException, IOException {
		TestSuite suite = new TestSuite(asst);
		suite.getProperties().cutoffDate = "2025-01-03T00:00:00";

		suite.clearTheStage(stagingPath);

		// Were reports generated?

		Submission jones1 = new Submission(asst, "jones", Paths.get("build"), "2025-01-01 12:00:00");
		suite.recordInGradeLog(jones1, 100);

		Submission smith1 = new Submission(asst, "smith", Paths.get("build"), "2025-01-03 12:00:00");
		suite.recordInGradeLog(smith1, 90);

		Submission jones2 = new Submission(asst, "jones", Paths.get("build"), "2025-01-05 12:00:00"); // after the
																										// cutoff date
		suite.recordInGradeLog(jones2, 100);

		SubmissionSet submissions = new SubmissionSet(asst);
		submissions.add(jones1);
		submissions.add(jones2);
		submissions.add(smith1);
		suite.prepareClassSummary(submissions);

		Path summaryFile = suite.getClassGradeSummaryFile();
		assertTrue(summaryFile.toFile().exists());

		String[] found = searchGradeSummaryFor(summaryFile, "jones");
		assertNotNull(found, "No grade recorded for jones");
		assertEquals(found[1], "100");

		found = searchGradeSummaryFor(summaryFile, "smith");
		assertNull(found, "Grade should not have been recorded for smith");

	}

	private String[] searchGradeSummaryFor(Path summaryFile, String studentName)
			throws IOException, CsvValidationException, FileNotFoundException {
		String[] row = { "xx", "", "" };
		String[] found = null;
		try (CSVReader csvReader = new CSVReader(new FileReader(summaryFile.toFile()))) {
			while (row != null) {
				if (row[0].equals(studentName)) {
					found = row;
				}
				row = csvReader.readNext();
			}
		}
		return found;
	}

}
