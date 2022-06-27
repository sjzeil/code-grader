package edu.odu.cs.zeil.codegrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestProperties {

    private Path testDirectory;

    private Logger log = Logger.getLogger(TestProperties.class.getName());

    /**
     * Create a test case directory based upon information in testDirectory and
     * in the assignment directory above it.
     * 
     * @param testDirectory
     */
    public TestProperties(Path testDirectory) {
        this.testDirectory = testDirectory;
    }

    public String getPoints() {
        return getProperty("points");
    }

    private String getProperty(String name) {
        File testDir = testDirectory.toFile();
        File[] contents = testDir.listFiles();
        String extension = "." + name;
        for (File file : contents) {
            System.err.println(file.getName());
            if (file.getName().endsWith(extension)) {
                return readContentsOf(file);
            }
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
        return null;
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
