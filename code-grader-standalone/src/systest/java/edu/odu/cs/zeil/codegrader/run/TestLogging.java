package edu.odu.cs.zeil.codegrader.run;

import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.ExternalProcess;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;

public class TestLogging {

    private Path recording = Paths.get("build", "test-results", "recording");


    /**
     * Set up assignment2 params test.
     * 
     * @throws IOException
     * @throws TestConfigurationError
     */
    @BeforeEach
    public void setup() throws IOException {
        if (recording.toFile().exists()) {
            FileUtils.deleteDirectory(recording);
        Files.createDirectories(recording);
        }
    }


    @Test
    void testLogToRecording() {
        String[] args = {"-recording", recording.toString()};

        Path cwd = Paths.get("").toAbsolutePath();
        File jar = FileUtils.findFile(Paths.get("build", "libs"),
             ".jar").get();
        String commandLine = "java -cp " + jar.toString()
            + " edu.odu.cs.zeil.codegrader.run.CLI " 
            + String.join(" ", args);
		final int tenSeconds = 10;
        ExternalProcess launcher = new ExternalProcess(cwd, commandLine, 
            tenSeconds, null, "launch from Jar");
        launcher.execute();

        //assertEquals(false, launcher.crashed(), launcher.getErr());

        Path logFile = recording.resolve("codegrader.log");
        assertTrue(logFile.toFile().exists());
        String contents = FileUtils.readTextFile(logFile.toFile());
        assertThat(contents, containsStringIgnoringCase("test suite not specified"));
    }

}
