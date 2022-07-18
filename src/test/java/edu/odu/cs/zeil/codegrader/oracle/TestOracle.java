package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestCaseProperties;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;


public class TestOracle {

	public Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	public Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	
	Assignment asst;
	TestCase testCase;
	

	@BeforeEach
	public void setup() throws IOException, TestConfigurationError {
		testSuitePath.toFile().getParentFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, 
            StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		testCase = new TestCase(new TestCaseProperties(asst, "params"));
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(testSuitePath);
	}
	

	@Test
	void testDefaults() throws FileNotFoundException {
		Oracle oracle = OracleFactory.getOracle(new OracleProperties(), testCase);
        assertTrue(oracle instanceof SmartOracle);
        assertFalse(oracle.getIgnoreCase());
        assertThat(oracle.getScoring(), is(Oracle.ScoringOptions.All));
        assertThat(oracle.getPrecision(), lessThan(0.0));
        assertTrue(oracle.getIgnoreWS());
        assertTrue(oracle.getIgnoreEmptyLines());
        assertThat(oracle.getCommand(), is(""));
        assertThat(oracle.getCap(), is(100));
	}

    @Test
	void testImplicitSmartWithSettings() throws FileNotFoundException {
        OracleProperties prop = new OracleProperties();
        prop.caseSig = Optional.of(false);
        prop.precision = OptionalDouble.of(0.01);
        prop.emptylines = Optional.of(true);
        prop.cap = OptionalInt.of(80);
		Oracle oracle = OracleFactory.getOracle(prop, testCase);
        assertTrue(oracle instanceof SmartOracle);
        assertTrue(oracle.getIgnoreCase());
        assertThat(oracle.getScoring(), is(Oracle.ScoringOptions.All));
        assertThat(oracle.getPrecision(), is(0.01));
        assertTrue(oracle.getIgnoreWS());
        assertFalse(oracle.getIgnoreEmptyLines());
        assertThat(oracle.getCommand(), is(""));
        assertThat(oracle.getCap(), is(80));
	}

    @Test
	void testSmartByName() throws FileNotFoundException {
        OracleProperties prop = new OracleProperties();
        prop.oracle = Optional.of("smart");
        prop.caseSig = Optional.of(false);
        prop.precision = OptionalDouble.of(0.01);
        prop.emptylines = Optional.of(true);
        prop.cap = OptionalInt.of(80);
		
		Oracle oracle = OracleFactory.getOracle(prop, testCase);
        assertTrue(oracle instanceof SmartOracle);
        assertTrue(oracle.getIgnoreCase());
        assertThat(oracle.getScoring(), is(Oracle.ScoringOptions.All));
        assertThat(oracle.getPrecision(), is(0.01));
        assertTrue(oracle.getIgnoreWS());
        assertFalse(oracle.getIgnoreEmptyLines());
        assertThat(oracle.getCommand(), is(""));
        assertThat(oracle.getCap(), is(80));
	}

    @Test
	void testCommandByName() throws FileNotFoundException {
        OracleProperties prop = new OracleProperties();
        prop.oracle = Optional.of("external");
        prop.command = Optional.of("diff -b");
        prop.cap = OptionalInt.of(75);

        Oracle oracle = OracleFactory.getOracle(prop, testCase);
        assertTrue(oracle instanceof ExternalOracle);
        assertThat(oracle.getCommand(), is("diff -b"));
        assertThat(oracle.getCap(), is(75));
	}

    @Test
	void testOracleFromClassName() throws FileNotFoundException {
        OracleProperties prop = new OracleProperties();
        prop.oracle = Optional.of("edu.odu.cs.zeil.codegrader.oracle.ExternalOracle");
        prop.scoring = Optional.of(Oracle.ScoringOptions.ByLine);
        prop.ws = Optional.of(true);

        Oracle oracle = OracleFactory.getOracle(prop, testCase);
        assertTrue(oracle instanceof ExternalOracle);
        assertFalse(oracle.getIgnoreCase());
        assertThat(oracle.getScoring(), is(Oracle.ScoringOptions.ByLine));
        assertThat(oracle.getPrecision(), lessThan(0.0));
        assertFalse(oracle.getIgnoreWS());
        assertTrue(oracle.getIgnoreEmptyLines());
        assertThat(oracle.getCommand(), is(""));
        assertThat(oracle.getCap(), is(100));
	}

}
