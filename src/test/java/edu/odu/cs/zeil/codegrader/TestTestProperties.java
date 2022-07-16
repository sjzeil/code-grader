package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestTestProperties {
	
	public Path asst1DirPath = Paths.get("build", "test-data", "assignment1");

	Assignment asst;
	
	@BeforeEach
	public void setup() throws IOException {
		asst = new Assignment();
		asst.setTestSuiteDirectory(asst1DirPath.resolve("tests"));
		asst1DirPath.toFile().getParentFile().mkdirs();
		Path asst1SrcPath = Paths.get("src", "test", "data", "assignment1");
		FileUtils.copyDirectory(asst1SrcPath, asst1DirPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(asst1DirPath);
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
		TestCaseProperties testProperties 
			= new TestCaseProperties(asst, "simpleTests");
		int value = testProperties.getPoints();
		assertThat(value, is(5));
	}

	@Test
	void testDefaultProperty() 
		throws FileNotFoundException, TestConfigurationError {
		TestCaseProperties testProperties
			= new TestCaseProperties(asst, "simpleTests");
		String value = testProperties.getLaunch();
		assertThat(value, is("./dividers"));
	}

	@Test
	void testTestCaseBadCase() {
		assertThrows (FileNotFoundException.class,
	        () -> new TestCaseProperties(asst, "bogus"));
	}


}
