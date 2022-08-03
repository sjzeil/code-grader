package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
		"java-sqrt-assignment");

	private Path asstDestPath = Paths.get("build", "test-data", 
		"java-sqrt-assignment");

	private Path testSuitePath = asstDestPath.resolve("Tests");
	private Path stagingPath = Paths.get("build", "test-data", "stage");
	private Path submissionsPath = asstDestPath.resolve("submissions");
	private Path recordingPath = asstDestPath.resolve("Grades");
	private Path goldPath = asstDestPath.resolve("Gold");

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
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		asst.setStagingDirectory(stagingPath);
		asst.setSubmissionsDirectory(submissionsPath);
		asst.setRecordingDirectory(recordingPath);
		asst.setGoldDirectory(goldPath);

	}

	/**
	 * Clean up test data.
	 * 
	 * @throws IOException
	 */
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(testSuitePath);
	}

	@Test
	void testGoldBuild() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		suite.buildGoldVersionIfAvailable();

		assertTrue(asst.getGoldStage().toFile().exists());
		assertTrue(asst.getGoldStage().resolve("sqrtProg.java")
			.toFile().exists());
		assertTrue(asst.getGoldStage().resolve("sqrtProg.class")
			.toFile().exists());

	}

	@Test
	void testRunAllTests() 
			throws FileNotFoundException, TestConfigurationError  {

		Submission student1 = new Submission(asst, "student1");
		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
				+ System.getProperty("java.class.path")
				+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";

		TestSuite suite = new TestSuite(asst);
		suite.setLaunch(launcher);

		suite.runTests(student1);

		Path studentGrades = recordingPath.resolve("student1");

		File[] tests = asst.getTestSuiteDirectory().toFile().listFiles();
		for (File testDir: tests) {
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

		Submission student1 = new Submission(asst, "student1");
		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
				+ System.getProperty("java.class.path")
				+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";

		TestSuite suite = new TestSuite(asst);
		suite.setLaunch(launcher);
		String[] selections = {"params"};
		suite.setSelectedTests(Arrays.asList(selections));

		suite.runTests(student1);

		Path studentGrades = recordingPath.resolve("student1");

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


