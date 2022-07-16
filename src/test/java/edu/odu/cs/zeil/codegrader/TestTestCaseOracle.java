package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class TestTestCaseOracle {
	

	private Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	private Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	private Path stagingPath = Paths.get("build", "test-data", "assignment2",
		 "stage");
	private Path submissionsPath = Paths.get("build", "test-data", 
		"assignment2", "submissions");
	private Path recordingPath = Paths.get("build", "test-data",
		"assignment2", "grades");

	private Assignment asst;
	private TestCase testCase;
	private TestCaseProperties testProperties;
	
	/**
	 * Set up assignment2 params test.
	 * @throws IOException
	 * @throws TestConfigurationError
	 */
	@BeforeEach
	public void setup() throws IOException, TestConfigurationError {
		testSuitePath.toFile().getParentFile().mkdirs();
		stagingPath.toFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath,
			StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		asst.setStagingDirectory(stagingPath);
		asst.setSubmissionsDirectory(submissionsPath);

		testProperties = new TestCaseProperties(asst, "params");
		testCase = new TestCase(testProperties);
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
	void testPerformTest() throws FileNotFoundException, TestConfigurationError  {

		Submission student1 = new Submission(asst, "student1");
		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
			+ System.getProperty("java.class.path")
			+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";
		testProperties.setLaunch(launcher);
        

		Path recordedAt = testCase.performTest(student1);

		Path studentGrades = recordingPath.resolve("student1");
		assertTrue(studentGrades.toFile().exists());
		assertTrue(recordedAt.normalize().equals(studentGrades.normalize()));
		Path studentTestResults = studentGrades.resolve("params");
		assertTrue(studentTestResults.toFile().exists());
		Path studentScoreFile = studentTestResults.resolve("test.score");
		assertTrue(studentScoreFile.toFile().exists());
		String scoreContents = FileUtils.readTextFile(
				studentScoreFile.toFile());
		scoreContents = scoreContents.trim();
		assertThat(scoreContents, is("100"));
	}

}
