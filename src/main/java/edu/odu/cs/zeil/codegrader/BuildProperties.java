package edu.odu.cs.zeil.codegrader;

import java.util.ArrayList;
import java.util.List;
public class BuildProperties {

    /**
     * Command used to compile and build the code.
     */
    public String command;

    /**
     * Amount of time (in seconds) permitted before concluding that
     * the build is hung up.
     */
    public int timeLimit;

    /**
     * List of file names that students may submit and that should not
     * be taken from the instructor-supplied code.
     */
    public List<String> studentFiles;

    /**
     * Initialize the build properties.
     */
    public BuildProperties() {
        command = ""; // Will try to infer command from directory contents.
        timeLimit = 300; // 5 min
        studentFiles = new ArrayList<>(); // Accept all student files.
    }
}
