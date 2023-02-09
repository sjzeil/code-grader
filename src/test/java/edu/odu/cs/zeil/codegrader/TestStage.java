package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestStage {

    private Path asstSrcPath = Paths.get("src", "test", "data",
            "java-sqrt-assignment");

    private Path asstDestPath = Paths.get("build", "test-data",
            "java-sqrt-assignment");

    private Path testSuitePath = asstDestPath.resolve("Tests");
    private Path stagingPath = Paths.get("build", "test-data", "stage");
    private Path submissionsPath = asstDestPath.resolve("submissions");
    private Path recordingPath = asstDestPath.resolve("Grades");
    private Path goldPath = asstDestPath.resolve("Gold");

    private Assignment asst;
    private TestSuiteProperties tsProperties;

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
        tsProperties = TestSuiteProperties.loadYAML(tsPropertyStr);
    }


    @Test
    void testGoldBuild() {
        Submission submission = new Submission(asst, "perfect",
                submissionsPath.resolve("perfect"));

        Stage stage = new Stage(asst, tsProperties);

        stage.clear();

        stage.setupStage();

        assertTrue(asst.getGoldStage().toFile().exists());
        assertTrue(asst.getGoldStage().resolve("sqrtProg.java")
                .toFile().exists());

        TestCaseProperties builder = new DefaultBuildCase(tsProperties, asst)
                .generate();
        TestCase builderCase = new TestCase(builder);
        int score = builderCase.performTest(submission, true, stage);

        assertThat(score, is(-1));
        
        assertTrue(asst.getGoldStage().resolve("sqrtProg.class")
                .toFile().exists());

    }

    @Test
    void testSubmissionBuild() {
        Submission submission = new Submission(asst, "perfect",
                submissionsPath.resolve("perfect"));
        
        Stage stage = new Stage(asst, submission, tsProperties);

        stage.clear();

        stage.setupStage();

        // Check first on the submitter stage setup
        assertTrue(asst.getSubmitterStage(submission).toFile().exists());
        assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.java")
                .toFile().exists());
        assertTrue(asst.getSubmitterStage(submission).resolve("makefile")
                .toFile().exists());

        TestCaseProperties builder = new DefaultBuildCase(tsProperties,
                                asst).generate();
        TestCase builderCase = new TestCase(builder);
        int score = builderCase.performTest(submission, false, stage);


        assertThat(score, is(100));
    }

    @Test
    void testSubmissionBuildFailure() {
        Submission submission = new Submission(asst, "doesNotCompile",
                submissionsPath.resolve("doesNotCompile"));
        
        Stage stage = new Stage(asst, submission, tsProperties);

        stage.clear();

        stage.setupStage();

        // Check first on the submitter stage setup
        assertTrue(asst.getSubmitterStage(submission).toFile().exists());
        assertTrue(asst.getSubmitterStage(submission).resolve("sqrtProg.java")
                .toFile().exists());
        assertTrue(asst.getSubmitterStage(submission).resolve("makefile")
                .toFile().exists());

        TestCaseProperties builder = new DefaultBuildCase(tsProperties,
                                asst).generate();
        TestCase builderCase = new TestCase(builder);
        int score = builderCase.performTest(submission, false, stage);


        assertThat(score, is(0));
    }


}
