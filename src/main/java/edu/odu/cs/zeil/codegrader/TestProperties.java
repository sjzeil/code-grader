package edu.odu.cs.zeil.codegrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProperties {

    private Path testDirectory;
    private String name;

    private Map<String, Object> assignmentProperties;
    private Map<String, Object> localProperties;

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
    public TestProperties(Assignment asst, String testName) throws FileNotFoundException {
        this.name = testName;
        this.testDirectory = asst.getTestSuiteDirectory().resolve(testName);
        if (!this.testDirectory.toFile().isDirectory() ) {
            throw new FileNotFoundException("Could not find " + testDirectory.toString());
        }
        localProperties = loadFirstYamlFile(testDirectory);
        Path assignmentDir = testDirectory.getParent();
        assignmentProperties = loadFirstYamlFile(assignmentDir);
    }


    private Map<String,Object> loadFirstYamlFile (Path dir)
    {
        for (File yamlFile: dir.toFile().listFiles()) {
            if (yamlFile.getName().endsWith(".yaml")) {
                return FileUtils.loadYaml(yamlFile);
            }
        }
        return new HashMap<>();
    }

    public String getParams() {
        return (getProperty("params"));
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
        // First check for properties as inline files
        File testDir = testDirectory.toFile();
        File[] contents = testDir.listFiles();
        String extension = "." + name;
        for (File file : contents) {
            if (file.getName().endsWith(extension)) {
                return readContentsOf(file);
            }
        }

        // Next, check the test case yaml
        Object value = localProperties.get(name);
        if (value != null) {
            return value.toString();
        }

		// Finally, check the assignment yaml
		Object testProps = assignmentProperties.get("test");
		if ((testProps != null) && (testProps instanceof Map<?, ?>)) {
			Map<String, Object> testMap = castToMap(assignmentProperties.get("test"));
			value = testMap.get(name);
			if (value != null) {
				return value.toString();
			}
		}

        return "";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object object) {
		return (Map<String, Object>)object;
	}


	private String readContentsOf(File file) {
        StringBuffer result = new StringBuffer();
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            while (true) {
                String line = in.readLine();
                if (line == null)
                    break;
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
            }
        } catch (IOException ex) {
            logger.warn("Error in readContentsOf when reading from " + file.getAbsolutePath(), ex);
        }
        return result.toString();
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
                return Math.max(1,v);
            } catch (NumberFormatException ex) {
                return 1;
            }
        }
    }

    public void setLaunch(String command) {
        localProperties.put("launch", command);
    }


    public Path getTestCaseDirectory() {
        return testDirectory;
    }


    public String getName() {
        return name;
    }

}
