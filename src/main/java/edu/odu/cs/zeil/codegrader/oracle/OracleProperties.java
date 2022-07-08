package edu.odu.cs.zeil.codegrader.oracle;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.Properties;

public class OracleProperties {

    private Path testDirectory;

    private Properties assignmentProperties;
    private Properties localProperties;

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private boolean caseSensitive;

    /**
     * Create a property set based upon information in testDirectory and
     * in the assignment directory above it.
     * 
     * @param asst an assignment
     * @param testDirectory
     * @throws FileNotFoundException if the assignment's test suite directory does not exist or
     *                    does not contain a subdirectory matching testName
     */
    public OracleProperties(Assignment asst, String testName) throws FileNotFoundException {
        this.testDirectory = asst.getTestSuiteDirectory().resolve(testName);
        if (!this.testDirectory.toFile().isDirectory() ) {
            logger.error ("Could not find " + testDirectory.toString());
            throw new FileNotFoundException("Could not find " + testDirectory.toString());
        }
        localProperties = new Properties(testDirectory);
        Path assignmentDir = testDirectory.getParent();
        assignmentProperties = new Properties(assignmentDir);
        caseSensitive = true;
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
        value = assignmentProperties.getProperty("oracle", name);
        if (value != null)
            return value.toString();
        return "";
    }




    public Path getTestCaseDirectory() {
        return testDirectory;
    }


    private void loadCaseSensitive() {
        Object value = getProperty("casesensitive");
		if (value != null)
			caseSensitive = booleanEval(value.toString());
		else
			caseSensitive = true;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }



	private boolean booleanEval(String string) {
		String lc = string.toLowerCase();
		return lc.equals("true") || lc.equals("yes") || lc.equals("1");
	}



	public void setCaseSensitive(boolean b) {
		caseSensitive = b;
	}


}
