package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProperties {

    private Path testDirectory;
    private String name;

    private Assignment assignment;
    private Properties assignmentProperties;
    private Properties localProperties;

    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /**
     * Create a test case directory based upon information in testDirectory and
     * in the assignment directory above it.
     * 
     * @param asst          an assignment
     * @param testDirectory
     * @throws FileNotFoundException if the assignment's test suite directory
     *                               does not exist or
     *                               does not contain a subdirectory matching
     *                               testName
     */
    public TestProperties(Assignment asst, String testName)
            throws FileNotFoundException {
        this.name = testName;
        this.assignment = asst;
        this.testDirectory = asst.getTestSuiteDirectory().resolve(testName);
        if (!this.testDirectory.toFile().isDirectory()) {
            logger.error("Could not find " + testDirectory.toString());
            throw new FileNotFoundException("Could not find "
                    + testDirectory.toString());
        }
        localProperties = new Properties(testDirectory);
        Path assignmentDir = testDirectory.getParent();
        if (assignmentDir != null)
            assignmentProperties = new Properties(assignmentDir);
        else
            throw new FileNotFoundException(testDirectory.toString()
                    + " not within a suite.");
    }

    /**
     * Find the standard in file for a test (any file having a ".in" extension).
     * 
     * @return a path to a ".in" file or the test suite directory if no such
     *         file exists.
     */
    public Path getIn() {
        File[] files = testDirectory.toFile().listFiles();
        if (files != null) {
            for (File inFile : files) {
                if (inFile.getName().endsWith(".in")) {
                    return inFile.toPath();
                }
            }
        }
        return testDirectory;
    }

    public String getParams() {
        return getProperty("params");
    }

    public int getPoints() {
        try {
            int v = Integer.parseInt(getProperty("points"));
            return v;
        } catch (Exception ex) {
            // points are unspecified or incorrectly specified
            return 1;
        }
    }

    private String getProperty(String name) {
        Object value = localProperties.getProperty(name);
        if (value != null)
            return value.toString();
        value = assignmentProperties.getProperty("test", name);
        if (value != null)
            return value.toString();
        return "";
    }

    public String getLaunch() {
        return getProperty("launch");
    }

    public int getTimelimit() {
        String value = getProperty("timelimit");
        if (value.equals("")) {
            return 1;
        } else {
            try {
                int v = Integer.parseInt(value);
                return Math.max(1, v);
            } catch (NumberFormatException ex) {
                return 1;
            }
        }
    }

    public void setLaunch(String command) {
        localProperties.setProperty("launch", command);
    }

    public Path getTestCaseDirectory() {
        return testDirectory;
    }

    public String getName() {
        return name;
    }

    public Assignment getAssignment() {
        return assignment;
    }

}
