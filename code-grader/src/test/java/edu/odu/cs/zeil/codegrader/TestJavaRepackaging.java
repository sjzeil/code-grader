package edu.odu.cs.zeil.codegrader;

//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.containsString;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class TestJavaRepackaging {

	private Path asstSrcPath = Paths.get("src", "test", "data", 
		"java-repackaged-asst");

	private Path asstDestPath = Paths.get("build", "test-data", 
		"java-repackaged-asst");

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
	void testJavaRepackaging() {
		TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);

		Path submissionPath = submissionsPath.resolve("flattened");
		Submission submission = new Submission(asst, "flattened",
				submissionPath);

		Stage goldStage = new Stage(asst, tsProperties);
		goldStage.clear();
		goldStage.setupStage();

		suite.processThisSubmission(submission);

		// Check first on the submitter stage setup
		assertTrue(asst.getSubmitterStage(submission).toFile().exists());
		assertTrue(asst.getSubmitterStage(submission).resolve("src")
				.toFile().exists());

        Path srcMainJava = asst.getSubmitterStage(submission).resolve("src")
            .resolve("main").resolve("java");
        Path srcTestJava = asst.getSubmitterStage(submission).resolve("src")
            .resolve("test").resolve("java");

        assertTrue(srcMainJava.resolve("pack1")
			.resolve("sqrtProg.java")
			.toFile().exists());
		assertFalse(srcMainJava.resolve("pack1")
			.resolve("testSqrt.java")
			.toFile().exists());
		assertTrue(srcMainJava.resolve("pack1")
			.resolve("sqrtPrinter.java")
			.toFile().exists());

        assertFalse(srcTestJava.resolve("pack1")
			.resolve("sqrtProg.java")
			.toFile().exists());
		assertTrue(srcTestJava.resolve("pack1")
			.resolve("testSqrt.java")
			.toFile().exists());
		assertFalse(srcTestJava.resolve("pack1")
			.resolve("sqrtPrinter.java")
			.toFile().exists());
	}


}


