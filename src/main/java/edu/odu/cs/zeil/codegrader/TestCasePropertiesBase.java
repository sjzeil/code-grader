package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The properties available to describe a test case.
 * 
 * Each may be null if it has been left unspecified.
 */
public class TestCasePropertiesBase {

    /**
     * error logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());


    /**
     * The command line parameters to be supplies when executing the
     * program under evaluation.
    */
    public Optional<String> params;

    /**
     * How many points this test case is worth? 
     */
    public OptionalInt weight;

    /**
     * The command string used to launch the program under evaluation.
     */
    public Optional<String> launch;

    /**
     * A string containing expected output when the program under
     * evaluation is run.
     */
    public Optional<String> expected;

    /**
     * How many seconds to allow a test execution to run before concluding
     * that the program is hanging, caught in an infinite loop, or simply
     * unacceptably slow.
     */
    public OptionalInt timelimit;

    /**
     * Should the standard error stream be captures and included in the
     * program output being evaluated?
     */
    public Optional<Boolean> stderr;

    /**
     * Should the status code be checked for evidence that a program
     * has crashed? Defaults to false because students aren't likely to
     * be all that careful about returning status codes.
     */
    public Optional<Boolean> status;


    /**
     * The grading criteria for this test case.
     */
    public List<OracleProperties> grading;

    /**
     * Create a property set with an empty list of grading options.
     */
    public TestCasePropertiesBase() {
        params = Optional.empty();
        weight = OptionalInt.empty();
        launch = Optional.empty();
        expected = Optional.empty();
        timelimit = OptionalInt.empty();
        stderr = Optional.empty();
        status = Optional.empty();
        grading = new ArrayList<>();
    }

    /**
     * Load properties from a string.
     * 
     * @param input properties in YAML
     * @return the properties
     * @throws TestConfigurationError if input cannot be parsed
     */
    public static TestCasePropertiesBase loadYAML(String input)
        throws TestConfigurationError {
        ObjectMapper mapper = getMapper();
        try {
            TestCasePropertiesBase result = mapper.readValue(input, 
                TestCasePropertiesBase.class);
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
    public static TestCasePropertiesBase loadYAML(File input)
        throws TestConfigurationError {
        ObjectMapper mapper = getMapper();
        try {
            TestCasePropertiesBase result = mapper.readValue(input, 
                TestCasePropertiesBase.class);
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
