package edu.odu.cs.zeil.codegrader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class TestTestSuiteReporting {

	//CHECKSTYLE:OFF

	private Path asstSrcPath = Paths.get("src", "test", "data", 
		"java-noGold-assignment");

	private Path asstDestPath = Paths.get("build", "test-data", 
		"java-noGold-assignment");

	private Path testSuitePath = asstDestPath.resolve("Tests");
	private Path stagingPath = Paths.get("build", "test-data", "stage");
	private Path submissionsPath = asstDestPath.resolve("submissions");
	private Path recordingPath = asstDestPath.resolve("Grades");
	private String studentName = "misspelling";
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
	void testErrorReporting() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		submissionsPath.resolve("misspelling").resolve("makefile")
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
		assertTrue(submission.getRecordingDir()
			.resolve(studentName + ".txt")
			.toFile().exists());
		Path totalFile = submission.getRecordingDir()
			.resolve(studentName + ".total");
		assertTrue(totalFile.toFile().exists());
		String total = FileUtils.readTextFile(totalFile.toFile());
		assertEquals("63\n", total);

        Path messageFile = recordingPath.resolve(studentName)
            .resolve("TestCases").resolve("t10000").resolve("t10000.message");
        String message = FileUtils.readTextFile(messageFile.toFile());
        assertThat (message, containsString("suqare"));
	}


}


