
package edu.odu.cs.zeil.codegrader;

//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSelectiveCopying2 {

    private final Path asstSrcPath = Paths.get("src", "test", "data",
            "copyTest2");
    private final Path submissionPath = asstSrcPath
            .resolve("student");
    private final Path goldPath = asstSrcPath
            .resolve("gold");
    private final Path suitePath = asstSrcPath
            .resolve("suite");
    private final Path suitePath2 = asstSrcPath
            .resolve("suite2");

    private final Path asstDestPath = Paths.get("build", "test-data",
            "copyTest2");

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
        asstDestPath.toFile().mkdirs();
    }


    @Test
    void testStudentCopy() throws IOException {

        List<String> inclusions = new ArrayList<>();
        List<String> exclusions = new ArrayList<>();
        inclusions.add("**/CashRegister.java");
        inclusions.add("**/TestRegister.java");
        exclusions.add("makefile");

        assertTrue(submissionPath.toFile().exists());
        FileUtils.copyDirectory(submissionPath, asstDestPath, inclusions, exclusions);

        Path deepSrcPathRel = Paths.get("src", "main", "java", "edu", "odu", "cs", "cs350", "tdd");
        Path deepSrcPath = asstDestPath.resolve(deepSrcPathRel);
        
        assertTrue(deepSrcPath.resolve("CashRegister.java").toFile().exists());
        assertTrue(deepSrcPath.resolve("TestRegister.java").toFile().exists());
        assertFalse(asstDestPath.resolve("makefile").toFile().exists());
        assertFalse(deepSrcPath.resolve("Money.java").toFile().exists());
    }

    @Test
    void testInstructorCopy() throws IOException {

        List<String> inclusions = new ArrayList<>();
        List<String> exclusions = new ArrayList<>();
        inclusions.add("src/**/*.java");
        inclusions.add("lib/*.jar");
        inclusions.add("makefile");
        exclusions.add("**/CashRegister.java");
        exclusions.add("**/TestRegister.java");

        assertTrue(submissionPath.toFile().exists());
        FileUtils.copyDirectory(goldPath, asstDestPath, inclusions, exclusions);

        Path deepSrcPathRel = Paths.get("src", "main", "java", "edu", "odu", "cs", "cs350", "tdd");
        Path deepSrcPath = asstDestPath.resolve(deepSrcPathRel);
        
        assertFalse(deepSrcPath.resolve("CashRegister.java").toFile().exists());
        assertFalse(deepSrcPath.resolve("TestRegister.java").toFile().exists());
        assertTrue(deepSrcPath.resolve("SJZTestRegister.java").toFile().exists());
        assertTrue(asstDestPath.resolve("makefile").toFile().exists());
        assertTrue(deepSrcPath.resolve("Money.java").toFile().exists());
        assertTrue(asstDestPath.resolve("lib").resolve("hamcrest-all-1.3.jar").toFile().exists());
    }

    @Test
    public void testSuiteCopies() {
        Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(suitePath);
		asst.setStagingDirectory(asstDestPath);
		asst.setSubmissionsDirectory(submissionPath);
		asst.setRecordingDirectory(asstDestPath);
        asst.setInstructorCodeDirectory(goldPath);

		Submission submission = new Submission(asst, "johnDoe", submissionPath);
        TestSuite suite = new TestSuite(asst);
        //suite.getProperties().
		
		suite.processThisSubmission(submission);

        Path deepSrcPathRel = Paths.get("src", "main", "java");
        Path stagePath = asstDestPath.resolve("johnDoe");
        Path deepSrcPath = stagePath.resolve(deepSrcPathRel);
        
        assertTrue(deepSrcPath.resolve("CashRegister.java").toFile().exists());
        assertTrue(deepSrcPath.resolve("TestRegister.java").toFile().exists());
        assertTrue(deepSrcPath.resolve("SJZTestRegister.java").toFile().exists());
        assertTrue(stagePath.resolve("makefile").toFile().exists());
        assertTrue(deepSrcPath.resolve("Money.java").toFile().exists());
        assertTrue(stagePath.resolve("lib").resolve("hamcrest-all-1.3.jar").toFile().exists());

        String makeFileContents = FileUtils.readTextFile(stagePath.resolve("makefile").toFile()).strip();
        assertFalse(makeFileContents.contains("student"));
    }

        @Test
    public void testSuiteCopies2() {
        Assignment asst = new Assignment();
		asst.setTestSuiteDirectory(suitePath2);
		asst.setStagingDirectory(asstDestPath);
		asst.setSubmissionsDirectory(submissionPath);
		asst.setRecordingDirectory(asstDestPath);
        asst.setInstructorCodeDirectory(goldPath);

		Submission submission = new Submission(asst, "johnDoe", submissionPath);
        TestSuite suite = new TestSuite(asst);
        //suite.getProperties().
		
		suite.processThisSubmission(submission);

        Path deepSrcPathRel = Paths.get("src", "main", "java");
        Path stagePath = asstDestPath.resolve("johnDoe");
        Path deepSrcPath = stagePath.resolve(deepSrcPathRel);
        
        assertTrue(deepSrcPath.resolve("CashRegister.java").toFile().exists());
        assertTrue(deepSrcPath.resolve("TestRegister.java").toFile().exists());
        assertFalse(deepSrcPath.resolve("SJZTestRegister.java").toFile().exists());
        assertTrue(stagePath.resolve("makefile").toFile().exists());
        assertTrue(deepSrcPath.resolve("Money.java").toFile().exists());
        assertFalse(stagePath.resolve("lib").resolve("hamcrest-all-1.3.jar").toFile().exists());

        String makeFileContents = FileUtils.readTextFile(stagePath.resolve("makefile").toFile()).strip();
        assertFalse(makeFileContents.contains("student"));
    }

}
