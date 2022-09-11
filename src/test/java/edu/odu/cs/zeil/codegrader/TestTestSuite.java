package edu.odu.cs.zeil.codegrader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
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
	}


	@Test
	void testIncrementalProcessing() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Submission submission = new Submission(asst, "perfect");
		
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
	void testDateParsing() {
		LocalDateTime dt0 = LocalDateTime.of(2022, 12, 15, 23, 59, 59);
		LocalDateTime dt1 = LocalDateTime.of(2022, 12, 15, 20, 58, 16);

		TestSuite suite = new TestSuite(asst);

		assertThat(suite.parseDateTime("2022-12-15"), is(dt0));
		assertThat(suite.parseDateTime("12/15/2022"), is(dt0));
		assertThat(suite.parseDateTime("2022-12-15_20:58:16"), is(dt1));
		assertThat(suite.parseDateTime("2022-12-15T20:58:16"), is(dt1));
		assertThat(suite.parseDateTime("12/15/2022 20:58:16"), is(dt1));
	}

	@Test
	void testDaysLate() {
		TestSuite suite = new TestSuite(asst);
		Submission submission = new Submission(asst, "perfect");

		assertThat(suite.computeDaysLate(submission), is(0));

		suite.setDueDate("2022-12-15");
		asst.setDateCommand("echo 2022-09-09_20:58:16");
		assertThat(suite.computeDaysLate(submission), is(0));

		asst.setDateCommand("echo 12/15/2022 23:59:16");
		assertThat(suite.computeDaysLate(submission), is(0));

		asst.setDateCommand("echo 12/17/2022 23:59:16");
		assertThat(suite.computeDaysLate(submission), is(2));
	}

	@Test
	void testJavaDefaultLaunchBuild() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Submission submission = new Submission(asst, "perfect");
		
		submissionsPath.resolve("perfect").resolve("makefile")
		.toFile().delete();  // use default Java launch

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage().toFile().exists());
		assertTrue(asst.getSubmitterStage().resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getSubmitterStage().resolve("sqrtProg.class")
			.toFile().exists());

					// Were reports generated?
		String studentName = "perfect";
		Submission submitter = new Submission(asst, studentName);
		assertTrue(submitter.getRecordingDir()
			.resolve("testsSummary.csv")
			.toFile().exists());
		assertTrue(submitter.getRecordingDir()
			.resolve(studentName + ".html")
			.toFile().exists());
		Path totalFile = submitter.getRecordingDir()
			.resolve(studentName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("100\n", total);
	}

	@Test
	void testLateSubmission() {
		TestSuite suite = new TestSuite(asst);
		asst.setDateCommand("echo 2022-12-16"); // one day late
		suite.clearTheStage(stagingPath);

		Submission submission = new Submission(asst, "perfect");
		
		submissionsPath.resolve("perfect").resolve("makefile")
		.toFile().delete();  // use default Java launch

		suite.processThisSubmission(submission);
					// Were reports generated?

					String studentName = "perfect";
		Submission submitter = new Submission(asst, studentName);
		assertTrue(submitter.getRecordingDir()
			.resolve("testsSummary.csv")
			.toFile().exists());
		assertTrue(submitter.getRecordingDir()
			.resolve(studentName + ".html")
			.toFile().exists());
		Path totalFile = submitter.getRecordingDir()
			.resolve(studentName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("90\n", total); // 10% penalty
	}

	@Test
	void testBarelyLateSubmission() {
		TestSuite suite = new TestSuite(asst);
		asst.setDateCommand("echo 2022-12-16 00:00:00"); // one second late
		suite.clearTheStage(stagingPath);

		Submission submission = new Submission(asst, "perfect");
		
		submissionsPath.resolve("perfect").resolve("makefile")
		.toFile().delete();  // use default Java launch

		suite.processThisSubmission(submission);
					// Were reports generated?

					String studentName = "perfect";
		Submission submitter = new Submission(asst, studentName);
		assertTrue(submitter.getRecordingDir()
			.resolve("testsSummary.csv")
			.toFile().exists());
		assertTrue(submitter.getRecordingDir()
			.resolve(studentName + ".html")
			.toFile().exists());
		Path totalFile = submitter.getRecordingDir()
			.resolve(studentName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("90\n", total); // 10% penalty
	}

	@Test
	void testLaterSubmission() {
		TestSuite suite = new TestSuite(asst);
		asst.setDateCommand("echo 2022-12-20"); // five days late
		
		suite.clearTheStage(stagingPath);

		Submission submission = new Submission(asst, "perfect");
		
		submissionsPath.resolve("perfect").resolve("makefile")
		.toFile().delete();  // use default Java launch

		suite.processThisSubmission(submission);
					// Were reports generated?

		String studentName = "perfect";
		Submission submitter = new Submission(asst, studentName);
		assertTrue(submitter.getRecordingDir()
			.resolve("testsSummary.csv")
			.toFile().exists());
		assertTrue(submitter.getRecordingDir()
			.resolve(studentName + ".html")
			.toFile().exists());
		Path totalFile = submitter.getRecordingDir()
			.resolve(studentName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("0\n", total); // 100% penalty
	}



	@Test
	void testInPlaceProcessing() {
		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath);
		Path inPlacePath = asstDestPath.resolve("submissions")
			.resolve("perfect");
		asst.setSubmissionsDirectory(inPlacePath);
		asst.setInPlace(true);

		TestSuite suite = new TestSuite(asst);

		Submission submission = new Submission(asst, "perfect");

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage().toFile().exists());
		assertTrue(asst.getSubmitterStage().resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran in place.
		assertTrue(inPlacePath.resolve("sqrtProg.class")
			.toFile().exists());

		// Were reports generated in place?
		String studentName = "perfect";
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
	}





}


