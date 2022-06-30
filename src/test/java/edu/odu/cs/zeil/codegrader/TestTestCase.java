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


public class TestTestCase {
	
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
	void testTestCaseConstructor() {
        Assignment asst = new Assignment();
        TestProperties testProperties = new TestProperties(asst, "params");
        Submission submission = new Submission (asst, "student1");
        TestCase testCase = new TestCase(testProperties);
		assertThat (testCase.getOutput(), is(""));
		assertThat (testCase.getErr(), is(""));
		assertThat (testCase.timedOut(), is(false));
		assertThat (testCase.crashed(), is(false));
	}

}
