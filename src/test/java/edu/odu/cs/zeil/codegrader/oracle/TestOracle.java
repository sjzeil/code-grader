package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.FileUtils;


public class TestOracle {

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
	void testTextCompare() throws FileNotFoundException {
		String expected = "Twas brillig and the slithy toves\ndid gyre and gimble in the wabe;";
		String actual = "Twas brillig and the slithy toves\ndid gyre and gimble in the wabe;";
		Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));

		OracleProperties config = new OracleProperties(asst, "params");
		Oracle oracle = new Oracle(config);
		assertThat (oracle.compare(expected, actual), equalTo(true));

		String actualCaseVariant = "twas Brillig and the slithy toves\ndid gyre and gimble in the wabe;";
		assertThat (oracle.compare(expected, actualCaseVariant), equalTo(false));
		config.setCaseSensitive (false);
		assertThat (oracle.compare(expected, actualCaseVariant), equalTo(true));

	}

	@Test
	void testNumericCompare() throws FileNotFoundException {
		String expected = "If train A leaves New York traveling at 35 mph and\n"
				+ "train B leaves Boston traveling at 42.50 mph,";
				Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));

		OracleProperties config = new OracleProperties(asst, "params");
		Oracle oracle = new Oracle(config);
		assertThat (oracle.compare(expected, expected), equalTo(true));
		
		String reformatted = "If train A leaves New York traveling at 35.0 mph and\n"
				+ "train B leaves Boston traveling at 42.5 mph,";
		assertThat (oracle.compare(expected, reformatted), equalTo(false));

		String highOK = "If train A leaves New York traveling at 35 mph and\n"
				+ "train B leaves Boston traveling at 42.51 mph,";
		assertThat (oracle.compare(expected, highOK), equalTo(true));
	
		String lowOK = "If train A leaves New York traveling at 35 mph and\n"
				+ "train B leaves Boston traveling at 42.49 mph,";
		assertThat (oracle.compare(expected, lowOK), equalTo(true));
	
		String tooHigh1 = "If train A leaves New York traveling at 36 mph and\n"
				+ "train B leaves Boston traveling at 42.4 mph,";
		assertThat (oracle.compare(expected, tooHigh1), equalTo(false));

		String tooHigh2 = "If train A leaves New York traveling at 35 mph and\n"
				+ "train B leaves Boston traveling at 42.61 mph,";
		assertThat (oracle.compare(expected, tooHigh2), equalTo(false));
	
		String tooLow1 = "If train A leaves New York traveling at 34 mph and\n"
				+ "train B leaves Boston traveling at 42.5 mph,";
		assertThat (oracle.compare(expected, tooLow1), equalTo(false));

		String tooLow2 = "If train A leaves New York traveling at 35 mph and\n"
				+ "train B leaves Boston traveling at 42.45 mph,";
		assertThat (oracle.compare(expected, tooLow2), equalTo(false));
	}

}
