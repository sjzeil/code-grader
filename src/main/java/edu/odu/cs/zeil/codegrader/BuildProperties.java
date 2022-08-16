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
     * List of file names that students may submit.
     */
    public CopyPatterns studentFiles;

    /**
     * List of file names that instructors will supply.
     */
    public CopyPatterns instructorFiles;

    /**
     * List of directories that serve as roots for java source compilation.
     * If empty, assumed to be only "./".
     */
    public List<String> javaSrcDir;

    /**
     * Initialize the build properties.
     */
    public BuildProperties() {
        command = ""; // Will try to infer command from directory contents.
        timeLimit = 300; // 5 min
        studentFiles = new CopyPatterns();
        instructorFiles = new CopyPatterns();
        javaSrcDir = new ArrayList<>();
    }
}
