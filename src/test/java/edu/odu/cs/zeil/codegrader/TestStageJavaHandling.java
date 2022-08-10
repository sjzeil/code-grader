package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class TestStageJavaHandling {

	private Path asstSrcPath = Paths.get("src", "test", "data", 
		"java-packaged-asst");

	private Path asstDestPath = Paths.get("build", "test-data", 
		"java-packaged-asst");

    private Path testSuitePath = asstDestPath.resolve("Tests");
    private Path stagingPath = Paths.get("build", "test-data", "stage");
    private Path submissionsPath = asstDestPath.resolve("submissions");
    private Path recordingPath = asstDestPath.resolve("Grades");
    private Path goldPath = asstDestPath.resolve("Gold");

    private Assignment asst;
    private TestSuitePropertiesBase tsProperties;

    /**
     * Set up assignment2 params test.
     * 
     * @throws IOException
     * @throws TestConfigurationError
     */
    @BeforeEach
    public void setup() throws IOException, TestConfigurationError {
        asstDestPath.toFile().getParentFile().mkdirs();
        stagingPath.toFile().mkdirs();
        FileUtils.copyDirectory(asstSrcPath, asstDestPath, null, null,
                StandardCopyOption.REPLACE_EXISTING);

        asst = new Assignment();
        asst.setTestSuiteDirectory(testSuitePath);
        asst.setStagingDirectory(stagingPath);
        asst.setSubmissionsDirectory(submissionsPath);
        asst.setRecordingDirectory(recordingPath);
        asst.setGoldDirectory(goldPath);

        String tsPropertyStr = FileUtils.readTextFile(
                testSuitePath.resolve("tests.yaml").toFile());
        tsProperties = TestSuitePropertiesBase.loadYAML(tsPropertyStr);
    }

    /**
     * Clean up test data.
     * 
     * @throws IOException
     */
    @AfterEach
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(testSuitePath);
    }



	@Test
	void testGoldBuild() {
		Stage stage = new Stage(asst, null, tsProperties);

        stage.clear();

        stage.setupStage();

        assertTrue(asst.getGoldStage().toFile().exists());
        assertTrue(asst.getGoldStage().resolve("sqrtProg.java")
                .toFile().exists());

        Stage.BuildResult result = stage.buildCode();

        assertThat(result.getStatusCode(), is(0));
        
		assertTrue(asst.getGoldStage().toFile().exists());
		assertTrue(asst.getGoldStage().resolve("sqrtProg.java")
			.toFile().exists());
		assertTrue(asst.getGoldStage().resolve("sqrtProg.class")
			.toFile().exists());

	}

	@Test
	void testSubmissionBuild() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Submission submission = new Submission(asst, "perfect");
		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage().toFile().exists());
		assertTrue(asst.getSubmitterStage().resolve("sqrtProg.java")
			.toFile().exists());
		assertTrue(asst.getSubmitterStage().resolve("makefile")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getSubmitterStage().resolve("sqrtProg.class")
			.toFile().exists());

	}

	@Test
	void testJavaDefaultLaunchBuild() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Submission submission = new Submission(asst, "perfect");

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage().toFile().exists());
		assertTrue(asst.getSubmitterStage().resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getSubmitterStage().resolve("sqrtProg.class")
			.toFile().exists());

	}

	@Test
	void testJavaSetup() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Submission submission = new Submission(asst, "flattened");

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage().toFile().exists());
		assertTrue(asst.getSubmitterStage().resolve("src").toFile().exists());
		assertTrue(asst.getSubmitterStage().resolve("src")
			.resolve("sqrtProg.java")
			.toFile().exists());
		assertTrue(asst.getSubmitterStage().resolve("src").resolve("unexpected")
			.toFile().isDirectory());
		assertTrue(asst.getSubmitterStage().resolve("src")
			.resolve("unexpected")
			.resolve("sqrtPrinter.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getGoldStage().resolve("src").resolve("sqrtProg.class")
			.toFile().exists());

	}


}


