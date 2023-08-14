package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;

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

public class TestSelfScoredOracle {

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
    void testSelfOracle1() throws FileNotFoundException {
        OracleProperties props = new OracleProperties();
        props.pattern = "(?<pts>[0-9]+(?:[.][0-9]+))%";
        Oracle oracle = new SelfScoredOracle(props, testCase, sub, stage);

        String observed = 
            "1..8\n" +
            "ok 1 - EncyclopediaAddCourse\n" +
            "ok 2 - EncyclopediaAssign\n" +
            "not ok 3 - EncyclopediaConstructor\n" +
            "ok 4 - EncyclopediaCopy\n" +
            "ok 5 - EncyclopediaRead\n" +
            "not ok 6 - EncyclopediaRemoveCourse\n" +
            "ok 7 - ResearchPlanAddPrior\n" +
            "ok 8 - ResearchPlanAddLater\n" +
        "# UnitTest: passed 6 out of 8 tests,\n# for a success rate of 75.0%\n"
            ;

        OracleResult result = oracle.compare("", observed);
        assertThat(result.score, equalTo(75));
        assertThat(result.message, containsString("75.0%"));

    }

    @Test
    void testSelfOracle2() throws FileNotFoundException {
        OracleProperties props = new OracleProperties();
        props.pattern = "(?<pts>[0-9]+) out of (?<poss>[0-9]+)";
        Oracle oracle = new SelfScoredOracle(props, testCase, sub, stage);

        String observed = 
            "1..8\n" +
            "ok 1 - EncyclopediaAddCourse\n" +
            "ok 2 - EncyclopediaAssign\n" +
            "not ok 3 - EncyclopediaConstructor\n" +
            "ok 4 - EncyclopediaCopy\n" +
            "ok 5 - EncyclopediaRead\n" +
            "not ok 6 - EncyclopediaRemoveCourse\n" +
            "ok 7 - ResearchPlanAddPrior\n" +
            "ok 8 - ResearchPlanAddLater\n" +
        "# UnitTest: passed 6 out of 8 tests,\n# for a success rate of 75.0%\n"
            ;

        OracleResult result = oracle.compare("", observed);
        assertThat(result.score, equalTo(75));
        assertThat(result.message, containsString("6 out of 8"));
        assertThat(result.message, containsString("ok 1"));
    }

}
