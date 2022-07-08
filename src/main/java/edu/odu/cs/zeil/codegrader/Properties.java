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

/**
 * A collection of properties loaded from a YAML file or from files with property-named
 * extensions.
 */
public class Properties {

    private Path scope;
    private Map<String, Object> localProperties;

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    

    /**
     * Create a property set. 
     * 
     * @param propertyDir a directory indicating the scope of the properties.
     * @throws FileNotFoundException if the directory does not exist
     */
    public Properties(Path propertyDir) throws FileNotFoundException {
        scope = propertyDir;
        if (!scope.toFile().isDirectory() ) {
            throw new FileNotFoundException("Could not find " + scope.toString());
        }
        localProperties = loadFirstYamlFile(scope);
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

   
    public Object getProperty(String name) {
        // First check for properties as inline files
        File testDir = scope.toFile();
        File[] contents = testDir.listFiles();
        String extension = "." + name;
        for (File file : contents) {
            if (file.getName().endsWith(extension)) {
                return readContentsOf(file);
            }
        }

        // Next, check the test case yaml
        Object value = localProperties.get(name);
        return value;
    }


    public Object getProperty(String scopeName, String name) {
        try {
            Map<String, Object> propertiesInScope = castToMap(localProperties.get(scopeName));
            return propertiesInScope.get(name);
        } catch (Exception e) {
            return null;
        }
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


    public void setProperty(String property, String value) {
        localProperties.put(property, value);
    }


    public Path getScope() {
        return scope;
    }

}
