package edu.odu.cs.zeil.codegrader;

import java.util.ArrayList;
import java.util.List;
public class CopyPatterns {

    //CHECKSTYLE:OFF
    /**
     * List of file patterns to include in a copy.
     */
    public List<String> include;

    /**
     * List of files to exclude from a copy.
     */
    public List<String> exclude;
    //CHECKSTYLE:ON

    /**
     * Initialize the build properties.
     */
    public CopyPatterns() {
        include = new ArrayList<>(); // Accept all files
        exclude = new ArrayList<>(); // Exclude nothing
    }
}
