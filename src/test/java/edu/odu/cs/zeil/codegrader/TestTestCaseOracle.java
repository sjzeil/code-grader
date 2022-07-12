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

import edu.odu.cs.zeil.codegrader.oracle.Oracle;
import edu.odu.cs.zeil.codegrader.oracle.OracleFactory;
import edu.odu.cs.zeil.codegrader.oracle.OracleResult;


public class TestTestCaseOracle {
	
	public Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	public Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	public Path stagingPath = Paths.get("build", "test-data", "assignment2", "stage");

	public Assignment asst;
	public TestCase testCase;
	
	@BeforeEach
	public void setup() throws IOException {
		testSuitePath.toFile().getParentFile().mkdirs();
		stagingPath.toFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		asst.setStagingDirectory(stagingPath);

		testCase = new TestCase(new TestProperties(asst, "params"));
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(testSuitePath);
	}
	

	@Test
	void testParamsTestCase() throws FileNotFoundException  {

		String actualOutput = "a\nb\nc\n";
		String expectedOutput = "a\nb\nc\n";
		
        Oracle oracle = OracleFactory.getOracle("", testCase); // default oracle
        OracleResult result = oracle.compare(expectedOutput, actualOutput);

		assertThat (result.score, is(100));
		assertThat (result.message, is(Oracle.PassedTestMessage));

		actualOutput = "a\nx\nc\n";
		result = oracle.compare(expectedOutput, actualOutput);

		assertThat (result.score, is(0));
		assertThat (result.message.contains("x"), is(true));
		assertThat (result.message.contains("b"), is(true));

		actualOutput = "a\nb\nc\nd\n";
		result = oracle.compare(expectedOutput, actualOutput);

		assertThat (result.score, is(0));
		assertThat (result.message, not(is(Oracle.PassedTestMessage)));

		actualOutput = "a\nb\n";
		result = oracle.compare(expectedOutput, actualOutput);

		assertThat (result.score, is(0));
		assertThat (result.message, not(is(Oracle.PassedTestMessage)));

	}

}
