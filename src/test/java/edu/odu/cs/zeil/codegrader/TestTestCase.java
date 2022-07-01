package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestTestCase {
	
	public Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	public Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	
	@BeforeEach
	public void setup() throws IOException {
		testSuitePath.toFile().getParentFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(testSuitePath);
	}
	

	@Test
	void testTestCaseConstructor() throws FileNotFoundException  {
        Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
        TestProperties testProperties = new TestProperties(asst, "params");
        Submission submission = new Submission (asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		assertThat (testCase.getOutput(), is(""));
		assertThat (testCase.getErr(), is(""));
		assertThat (testCase.timedOut(), is(false));
		assertThat (testCase.crashed(), is(false));
	}

	@Test
	void testParamsTestCase() throws FileNotFoundException  {
        Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
        TestProperties testProperties = new TestProperties(asst, "params");

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " + System.getProperty("java.class.path") + " edu.odu.cs.zeil.codegrader.samples.ParamLister";
		System.err.println(launcher);
		testProperties.setLaunch (launcher);
        Submission submission = new Submission (asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		testCase.runTest(submission);
		assertThat (testCase.crashed(), is(false));
		assertThat (testCase.timedOut(), is(false));
		assertThat (testCase.getOutput(), is("a\nb\nc\n"));
		assertThat (testCase.getErr(), is("3\n"));
	}

}
