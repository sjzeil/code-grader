package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
        asst.setGoldDirectory(goldPath);

        String tsPropertyStr = FileUtils.readTextFile(
                testSuitePath.resolve("tests.yaml").toFile());
        tsProperties = TestSuitePropertiesBase.loadYAML(tsPropertyStr);
	}




	@Test
	void testGoldBuild() {
		Stage stage = new Stage(asst, tsProperties);

        stage.clear();

        stage.setupStage();
        assertTrue(asst.getGoldStage().toFile().exists());
		assertTrue(asst.getGoldStage().resolve("src").toFile().isDirectory());
		assertTrue(asst.getGoldStage()
			.resolve("src")
			.resolve("sqrtProg.java")
            .toFile().exists());

		String build = stage.getBuildCommand();
		assertThat(build, containsString("javac"));
		assertThat(build, containsString("-cp src"));
		assertThat(build, not(containsString("lib/")));
		assertThat(build, containsString("src/sqrtProg.java"));

		String launch = stage.getLaunchCommand("");
		assertThat(launch, containsString("java"));
		assertThat(launch, containsString("-cp src"));
		assertThat(launch, not(containsString("lib/")));
		assertThat(launch, containsString("sqrtProg"));


        Stage.BuildResult result = stage.buildCode();

        assertThat(result.getStatusCode(), is(0));
        
		assertTrue(asst.getGoldStage()
			.resolve("src")
			.resolve("sqrtProg.class")
            .toFile().exists());

	}

	@Test
	void testSubmissionBuild() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Stage goldStage = new Stage(asst, tsProperties);

        goldStage.clear();

        goldStage.setupStage();

		goldStage.buildCode();

		Path submissionPath = submissionsPath.resolve("perfect");
		Submission submission = new Submission(asst, "perfect",
			submissionPath);
		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission)
			.resolve("src").resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getSubmitterStage(submission)
			.resolve("src")
			.resolve("sqrtProg.class")
			.toFile().exists());

		assertFalse(asst.getSubmitterStage(submission)
			.resolve("sqrtProg.class")
			.toFile().exists());
	}

	@Test
	void testJavaDefaultLaunchBuild() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Path submissionPath = submissionsPath.resolve("perfect");
		Submission submission = new Submission(asst, "perfect", submissionPath);

		Stage goldStage = new Stage(asst, tsProperties);
        goldStage.clear();
        goldStage.setupStage();
		goldStage.buildCode();


		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission)
		.resolve("src")
		.resolve("sqrtProg.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getSubmitterStage(submission)
			.resolve("src")
			.resolve("sqrtProg.class")
			.toFile().exists());

	}

	@Test
	void testJavaSetup() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Path submissionPath = submissionsPath.resolve("flattened");
		Submission submission = new Submission(asst, "flattened",
			submissionPath);

		Stage goldStage = new Stage(asst, tsProperties);
        goldStage.clear();
        goldStage.setupStage();
		goldStage.buildCode();

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("src")
			.toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("src")
			.resolve("sqrtProg.java")
			.toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("src")
			.resolve("unexpected")
			.toFile().isDirectory());
		assertTrue(asst.getSubmitterStage(submission).resolve("src")
			.resolve("unexpected")
			.resolve("sqrtPrinter.java")
			.toFile().exists());

		// Now check if the build ran.
		assertTrue(asst.getGoldStage().resolve("src").resolve("sqrtProg.class")
			.toFile().exists());

	}


}


