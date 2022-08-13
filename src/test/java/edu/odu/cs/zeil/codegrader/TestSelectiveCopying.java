
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSelectiveCopying {

    private final Path asstSrcPath = Paths.get("src", "test", "data",
            "java-sqrt-assignment");
    private final Path submissionPath = asstSrcPath
            .resolve("submissions")
            .resolve("packaged");

    private final Path asstDestPath = Paths.get("build", "test-data",
            "ant-test");

    /**
     * Set up assignment2 params test.
     * 
     * @throws IOException
     * @throws TestConfigurationError
     */
    @BeforeEach
    public void setup() throws IOException, TestConfigurationError {
        asstDestPath.toFile().mkdirs();
    }

    /**
     * Clean up test data.
     * 
     * @throws IOException
     */
    @AfterEach
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(asstDestPath);
    }

    @Test
    void testSimpleCopy() throws IOException {

        FileUtils.copyDirectory(submissionPath, asstDestPath, null, null);

        assertTrue(asstDestPath.resolve("sqrtProg.java").toFile().exists());
        assertTrue(asstDestPath.resolve("unused.java").toFile().exists());
        assertTrue(asstDestPath.resolve("unexpected").toFile().isDirectory());
        assertTrue(asstDestPath
                .resolve("unexpected")
                .resolve("sqrtPrinter.java").toFile().exists());

    }

    @Test
    void testCopyWithInclusions() throws IOException {
        List<String> inclusions = new ArrayList<>();
        inclusions.add("*.java");
        List<String> exclusions = new ArrayList<>();

        FileUtils.copyDirectory(submissionPath, asstDestPath, 
            inclusions, exclusions);

        assertTrue(asstDestPath.resolve("sqrtProg.java").toFile().exists());
        assertTrue(asstDestPath.resolve("unused.java").toFile().exists());
        assertFalse(asstDestPath.resolve("unexpected").toFile().exists());
    }

    @Test
    void testCopyWithGlobInclusions() throws IOException {
        List<String> inclusions = new ArrayList<>();
        inclusions.add("**.java");
        List<String> exclusions = new ArrayList<>();

        FileUtils.copyDirectory(submissionPath, asstDestPath, 
            inclusions, exclusions);

        assertTrue(asstDestPath.resolve("sqrtProg.java").toFile().exists());
        assertTrue(asstDestPath.resolve("unused.java").toFile().exists());
        assertTrue(asstDestPath.resolve("unexpected").toFile().isDirectory());
        assertTrue(asstDestPath
                .resolve("unexpected")
                .resolve("sqrtPrinter.java").toFile().exists());
    }

    @Test
    void testCopyWithExclusions() throws IOException {
        List<String> inclusions = new ArrayList<>();
        inclusions.add("**.java");
        List<String> exclusions = new ArrayList<>();
        exclusions.add("unused*");

        FileUtils.copyDirectory(submissionPath, asstDestPath, 
            inclusions, exclusions);

        assertTrue(asstDestPath.resolve("sqrtProg.java").toFile().exists());
        assertFalse(asstDestPath.resolve("unused.java").toFile().exists());
        assertTrue(asstDestPath.resolve("unexpected").toFile().isDirectory());
        assertTrue(asstDestPath
                .resolve("unexpected")
                .resolve("sqrtPrinter.java").toFile().exists());
    }


}
