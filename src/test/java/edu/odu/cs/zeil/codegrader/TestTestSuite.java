package edu.odu.cs.zeil.codegrader;

import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.io.File;

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

	/**
	 * Clean up test data.
	 * 
	 * @throws IOException
	 */
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(Paths.get("build", "test-data"));
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
	void testRunAllTests() 
			throws TestConfigurationError, IOException  {

		String studentName = "perfect";
		Submission student1 = new Submission(asst, studentName);
		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
				+ System.getProperty("java.class.path")
				+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";

		TestSuite suite = new TestSuite(asst);
		suite.setLaunch(launcher);
		

		Path studentGrades = recordingPath.resolve(studentName)
			.resolve("Grading");
		if (studentGrades.toFile().exists()) {
			FileUtils.deleteDirectory(studentGrades);
		}
		FileUtils.copyDirectory(asst.getTestSuiteDirectory(), 
			studentGrades, null, null);

		suite.runTests(student1, 0);

		File[] tests = asst.getTestSuiteDirectory().toFile().listFiles();
		for (File testDir: tests) {
			if (!testDir.isDirectory()) {
				continue;
			}
			String testName = testDir.getName();

			Path recordedTest = studentGrades.resolve(testName);
			assertTrue(Files.exists(recordedTest));
			Path recordedScore = recordedTest.resolve(testName + ".score");
			assertTrue(Files.exists(recordedScore));
		}

	}

	@Test
	void testRunSelectedTest()
	throws FileNotFoundException, TestConfigurationError  {

		Submission student1 = new Submission(asst, "perfect");
		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
				+ System.getProperty("java.class.path")
				+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";

		TestSuite suite = new TestSuite(asst);
		suite.setLaunch(launcher);
		String[] selections = {"params"};
		suite.setSelectedTests(Arrays.asList(selections));

		suite.runTests(student1, 0);

		Path studentGrades = recordingPath.resolve("perfect");

		File[] tests = asst.getTestSuiteDirectory().toFile().listFiles();
		for (File testDir: tests) {
			String testName = testDir.getName();
			if (testName.equals("params")) {
				Path recordedTest = studentGrades.resolve(testName);
				assertTrue(Files.exists(recordedTest));
				Path recordedScore = recordedTest.resolve(testName + ".score");
				assertTrue(Files.exists(recordedScore));
			} else {
				Path recordedTest = asst.getRecordingDirectory()
					.resolve(testName);
				assertFalse(Files.exists(recordedTest));
			}
		}
	}



}


