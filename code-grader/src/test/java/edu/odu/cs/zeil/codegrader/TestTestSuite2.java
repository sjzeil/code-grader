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



public class TestTestSuite2 {

	//CHECKSTYLE:OFF

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




}


