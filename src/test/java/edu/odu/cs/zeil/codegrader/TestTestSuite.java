package edu.odu.cs.zeil.codegrader;

import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class TestTestSuite {


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

		String template = "gradeTemplate.xlsx";
		Path gradingTemplate = Paths.get("src", "main", "resources",
			"edu", "odu", "cs", "zeil", "codegrader", template);
		Path binDir = Paths.get("bin", "main",
			"edu", "odu", "cs", "zeil", "codegrader");
		Path buildDir = Paths.get("build", "classes", "java", "main",
			"edu", "odu", "cs", "zeil", "codegrader");
		if (binDir.toFile().exists()) {
			Files.copy(gradingTemplate, binDir.resolve(template), 
				StandardCopyOption.REPLACE_EXISTING);
		}
		if (buildDir.toFile().exists()) {
			Files.copy(gradingTemplate, buildDir.resolve(template), 
				StandardCopyOption.REPLACE_EXISTING);
		}
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
				.resolve(studentName + ".xlsx")
				.toFile().exists());
		assertTrue(submitter.getRecordingDir()
				.resolve("testInfo.csv")
				.toFile().exists());
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
	void testInPlaceProcessing() {
		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath);
		Path inPlacePath = asstDestPath.resolve("submissions")
			.resolve("perfect");
		asst.setSubmissionsDirectory(inPlacePath);
		asst.setInPlace(true);

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

		// Now check if the build ran in place.
		assertTrue(inPlacePath.resolve("sqrtProg.class")
			.toFile().exists());

		// Were reports generated in place?
		String studentName = "perfect";
		assertTrue(asst.getTestSuiteDirectory()
				.resolve("testInfo.csv")
				.toFile().exists());
				assertTrue(asst.getTestSuiteDirectory()
				.resolve("testsSummary.csv")
				.toFile().exists());
		assertTrue(asst.getTestSuiteDirectory()
				.resolve(studentName + "assignment.xlsx")
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


