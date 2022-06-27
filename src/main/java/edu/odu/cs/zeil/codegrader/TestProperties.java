package edu.odu.cs.zeil.codegrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestProperties {

    private Path testDirectory;

    private Map<String, Object> assignmentProperties;
    private Map<String, Object> localProperties;

    private Logger log = Logger.getLogger(TestProperties.class.getName());

    /**
     * Create a test case directory based upon information in testDirectory and
     * in the assignment directory above it.
     * 
     * @param testDirectory
     */
    public TestProperties(Path testDirectory) {
        this.testDirectory = testDirectory;
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
        Map<String,Object> testMap = (Map<String,Object>)assignmentProperties.get("test");
        value = testMap.get(name);
        if (value != null) {
            return value.toString();
        }

        return "";
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
            log.log(Level.WARNING, "Error in readContentsOf when reading from " + file.getAbsolutePath(), ex);
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

    public void setLaunch(String string) {
    }

}
