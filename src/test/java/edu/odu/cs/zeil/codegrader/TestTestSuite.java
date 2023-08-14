package edu.odu.cs.zeil.codegrader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class TestTestSuite {

	//CHECKSTYLE:OFF

	private Path asstSrcPath = Paths.get("src", "test", "data", 
		"java-noGold-assignment");

	private Path asstDestPath = Paths.get("build", "test-data", 
		"java-noGold-assignment");

	private Path testSuitePath = asstDestPath.resolve("Tests");
	private Path stagingPath = Paths.get("build", "test-data", "stage");
	private Path submissionsPath = asstDestPath.resolve("submissions");
	private Path recordingPath = asstDestPath.resolve("Grades");
	private String studentName = "perfect";
	private Path submissionPath = submissionsPath.resolve(studentName);
	private Submission submission;
	

	private Assignment asst;

	/**
	 * Set up assignment2 params test.
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

		submission = new Submission(asst, studentName, submissionPath);
	}


	@Test
	void testIncrementalProcessing() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Path recordAt = submission.getRecordingDir();

		assertTrue(suite.needsRegrading(recordAt, submission));

		suite.processThisSubmission(submission);

		assertFalse(suite.needsRegrading(recordAt, submission));

		FileUtils.writeTextFile(
			submission.getSubmissionDirectory().resolve("foo.txt"), 
			"Hello");

		assertTrue(suite.needsRegrading(recordAt, submission));


	}

	@Test
	void testLockedSubmission() {
		TestSuite suite = new TestSuite(asst);
		TestSuiteProperties props = suite.getProperties();
		props.setSubmissionLockIn("@R/@s.lock");
		submission.getRecordingDir().toFile().mkdirs();
		FileUtils.writeTextFile(
			submission.getRecordingDir().resolve("perfect.lock"),
			"1999-01-01 05:00  No more submissions.");
		suite.clearTheStage(stagingPath);

		Path recordAt = submission.getRecordingDir();

		assertTrue(suite.needsRegrading(recordAt, submission));

		suite.processThisSubmission(submission);

		Path totalFile = submission.getRecordingDir()
			.resolve(studentName + ".total");
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertThat(total.trim(), is(""));

	}

	@Test
	void testLaterLockedSubmission() {
		TestSuite suite = new TestSuite(asst);
		TestSuiteProperties props = suite.getProperties();
		props.setSubmissionLockIn("@R/@s.lock");
		submission.getRecordingDir().toFile().mkdirs();
		FileUtils.writeTextFile(
			submission.getRecordingDir().resolve("perfect.lock"),
			"2100-01-01 05:00  No more submissions.");
		suite.clearTheStage(stagingPath);

		Path recordAt = submission.getRecordingDir();

		assertTrue(suite.needsRegrading(recordAt, submission));

		suite.processThisSubmission(submission);

		Path totalFile = submission.getRecordingDir()
			.resolve(studentName + ".total");
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertThat(total.trim(), is("100"));

	}

	@Test
	void testDateParsing() {
		LocalDateTime dt0 = LocalDateTime.of(2022, 12, 15, 23, 59, 59);
		LocalDateTime dt1 = LocalDateTime.of(2022, 12, 15, 20, 58, 16);
		LocalDateTime dt2 = LocalDateTime.of(2022, 12, 1, 20, 58, 16);

		TestSuite suite = new TestSuite(asst);

		assertThat(suite.parseDateTime("2022-12-15"), is(dt0));
		assertThat(suite.parseDateTime("12/15/2022"), is(dt0));
		assertThat(suite.parseDateTime("2022-12-15_20:58:16"), is(dt1));
		assertThat(suite.parseDateTime("2022-12-15T20:58:16"), is(dt1));
		assertThat(suite.parseDateTime("12/15/2022 20:58:16"), is(dt1));
		assertThat(suite.parseDateTime("Thu Dec 15 20:58:16 2022"), is(dt1));
		assertThat(suite.parseDateTime("Thu Dec 1 20:58:16 2022"), is(dt2));
	}

	@Test
	void testDaysLate() {
		TestSuite suite = new TestSuite(asst);

		assertThat(suite.computeDaysLate(submission), is(0));

		suite.setDueDate("2022-09-15");
		Path timeFile = submissionPath.resolve("perfect.time");
		String timeFileStr = timeFile.toAbsolutePath().toString();
		FileUtils.writeTextFile(timeFile, "2022-09-09_20:58:16");

		suite.setSubmissionDateIn(timeFileStr);
		assertThat(suite.computeDaysLate(submission), is(0));

		// Use modification date instead of contents
		suite.setSubmissionDateMod(timeFileStr);
		assertThat(suite.computeDaysLate(submission), greaterThan(0));


		FileUtils.writeTextFile(timeFile, "09/15/2022 23:59:16");
		suite.setSubmissionDateIn(timeFileStr);
		assertThat(suite.computeDaysLate(submission), is(0));

		FileUtils.writeTextFile(timeFile, "09/17/2022 23:59:16");
		suite.setSubmissionDateIn(timeFileStr);
		assertThat(suite.computeDaysLate(submission), is(2));

		// Use modification date instead of contents
		FileUtils.writeTextFile(timeFile, "09/15/2022 23:59:16");
		suite.setSubmissionDateMod(timeFileStr);
		assertThat(suite.computeDaysLate(submission), greaterThan(0));
		
	}

	@Test
	void testTestCaseIdentification() {
		TestSuite suite = new TestSuite(asst);

		int i = 0;
		String[] expected = {"t10000", "t2", "t3"};
		for (TestCase tc: suite) {
			assertThat(tc.getProperties().name, is(expected[i]));
			++i;
		}

	}

	@Test
	void testJavaDefaultLaunchBuild() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		submissionsPath.resolve("perfect").resolve("makefile")
		.toFile().delete();  // use default Java launch

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.class")
			.toFile().exists());

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
		assertEquals("100\n", total);

		assertThat (suite.isTagActive("t2_OK"), is(true));
		assertThat (suite.isTagActive("t2_passed"), is(true));
		assertThat (suite.isTagActive("t2_failed"), is(false));

	}

	@Test
	void testLateSubmission() {
		TestSuite suite = new TestSuite(asst);

		Path timeFile = submissionPath.resolve("perfect.time");
		String timeFileStr = timeFile.toAbsolutePath().toString();
		FileUtils.writeTextFile(timeFile, "2022-12-16"); // one day late

		suite.setSubmissionDateIn(timeFileStr); 
		suite.clearTheStage(stagingPath);

		submissionsPath.resolve("perfect").resolve("makefile")
			.toFile().delete();  // use default Java launch

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
		assertEquals("90\n", total); // 10% penalty
	}

	@Test
	void testBarelyLateSubmission() {
		TestSuite suite = new TestSuite(asst);
		Path timeFile = submissionPath.resolve("perfect.time");
		String timeFileStr = timeFile.toAbsolutePath().toString();
		FileUtils.writeTextFile(timeFile, 
			"2022-12-16  00:00:00"); // one second late

		suite.setSubmissionDateIn(timeFileStr); 

		suite.clearTheStage(stagingPath);

		submissionsPath.resolve(studentName).resolve("makefile")
		.toFile().delete();  // use default Java launch

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
		assertEquals("90\n", total); // 10% penalty
	}

	@Test
	void testLaterSubmission() {
		TestSuite suite = new TestSuite(asst);
		Path timeFile = submissionPath.resolve("perfect.time");
		String timeFileStr = timeFile.toAbsolutePath().toString();
		FileUtils.writeTextFile(timeFile, 
			"2022-12-20"); // fave days late

		suite.setSubmissionDateIn(timeFileStr); 

		suite.clearTheStage(stagingPath);

		submissionsPath.resolve("perfect").resolve("makefile")
		.toFile().delete();  // use default Java launch

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
		assertEquals("0\n", total); // 100% penalty
	}



	@Test
	void testInPlaceProcessing() {
		asst.setTestSuiteDirectory(testSuitePath);
		Path inPlacePath = asstDestPath.resolve("submissions")
			.resolve("perfect");
		asst.setSubmissionsDirectory(inPlacePath);
		asst.setInPlace(true);
		asst.setRecordingDirectory(testSuitePath);

		TestSuite suite = new TestSuite(asst);

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran in place.
		assertTrue(inPlacePath.resolve("sqrtProg.class")
			.toFile().exists());

		// Were reports generated in place?
		String studentName = System.getProperty("user.name");
		assertTrue(asst.getTestSuiteDirectory()
				.resolve("testsSummary.csv")
				.toFile().exists());
		assertTrue(asst.getTestSuiteDirectory()
			.resolve(studentName + ".html")
			.toFile().exists());
		Path totalFile = asst.getTestSuiteDirectory()
			.resolve(studentName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("100\n", total);

		// In-place processing should not leave hash files.
		Path hashFile =  asst.getTestSuiteDirectory()
			.resolve(studentName + ".hash");
		assertFalse(hashFile.toFile().exists());
		
	}

	@Test
	void testInPlaceProcessingRecording() {
		asst.setTestSuiteDirectory(testSuitePath);
		Path inPlacePath = asstDestPath.resolve("submissions")
			.resolve("perfect");
		asst.setSubmissionsDirectory(inPlacePath);
		Path recording = asstDestPath.resolve("recording");
		asst.setRecordingDirectory(recording);
		asst.setInPlace(true);

		TestSuite suite = new TestSuite(asst);

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran in place.
		assertTrue(inPlacePath.resolve("sqrtProg.class")
			.toFile().exists());

		// Were reports generated in place?
		String studentName = System.getProperty("user.name");
		assertTrue(recording.resolve("testsSummary.csv")
				.toFile().exists());
		assertTrue(recording.resolve(studentName + ".html")
			.toFile().exists());
		Path totalFile = recording.resolve(studentName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("100\n", total);

		// In-place processing should not leave hash files.
		Path hashFile =  asst.getTestSuiteDirectory()
			.resolve(studentName + ".hash");
		assertFalse(hashFile.toFile().exists());
		
	}


	@Test
	void testOnFail() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		String submitterName = "bad";

		Path submissionPath = submissionsPath.resolve(submitterName);

		submissionsPath.resolve(submitterName).resolve("makefile")
		.toFile().delete();  // use default Java launch

		submission = new Submission(asst, submitterName, submissionPath);

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.class")
			.toFile().exists());

		assertThat (suite.isTagActive("t2_OK"), is(false));
		assertThat (suite.isTagActive("t2_failed"), is(true));

		// Were reports generated?
		assertTrue(submission.getRecordingDir()
			.resolve("testsSummary.csv")
			.toFile().exists());
		assertTrue(submission.getRecordingDir()
			.resolve(submitterName + ".html")
			.toFile().exists());
		Path totalFile = submission.getRecordingDir()
			.resolve(submitterName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("25\n", total);
	}

	@Test
	void testFailIf() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		String submitterName = "imprecise";

		Path submissionPath = submissionsPath.resolve(submitterName);

		submissionsPath.resolve(submitterName).resolve("makefile")
		.toFile().delete();  // use default Java launch

		submission = new Submission(asst, submitterName, submissionPath);

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.class")
			.toFile().exists());

		assertThat (suite.isTagActive("t10000_failed"), is(true));
		assertThat (suite.isTagActive("t2_failed"), is(true));

		// Were reports generated?
		assertTrue(submission.getRecordingDir()
			.resolve("testsSummary.csv")
			.toFile().exists());
		assertTrue(submission.getRecordingDir()
			.resolve(submitterName + ".html")
			.toFile().exists());
		Path totalFile = submission.getRecordingDir()
			.resolve(submitterName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("69\n", total);
	}


}


