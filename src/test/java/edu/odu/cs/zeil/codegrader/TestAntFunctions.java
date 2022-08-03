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

import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class TestAntFunctions {


	private final Path asstSrcPath = Paths.get("src", "test", "data", 
		"java-sqrt-assignment");
    private final Path submissionPath = asstSrcPath
        .resolve("submissions")
        .resolve("packaged");

	private final Path asstDestPath = Paths.get("build", "test-data", 
		"ant-test");



	/**
	 * Set up assignment2 params test.
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
	void testSimpleCopy() {
        Copy copyTask = new Copy();
        copyTask.setTodir(asstDestPath.toFile());

        FileSet filesToCopy = new FileSet();
        filesToCopy.setDir(asstDestPath.toFile());
        
        //FilenameSelector selector = new FilenameSelector();
        //selector.setName("**/*.java");
        //filesToCopy.addFilename(selector);
        
        copyTask.addFileset(filesToCopy);
        copyTask.execute();

        assertTrue(asstDestPath.resolve("sqrtProg.java").toFile().exists());
        assertTrue(asstDestPath.resolve("unused.java").toFile().exists());
        assertTrue(asstDestPath.resolve("unexpected").toFile().isDirectory());
        assertTrue(asstDestPath
            .resolve("unexpected")
            .resolve("sqrtPrinter.java").toFile().exists());
        
	}

}


