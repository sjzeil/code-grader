package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestTestProperties {
	
	private Path asst1DirPath = Paths.get("build", "test-data", "assignment1");

	private Assignment asst;
	
	/**
	 * initialize fixtures.
	 * @throws IOException
	 */
	@BeforeEach
	public void setup() throws IOException {
        Path testData = Paths.get("build", "test-data");
        if (testData.toFile().exists()) {
            FileUtils.deleteDirectory(testData);
        }

		asst = new Assignment();
		asst.setTestSuiteDirectory(asst1DirPath.resolve("tests"));
		asst1DirPath.toFile().getParentFile().mkdirs();
		Path asst1SrcPath = Paths.get("src", "test", "data", "assignment1");
		FileUtils.copyDirectory(asst1SrcPath, asst1DirPath, null, null, 
			StandardCopyOption.REPLACE_EXISTING);
	}
	
	

	@Test
	void testInlineProperty() 
		throws FileNotFoundException, TestConfigurationError {
		TestCaseProperties testProperties
			= new TestCaseProperties(asst, "largeTests");
		int value = testProperties.getTimelimit();
		assertThat(value, is(2));
	}

	@Test
	void testYamlProperty() 
		throws FileNotFoundException, TestConfigurationError {
		//CHECKSTYLE:OFF

		TestCaseProperties testProperties 
			= new TestCaseProperties(asst, "simpleTests");
		int value = testProperties.getWeight();
		assertThat(value, is(5));
		//CHECKSTYLE:ON
	}

	@Test
	void testCaseOverridesSuite() 
		throws FileNotFoundException, TestConfigurationError {
		TestCaseProperties testProperties
			= new TestCaseProperties(asst, "simpleTests");
		String value = testProperties.getLaunch();
		assertThat(value, is("././dividers"));
	}

	@Test
	void testCaseOraclePropOverride() 
		throws FileNotFoundException, TestConfigurationError {
		TestCaseProperties testProperties
			= new TestCaseProperties(asst, "simpleTests");
		List<OracleProperties> value = testProperties.grading;
		assertThat(value.size(), is(2));
	}
	

}
