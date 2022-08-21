package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The properties available to describe a test suite.
 */
public class TestSuitePropertiesBase {

    /**
     * error logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());


    /**
     * Default properties common to all test cases.
     */
    public TestCasePropertiesBase test;

    /**
     * Properties related to setup and build.
     */
    public BuildProperties build;


    /**
     * Path to grade calculation spreadsheet.
     */
    public String reportTemplate;

    /**
     * Assignment name.
     */
    public String assignment;

    /**
     * Date & time when assignment is due.
     */
    public String dueDate;

    /**
     * Command to get date when submission was submitted
     */
    public String findDateSubmitted;

    /**
     * Create a property set with an empty list of grading options.
     */
    public TestSuitePropertiesBase() {
        test = new TestCasePropertiesBase();
        build = new BuildProperties();
        reportTemplate = "";
        assignment = "";
        dueDate = "";
    }

    /**
     * Load properties from a string.
     * 
     * @param input properties in YAML
     * @return the properties
     * @throws TestConfigurationError if input cannot be parsed
     */
    public static TestSuitePropertiesBase loadYAML(String input)
        throws TestConfigurationError {
        ObjectMapper mapper = getMapper();
        try {
            TestSuitePropertiesBase result = mapper.readValue(input, 
                TestSuitePropertiesBase.class);
            return result;
        } catch (JsonProcessingException e) {
            String message = "Cannot load YAML from string input\n"
                + e.getMessage();
            LOG.error(message, e);
            throw new TestConfigurationError(message);
        }
    }

    /**
     * Load properties from a string.
     * 
     * @param input file containing properties in YAML
     * @return the properties
     * @throws TestConfigurationError if input cannot be parsed
     */
    public static TestSuitePropertiesBase loadYAML(File input)
        throws TestConfigurationError {
        ObjectMapper mapper = getMapper();
        try {
            TestSuitePropertiesBase result = mapper.readValue(input, 
                TestSuitePropertiesBase.class);
            return result;
        } catch (IOException e) {
            String message = "Cannot load YAML from " + input.toString() + "\n"
                + e.getMessage();
            LOG.error(message, e);
            throw new TestConfigurationError(message);
        }
    }

    private static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.findAndRegisterModules();
        /*  Not sure if this is a good idea.
        objectMapper.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, 
            false);
        */
        return objectMapper;
    }

    /**
     * Render as a string (YAML).
     */
    @Override
    public String toString() {
        ObjectMapper mapper = getMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "**Could not display value.**";
        }
    }

}
