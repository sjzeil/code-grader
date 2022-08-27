package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestTestCaseExec {
	
	private Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	private Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	private Path stagingPath = Paths.get("build", "test-data",
		"assignment2", "stage");
	private Path recordingPath = Paths.get("build", "test-data",
		"assignment2", "recording");


		
	private Assignment asst;
	private Submission student1;
	private Stage stage;
	
	/**
	 * Set up the test data in build/.
	 * @throws IOException
	 */
	@BeforeEach
	public void setup() throws IOException {
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
		TestSuitePropertiesBase tcProps = new TestSuitePropertiesBase();
		student1 = new Submission(asst, "student1");
		stage = new Stage(asst, student1, tcProps);
		stage.getStageDir().toFile().mkdirs();
		asst.setRecordingDirectory(recordingPath);
		recordingPath.toFile().mkdirs();
	}
	
	

	@Test
	void testTestCaseConstructor() throws FileNotFoundException, 
			TestConfigurationError  {
        TestCaseProperties testProperties = 
			new TestCaseProperties(asst, "params");
        TestCase testCase = new TestCase(testProperties);
		assertThat(testCase.getOutput(), is(""));
		assertThat(testCase.getErr(), is(""));
		assertThat(testCase.timedOut(), is(false));
		assertThat(testCase.crashed(), is(false));
	}

	@Test
	void testParamsTestCase() 
		throws FileNotFoundException, TestConfigurationError  {
        TestCaseProperties testProperties = 
			new TestCaseProperties(asst, "params");

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
			+ System.getProperty("java.class.path") 
			+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";
		//System.err.println(launcher);
		testProperties.setLaunch(launcher);
        Submission submission = new Submission(asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		testCase.executeTest(submission, stage, 0);
		assertThat(testCase.crashed(), is(false));
		assertThat(testCase.timedOut(), is(false));
		assertThat(testCase.getOutput(), is("a\nb\nc\n"));
		assertThat(testCase.getErr(), is("3\n"));
	}

	@Test
	void testStdInTestCase() 
		throws FileNotFoundException, TestConfigurationError  {
        TestCaseProperties testProperties = new TestCaseProperties(
				asst, "stdin");

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
			+ System.getProperty("java.class.path") 
			+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";
		// System.err.println(launcher);
		testProperties.setLaunch(launcher);
        Submission submission = new Submission(asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		testCase.executeTest(submission, stage, 0);
		assertThat(testCase.crashed(), is(false));
		assertThat(testCase.timedOut(), is(false));
		assertThat(testCase.getOutput(), is("Hello world!\nHow are\nyou?\n"));
		assertThat(testCase.getErr(), is("0\n"));
	}


	@Test
	void testSoftCrashCase() 
		throws FileNotFoundException, TestConfigurationError  {
        TestCaseProperties testProperties = 
			new TestCaseProperties(asst, "softCrash");

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp "
			+ System.getProperty("java.class.path") 
			+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";
		// System.err.println(launcher);
		testProperties.setLaunch(launcher);
        Submission submission = new Submission(asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		testCase.executeTest(submission, stage, 0);
		assertThat(testCase.crashed(), is(true));
		assertThat(testCase.timedOut(), is(false));
	}

	@Test
	void testTimeOut() throws FileNotFoundException, TestConfigurationError  {
        TestCaseProperties testProperties 
			= new TestCaseProperties(asst, "softCrash");

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
			+ System.getProperty("java.class.path") 
			+ " edu.odu.cs.zeil.codegrader.samples.SlowProgram";
		// System.err.println(launcher);
		testProperties.setLaunch(launcher);
        Submission submission = new Submission(asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		testCase.executeTest(submission, stage, 0);
		assertThat(testCase.crashed(), is(false));
		assertThat(testCase.timedOut(), is(true));
	}

	@Test
	void testLargeOutputTestCase() throws IOException, TestConfigurationError  {
        TestCaseProperties testProperties = new TestCaseProperties(
			asst, "params");

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
			+ System.getProperty("java.class.path") 
			+ " edu.odu.cs.zeil.codegrader.samples.LargeOutput";
		// System.err.println(launcher);
		testProperties.setLaunch(launcher);
        Submission submission = new Submission(asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		testCase.executeTest(submission, stage, 0);
		assertThat(testCase.crashed(), is(false));
		assertThat(testCase.timedOut(), is(false));
		assertThat(testCase.getErr(), is(""));
		String actualOutput = testCase.getOutput();
		BufferedReader actual = new BufferedReader(
				new StringReader(actualOutput));
		String actualLine = actual.readLine();
		for (int i = 0; 
			i < edu.odu.cs.zeil.codegrader.samples.LargeOutput.OutputSize / 10; 
			++i) {
			assertThat(actualLine, is("abcdefghi"));
			actualLine = actual.readLine();
		}
		assertNull(actualLine);
	}


	@Test
	void testTestCaseContext() throws IOException, TestConfigurationError  {
        TestCaseProperties testProperties = new TestCaseProperties(
			asst, "params");

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
			+ System.getProperty("java.class.path") 
			+ " edu.odu.cs.zeil.codegrader.samples.CWDLister";
		//System.err.println(launcher);
		testProperties.setLaunch(launcher);
        Submission submission = new Submission(asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		testCase.executeTest(submission, stage, 0);
		assertThat(testCase.crashed(), is(false));
		assertThat(testCase.timedOut(), is(false));
		assertThat(testCase.getErr(), is(""));
		Path execCWD = Paths.get(testCase.getOutput().trim());
		assertThat(execCWD.toRealPath(), is(stage.getStageDir().toRealPath()));
	}

	/* Move to to TestStage? 
	@Test
	void testParamSubstitutionTestCase() 
		throws IOException, TestConfigurationError  {
        TestCaseProperties testProperties 
			= new TestCaseProperties(asst, "paramsSub");

		String javaHome = System.getProperty("java.home");
		Path javaExec = Paths.get(javaHome, "bin", "java");
		String launcher = javaExec + " -cp " 
			+ System.getProperty("java.class.path") 
		   	+ " edu.odu.cs.zeil.codegrader.samples.ParamLister";
		//System.err.println(launcher);
		testProperties.setLaunch(launcher);
        Submission submission = new Submission(asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		testCase.executeTest(submission, stage, 0);
		assertThat(testCase.crashed(), is(false));
		assertThat(testCase.timedOut(), is(false));
		assertThat(testCase.getErr(), is("4\n"));
		String[] lines = testCase.getOutput().trim().split("\n");
		assertThat(Paths.get(lines[0]).toRealPath(),
			is(asst.getStagingDirectory().toRealPath()));
		assertThat(Paths.get(lines[1]).toRealPath(),
			is(asst.getTestSuiteDirectory().toRealPath()));
		assertThat(lines[2], is("paramsSub"));
		assertThat(lines[3], is("@treble"));
	}
*/

}
