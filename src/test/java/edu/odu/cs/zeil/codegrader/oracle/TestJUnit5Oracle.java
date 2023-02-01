package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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

//CHECKSTYLE:OFF

public class TestJUnit5Oracle {

	private  Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	private Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	private Submission sub;
	
	private Assignment asst;
	private TestCase testCase;

	private Stage stage;
	
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
		asst.setStagingDirectory(testSuitePath.resolve("stage"));

		testCase = new TestCase(new TestCaseProperties(asst, "params"));
		sub = new Submission(asst, "student1", 
            testSuitePath.resolve("submissions"));
		stage = new Stage(asst, sub, new TestSuiteProperties());
	}
	
    @Test
    void testJU5Oracle() throws FileNotFoundException {
        Oracle oracle = new JUnit5Oracle(new OracleProperties(), testCase,
                sub, stage);

        String observed = "Test run finished after 64 ms\n" +
                "[         3 containers found      ]\n" +
                "[         0 containers skipped    ]\n" +
                "[         3 containers started    ]\n" +
                "[         0 containers aborted    ]\n" +
                "[         3 containers successful ]\n" +
                "[         0 containers failed     ]\n" +
                "[        10 tests found           ]\n" +
                "[         0 tests skipped         ]\n" +
                "[        10 tests started         ]\n" +
                "[         0 tests aborted         ]\n" +
                "[        6 tests successful      ]\n" +
                "[         4 tests failed          ]\n";

        OracleResult result = oracle.compare("", observed);
        assertThat(result.score, equalTo(60));
        assertThat(result.message, equalTo(observed));
    }

}
