package edu.odu.cs.zeil.codegrader.oracle;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.Properties;

public class OracleProperties {

    private Path testDirectory;
    private String name;

    private Assignment assignment;
    private Properties assignmentProperties;
    private Properties localProperties;

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    

    /**
     * Create a test case directory based upon information in testDirectory and
     * in the assignment directory above it.
     * 
     * @param asst an assignment
     * @param testDirectory
     * @throws FileNotFoundException if the assignment's test suite directory does not exist or
     *                    does not contain a subdirectory matching testName
     */
    public OracleProperties(Assignment asst, String testName) throws FileNotFoundException {
        this.name = testName;
        this.assignment = asst;
        this.testDirectory = asst.getTestSuiteDirectory().resolve(testName);
        if (!this.testDirectory.toFile().isDirectory() ) {
            logger.error ("Could not find " + testDirectory.toString());
            throw new FileNotFoundException("Could not find " + testDirectory.toString());
        }
        localProperties = new Properties(testDirectory);
        Path assignmentDir = testDirectory.getParent();
        assignmentProperties = new Properties(assignmentDir);
    }


    /**
     * Find the .expected file for a test.
     * @return a path to a ".expected" file or the test suite directory if no such file exists.
     */
    public Path getExpected() {
        for (File inFile: testDirectory.toFile().listFiles()) {
            if (inFile.getName().endsWith(".in")) {
                return inFile.toPath();
            }
        }
        return testDirectory;
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


    public boolean isCaseSensitive() {
        return false;
    }


    public void setCaseSensitive(boolean b) {
    }


	public void setType(String string) {
	}

}
