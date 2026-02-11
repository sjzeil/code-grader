
package edu.odu.cs.zeil.codegrader;

//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSelectiveCopying260 {

    // test based on a problem encountered grading an assignment in CS260
    private final Path asstSrcPath = Paths.get("src", "test", "data",
            "copyTest260");
    private final Path submissionPath = asstSrcPath;
    private final Path iSrcPath = asstSrcPath;
    private final Path suitePath = asstSrcPath.resolve("Tests");

    private final Path asstDestPath = Paths.get("build", "test-data",
            "copyTest260");

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
        //asstDestPath.toFile().mkdirs();
        FileUtils.copyDirectory(asstSrcPath, asstDestPath, null, null);
    }

    @Test
    void testSuite() throws IOException {

        Path testSuitePath = asstDestPath.resolve("Tests");
        Path stagingPath = Paths.get("build", "test-data", "stage");
        Path submissionsPath = asstDestPath;
        Path recordingPath = Paths.get("build", "test-data", "grades");
        String studentName = "-";
        
        Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath);
		asst.setStagingDirectory(stagingPath);
		asst.setSubmissionsDirectory(submissionsPath);
		asst.setRecordingDirectory(recordingPath);
        asst.setInPlace(true);

        TestSuite suite = new TestSuite(asst);
		suite.clearTheStage(stagingPath);
        suite.performTests();

        assertTrue(recordingPath.toFile().exists());
        assertTrue(recordingPath.resolve("testSummary.csv").toFile().exists());
        
    }


}
