package edu.odu.cs.zeil.codegrader.oracle;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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


public class TestNumericToken {

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
	void testIntegerComparisons() throws FileNotFoundException {
		Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));

		OracleProperties settings = new OracleProperties(asst, "params");
		NumberToken a = new NumberToken("42", settings);
		StringToken b = new StringToken("42", settings);
		assertThat (a, not(equalTo(b)));
		NumberToken c = new NumberToken("42", settings);
		assertThat (a, equalTo(c));
		NumberToken d = new NumberToken("43", settings);
		assertThat (a, not(equalTo(d)));
		NumberToken e = new NumberToken("42.0", settings);
		assertThat (a, not(equalTo(e)));
		NumberToken f = new NumberToken("+42", settings);
		assertThat (a, equalTo(f));
	    assertThat(new NumberToken("-42", settings), 
	    		equalTo(new NumberToken("-42", settings)));
	}

	@Test
	void testFloatingPointComparisons() throws FileNotFoundException {
		Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));

		OracleProperties settings = new OracleProperties(asst, "params");
		NumberToken a = new NumberToken("42.00", settings);
		StringToken b = new StringToken("42", settings);
		assertThat (a, not(equalTo(b)));
		NumberToken c = new NumberToken("42.01", settings);
		assertThat (a, equalTo(c));
		c = new NumberToken("41.99", settings);
		assertThat (a, equalTo(c));
		c = new NumberToken("42.009", settings);
		assertThat (a, equalTo(c));
		c = new NumberToken("41.991", settings);
		assertThat (a, equalTo(c));
		c = new NumberToken("42.011", settings);
		assertThat (a, not(equalTo(c)));
		c = new NumberToken("42.899", settings);
		assertThat (a, not(equalTo(c)));
		c = new NumberToken("42", settings);
		assertThat (a, equalTo(c));
		assertThat (c, not(equalTo(a))); // Intentional asymmetry
		
	}

}