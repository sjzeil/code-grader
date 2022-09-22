package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestCaseProperties;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;
import edu.odu.cs.zeil.codegrader.TestSuitePropertiesBase;


public class TestOracle {

	private Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	private Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	
	private Assignment asst;
	private TestCase testCase;
    private Submission sub;
    private Stage stage;
	
    /**
     * .
     * @throws IOException
     * @throws TestConfigurationError
     */
	@BeforeEach
	public void setup() throws IOException, TestConfigurationError {
        Path testData = Paths.get("build", "test-data");
        if (testData.toFile().exists()) {
            FileUtils.deleteDirectory(testData);
        }

        testSuitePath.toFile().getParentFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, null, null, 
            StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
        asst.setRecordingDirectory(testSuitePath.resolve("grades"));
        asst.setStagingDirectory(testSuitePath.resolve("stage"));

		testCase = new TestCase(new TestCaseProperties(asst, "params"));
        sub = new Submission(asst, "student1", 
            testSuitePath.resolve("submissions"));
        stage = new Stage(asst, sub, new TestSuitePropertiesBase());
	}
	
	

	@Test
	void testDefaults() throws FileNotFoundException {
		Oracle oracle = OracleFactory.getOracle(new OracleProperties(),
            testCase, sub, stage);
        assertTrue(oracle instanceof SmartOracle);
        assertFalse(oracle.getIgnoreCase());
        assertThat(oracle.getScoring(), is(Oracle.ScoringOptions.All));
        assertThat(oracle.getPrecision(), lessThan(0.0));
        assertTrue(oracle.getIgnoreWS());
        assertTrue(oracle.getIgnoreEmptyLines());
        assertThat(oracle.getCommand(), is(""));
        assertThat(oracle.getCap(), is(OracleProperties.DEFAULT_POINT_CAP));
	}

    @Test
	void testImplicitSmartWithSettings() throws FileNotFoundException {
        final double tPrecision = 0.01;
        final int tCap = 80;
        OracleProperties prop = new OracleProperties();
        prop.caseSig = false;
        prop.precision = tPrecision;
        prop.emptyLines = true;
        prop.cap = tCap;
		Oracle oracle = OracleFactory.getOracle(prop, testCase, sub, stage);
        assertTrue(oracle instanceof SmartOracle);
        assertTrue(oracle.getIgnoreCase());
        assertThat(oracle.getScoring(), is(Oracle.ScoringOptions.All));
        assertThat(oracle.getPrecision(), is(tPrecision));
        assertTrue(oracle.getIgnoreWS());
        assertFalse(oracle.getIgnoreEmptyLines());
        assertThat(oracle.getCommand(), is(""));
        assertThat(oracle.getCap(), is(tCap));
	}

    @Test
	void testSmartByName() throws FileNotFoundException {
        final double tPrecision = 0.01;
        final int tCap = 80;

        OracleProperties prop = new OracleProperties();
        prop.oracle = "smart";
        prop.caseSig = false;
        prop.precision = tPrecision;
        prop.emptyLines = true;
        prop.cap = tCap;
		
		Oracle oracle = OracleFactory.getOracle(prop, testCase, sub, stage);
        assertTrue(oracle instanceof SmartOracle);
        assertTrue(oracle.getIgnoreCase());
        assertThat(oracle.getScoring(), is(Oracle.ScoringOptions.All));
        assertThat(oracle.getPrecision(), is(tPrecision));
        assertTrue(oracle.getIgnoreWS());
        assertFalse(oracle.getIgnoreEmptyLines());
        assertThat(oracle.getCommand(), is(""));
        assertThat(oracle.getCap(), is(tCap));
	}

    @Test
	void testCommandByName() throws FileNotFoundException {
        final int tCap = 80;

        OracleProperties prop = new OracleProperties();
        prop.oracle = "external";
        prop.command = "diff -b";
        prop.cap = tCap;

        Oracle oracle = OracleFactory.getOracle(prop, testCase, sub, stage);
        assertTrue(oracle instanceof ExternalOracle);
        assertThat(oracle.getCommand(), is("diff -b"));
        assertThat(oracle.getCap(), is(tCap));
	}

    @Test
	void testOracleFromClassName() throws FileNotFoundException {
        OracleProperties prop = new OracleProperties();
        prop.oracle = "edu.odu.cs.zeil.codegrader.oracle.ExternalOracle";
        prop.scoring = Oracle.ScoringOptions.ByLine;
        prop.ws = true;

        Oracle oracle = OracleFactory.getOracle(prop, testCase, sub, stage);
        assertTrue(oracle instanceof ExternalOracle);
        assertFalse(oracle.getIgnoreCase());
        assertThat(oracle.getScoring(), is(Oracle.ScoringOptions.ByLine));
        assertThat(oracle.getPrecision(), lessThan(0.0));
        assertFalse(oracle.getIgnoreWS());
        assertTrue(oracle.getIgnoreEmptyLines());
        assertThat(oracle.getCommand(), is(""));
        assertThat(oracle.getCap(), is(OracleProperties.DEFAULT_POINT_CAP));
	}

}
