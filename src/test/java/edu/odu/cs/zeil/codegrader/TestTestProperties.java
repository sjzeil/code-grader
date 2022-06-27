package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
//import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestTestProperties {
	
	public Path asst1DirPath = Paths.get("build", "test-data", "assignment1");
	
	@BeforeEach
	public void setup() throws IOException {
		asst1DirPath.toFile().getParentFile().mkdirs();
		Path asst1SrcPath = Paths.get("src", "test", "data", "assignment1");
		FileUtils.copyDirectory(asst1SrcPath, asst1DirPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(asst1DirPath);
	}
	

	@Test
	void testInlineProperty() {
		Path testCase = asst1DirPath.resolve("tests").resolve("largeTests");
		TestProperties testProperties = new TestProperties(testCase);
		int value = testProperties.getTimelimit();
		assertThat(value, is(2));
	}

	@Test
	void testYamlProperty() {
		Path testCase = asst1DirPath.resolve("tests").resolve("simpleTests");
		TestProperties testProperties = new TestProperties(testCase);
		int value = testProperties.getPoints();
		assertThat(value, is(5));
	}

	@Test
	void testDefaultProperty() {
		Path testCase = asst1DirPath.resolve("tests").resolve("simpleTests");
		TestProperties testProperties = new TestProperties(testCase);
		String value = testProperties.getLaunch();
		assertThat(value, is("./dividers"));
	}

}
