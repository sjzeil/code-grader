package edu.odu.cs.zeil.codegrader.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import edu.odu.cs.zeil.codegrader.ExternalProcess;
import edu.odu.cs.zeil.codegrader.FileUtils;

public class SysTestInPlace {

	private Path testDataPath = Paths.get("build", "test-data");
	private Path inPlaceDataPath = Paths.get("src", "systest", "data",
			"inPlace");
	
	/**
	 * Set up assignment2 params test.
	 * 
	 * @throws IOException
	 * @throws TestConfigurationError
	 */
	@BeforeEach
	public void setup() throws IOException {
        if (testDataPath.toFile().exists()) {
            FileUtils.deleteDirectory(testDataPath);
        }
		testDataPath.toFile().mkdirs();
        FileUtils.copyDirectory(inPlaceDataPath, testDataPath, null, null);
	}


	@Test
	void testInPlaceRun() {

		Path submission = testDataPath.resolve("workArea");

        assertTrue(submission.toFile().exists());

		Path recording = submission.resolve("Tests");
		
		String[] args = {
                "-inPlace",
				"-suite", recording.toString(),
				"-submissions", submission.toString(),
				"-recording", recording.toString() //,
		};

        Path cwd = Paths.get("").toAbsolutePath();
        File jar = FileUtils.findFile(Paths.get("build", "libs"),
             ".jar").get();
        String commandLine = "java -cp " + jar.toString()
            + " edu.odu.cs.zeil.codegrader.run.CLI " 
            + String.join(" ", args);
		final int twoMinutes = 120;
        ExternalProcess launcher = new ExternalProcess(cwd, commandLine, 
            twoMinutes, null, "launch from Jar");
        launcher.execute();

        assertEquals(false, launcher.crashed(), launcher.getErr());
        

        // Should leave makefile products in the submission directory
        Path madeFile = submission.resolve("compiled.txt");
        assertTrue(madeFile.toFile().exists());

        // Should leave a grade report in the Tests directory

        String studentName = System.getProperty("user.name");
        Path gradeReport = recording.resolve(studentName + ".html");
        assertTrue(gradeReport.toFile().exists());

	}

	@Test
	void testInPlaceRunWithRcordingOverride() {

		Path submission = testDataPath.resolve("workArea");

        assertTrue(submission.toFile().exists());

		Path suite = submission.resolve("Tests");
		Path recording = testDataPath.resolve("recording");
		
		String[] args = {
                "-inPlace",
				"-suite", suite.toString(),
				"-submissions", submission.toString(),
				"-recording", recording.toString() //,
		};

        Path cwd = Paths.get("").toAbsolutePath();
        File jar = FileUtils.findFile(Paths.get("build", "libs"),
             ".jar").get();
        String commandLine = "java -cp " + jar.toString()
            + " edu.odu.cs.zeil.codegrader.run.CLI " 
            + String.join(" ", args);
		final int twoMinutes = 120;
        ExternalProcess launcher = new ExternalProcess(cwd, commandLine, 
            twoMinutes, null, "launch from Jar");
        launcher.execute();

        assertEquals(false, launcher.crashed(), launcher.getErr());
        

        // Should leave makefile products in the submission directory
        Path madeFile = submission.resolve("compiled.txt");
        assertTrue(madeFile.toFile().exists());

        // Should leave a grade report in the Tests directory

        String studentName = System.getProperty("user.name");
        Path gradeReport = recording.resolve(studentName + ".html");
        assertTrue(gradeReport.toFile().exists());

	}

}
