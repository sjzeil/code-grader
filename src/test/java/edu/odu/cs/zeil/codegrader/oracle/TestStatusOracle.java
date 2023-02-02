package edu.odu.cs.zeil.codegrader.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestCaseProperties;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;
import edu.odu.cs.zeil.codegrader.TestSuiteProperties;



public class TestStatusOracle {

	//CHECKSTYLE:OFF

    private Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	private Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	private Path stagingPath = Paths.get("build", "test-data", "assignment2",
		 "stage");
	private Path submissionsPath = Paths.get("build", "test-data", 
		"assignment2", "submissions");
	private Path recordingPath = Paths.get("build", "test-data",
		"assignment2", "grades");
	private Path submissionPath = submissionsPath.resolve("student1");


	private Submission submission;
    private TestCaseProperties testProperties;
    private TestCase testCase;
	

	private Assignment asst;

	@BeforeEach
	public void setup() throws IOException, TestConfigurationError {
        Path testData = Paths.get("build", "test-data");
        if (testData.toFile().exists()) {
            FileUtils.deleteDirectory(testData);
        }

		testSuitePath.toFile().getParentFile().mkdirs();
		stagingPath.toFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, null, null,
			StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		asst.setStagingDirectory(stagingPath);
		asst.setSubmissionsDirectory(submissionsPath);
		asst.setRecordingDirectory(recordingPath);

		submission = new Submission(asst, "student1", submissionPath);

		testProperties = new TestCaseProperties(asst, "params");
		testCase = new TestCase(testProperties);
	}

	@Test
	void testNoCompile() 
	throws TestConfigurationError, IOException  {

		FileUtils.writeTextFile(testSuitePath.resolve("tests")
				.resolve("params").resolve("test.params"), "foo");
		
		testProperties = new TestCaseProperties(asst, "params");
		testCase = new TestCase(testProperties);

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = "make";
		testProperties.setLaunch(launcher);
        
		Stage stage = new Stage(asst, submission,
			new TestSuiteProperties());
		stage.getStageDir().toFile().mkdirs();
		
		Path recordAt = asst.getRecordingDirectory().resolve("student1")
			.resolve("TestCases").resolve("params");
		FileUtils.copyDirectory(asst.getTestSuiteDirectory().resolve("params"),
			recordAt, null, null);


		testCase.performTest(submission, false, stage, 0);
        String expected = "";
        String actual = testCase.getOutput() + "\n" + testCase.getErr();
        Oracle oracle = new StatusOracle(
            new OracleProperties(), testCase, submission, stage);
        OracleResult result = oracle.compare(expected, actual);
        assertThat(result.score, equalTo(0));
	}

	@Test
	void testPassed() 
	throws TestConfigurationError, IOException  {

		FileUtils.writeTextFile(testSuitePath.resolve("tests")
				.resolve("params").resolve("test.params"), "foo");
		
		testProperties = new TestCaseProperties(asst, "params");
		testCase = new TestCase(testProperties);

		String launcher = "echo";
		testProperties.setLaunch(launcher);
        
		Stage stage = new Stage(asst, submission,
			new TestSuiteProperties());
		stage.getStageDir().toFile().mkdirs();
		
		Path recordAt = asst.getRecordingDirectory().resolve("student1")
			.resolve("TestCases").resolve("params");
		FileUtils.copyDirectory(asst.getTestSuiteDirectory().resolve("params"),
			recordAt, null, null);


		testCase.performTest(submission, false, stage, 0);
        String expected = "";
        String actual = testCase.getOutput() + "\n" + testCase.getErr();
        Oracle oracle = new StatusOracle(
            new OracleProperties(), testCase, submission, stage);
        OracleResult result = oracle.compare(expected, actual);
        assertThat(result.score, equalTo(OracleProperties.DEFAULT_POINT_CAP));
	}
	

}


