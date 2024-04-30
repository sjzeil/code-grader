package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
import edu.odu.cs.zeil.codegrader.samples.ParamLister;
import edu.odu.cs.zeil.codegrader.samples.ParamWriter;

//CHECKSTYLE:OFF

public class TestExternalOracle {

	private  Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	private Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	
	private Assignment asst;
	private TestCase testCase;
	private Submission sub;
	private Stage stage;


	//private static final int OK = OracleProperties.DEFAULT_POINT_CAP;
	
	@BeforeEach
	private void setup() throws IOException, TestConfigurationError {
        Path testData = Paths.get("build", "test-data");
        if (testData.toFile().exists()) {
            FileUtils.deleteDirectory(testData);
        }

		testSuitePath.toFile().getParentFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, null, null,
			StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		asst.setRecordingDirectory(testSuitePath.resolve("grading"));
		asst.setStagingDirectory(testSuitePath.resolve("stage"));

		testCase = new TestCase(new TestCaseProperties(asst, "params"));
		sub = new Submission(asst, "student1", 
            testSuitePath.resolve("submissions"));
		stage = new Stage(asst, sub, new TestSuiteProperties());
		sub.getRecordingDir().toFile().mkdirs();
	}
	

	@Test
	void testSuccess() throws FileNotFoundException {
        OracleProperties properties = new OracleProperties();
        properties.command = ParamLister.launcher + " 100";
		Oracle oracle = new ExternalOracle(properties, testCase, sub, stage);

        String expected = "foo\n";
        String observed = "foo\n";

		OracleResult result = oracle.compare(expected, observed);

        assertThat(result.score, equalTo(OracleProperties.DEFAULT_POINT_CAP));
	}

	@Test
	void testScoreFile() throws FileNotFoundException {
        OracleProperties properties = new OracleProperties();
        properties.command = ParamWriter.launcher + " 42 test.score";
		//String oracleCommand = "echo 42 > test.score\necho foobar\n";
		Oracle oracle = new ExternalOracle(properties, testCase, sub, stage);

        String expected = "foo\n";
        String observed = "bar\n";

		OracleResult result = oracle.compare(expected, observed);

        assertThat(result.score, is(42));

	}

    @Test
	void testFail1() throws FileNotFoundException {
        OracleProperties properties = new OracleProperties();
        properties.command = ParamLister.launcher + " fail";

		Oracle oracle = new ExternalOracle(properties, testCase, sub, stage);

        String expected = "foo\n";
        String observed = "bar\n";

		OracleResult result = oracle.compare(expected, observed);

        assertThat(result.score, is(0));
        assertThat(result.message, not(is("")));
	}

    

}
