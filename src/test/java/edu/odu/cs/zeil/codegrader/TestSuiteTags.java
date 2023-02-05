package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class TestSuiteTags {

	//CHECKSTYLE:OFF

	private Path asstSrcPath = Paths.get("src", "test", "data", 
		"java-noGold-assignment");

	private Path asstDestPath = Paths.get("build", "test-data", 
		"java-noGold-assignment");

	private Path testSuitePath = asstDestPath.resolve("Tests");
	private Path stagingPath = Paths.get("build", "test-data", "stage");
	private Path submissionsPath = asstDestPath.resolve("submissions");
	private Path recordingPath = asstDestPath.resolve("Grades");
	private String studentName = "perfect";
	private Path submissionPath = submissionsPath.resolve(studentName);
	private Submission submission;
	

	private Assignment asst;

	/**
	 * Set up assignment2 params test.
	 * @throws IOException
	 * @throws TestConfigurationError
	 */
	@BeforeEach
	public void setup() throws IOException, TestConfigurationError {
        Path testData = Paths.get("build", "test-data");
        if (testData.toFile().exists()) {
            FileUtils.deleteDirectory(testData);
        }

		asstDestPath.toFile().getParentFile().mkdirs();
		stagingPath.toFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, asstDestPath, null, null,
				StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath);
		asst.setStagingDirectory(stagingPath);
		asst.setSubmissionsDirectory(submissionsPath);
		asst.setRecordingDirectory(recordingPath);

		submission = new Submission(asst, studentName, submissionPath);
	}


	@Test
	void testSetTags() {
		TestSuite suite = new TestSuite(asst);
        int nQueued0 = suite.getNumTestsToPerform();
        assertThat(nQueued0, is(0));
        assertThat(suite.isTagActive("test"), is(false));
        suite.setTag("test");
        assertThat(suite.isTagActive("test"), is(true));
        assertThat(suite.getNumTestsToPerform(), greaterThan(nQueued0));
	}

	@Test
	void testClearTag() {
		TestSuite suite = new TestSuite(asst);
        suite.setTag("build");
        suite.setTag("test");
        int nQueued0 = suite.getNumTestsToPerform();
        suite.clearTag("test");
        assertThat(suite.isTagActive("test"), is(false));
        assertThat(suite.getNumTestsToPerform(), lessThan(nQueued0));
	}

	@Test
	void testClearTags() {
		TestSuite suite = new TestSuite(asst);
        suite.setTag("build");
        suite.setTag("test");
        suite.clearTags();
        assertThat(suite.isTagActive("test"), is(false));
        assertThat(suite.isTagActive("build"), is(false));
        assertThat(suite.getNumTestsToPerform(), is(0));
	}

}


