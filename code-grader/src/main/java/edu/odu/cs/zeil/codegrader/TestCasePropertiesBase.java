package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class TestCasePropertiesBase  {

    private static final int DEFAULT_RUN_TIMELIMIT = 5;

    @JsonIgnore
    private Path testDirectory;
    @JsonIgnore
    private Assignment assignment;


 //CHECKSTYLE:OFF

    /**
     * Identifies this test case.
     */
    private String name;

    /**
     * A tag name used to determine when (if ever) a test case should
     * be performed.  Default is "test".
     * 
     * A test suite begins by activating all cases of kind "build".
     */
    public Optional<String> kind;


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
     * Is this test a multiplier for the overall score?
     */
    public Optional<Boolean> multiplier;

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
    public Optional<List<OracleProperties>> grading;

    /**
     * Description of this test case (for display in student's grade report)
     */
    public Optional<String> description;

    /**
     * What tags should be activated if this case fails (score < 100)
     */
    public Optional<List<String>> onFail;

    /**
     * What tags should be activated if this case succeeds (score == 100)
     */
    public Optional<List<String>> onSuccess;

    /**
     * If this tag is active, then this case should fail, unrun, with
     *  score of zero.
     */
    public Optional<String> failIf;

    //CHECKSTYLE:ON
    
    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());


    /**
     * Generate the default properties for each property.
     * 
     * @return a properties set consisting only of default values.
     */
    static TestCasePropertiesBase defaults() {
        TestCasePropertiesBase result = new TestCasePropertiesBase();
        result.params = Optional.of("");
        result.weight = OptionalInt.of(1);
        result.multiplier = Optional.of(false);
        result.launch = Optional.of("");
        result.expected = Optional.of("");
        result.timelimit = OptionalInt.of(DEFAULT_RUN_TIMELIMIT);
        result.stderr = Optional.of(false);
        result.status = Optional.of(false);
        result.grading = Optional.of(new ArrayList<>());
        result.description = Optional.of("");
        result.kind = Optional.of("test");
        result.onFail = Optional.of(new ArrayList<>());
        result.onSuccess = Optional.of(new ArrayList<>());
        result.failIf = Optional.of("");
        return result;
    }


    /**
     * Create an empty properties set.
     */
    public TestCasePropertiesBase() {
        testDirectory = null;
        name = "_internal";
        assignment = null;
        kind = Optional.empty();
        params = Optional.empty();
        weight = OptionalInt.empty();
        multiplier = Optional.empty();
        launch = Optional.empty();
        expected = Optional.empty();
        timelimit = OptionalInt.empty();
        stderr = Optional.empty();
        status = Optional.empty();
        grading = Optional.empty();
        description = Optional.empty();
        onFail = Optional.empty();
        onSuccess = Optional.empty();
        failIf = Optional.empty();
    }

    /**
     * Load property values that are stored in explicit files, e.g.,
     * file anything.params can contain values for the "params" property.
     * 
     * @param testCaseDirectory directory containing configuration info
     *      for this test case
     */
    public TestCasePropertiesBase(Path testCaseDirectory) {
        kind = readExplicitString(testCaseDirectory, "kind");
        params = readExplicitString(testCaseDirectory, "params");
        weight = readExplicitInt(testCaseDirectory, "weight");
        multiplier = readExplicitBoolean(testCaseDirectory, "multiplier");
        launch = readExplicitString(testCaseDirectory, "launch");
        expected = readExplicitString(testCaseDirectory, "expected");
        timelimit = readExplicitInt(testCaseDirectory, "timelimit");
        stderr = readExplicitBoolean(testCaseDirectory, "stderr");
        status = readExplicitBoolean(testCaseDirectory, "status");
        grading = Optional.empty();
        description = readExplicitString(testCaseDirectory, "description");
        onFail = Optional.empty();
        onSuccess = Optional.empty();
        failIf = readExplicitString(testCaseDirectory, "failIf");
    }

    private OptionalInt readExplicitInt(Path testCaseDirectory,
        String propertyName) {
        Optional<String> valueStr 
            = readExplicitString(testCaseDirectory, propertyName);
        if (valueStr.isPresent()) {
            try {
                int value = Integer.parseInt(valueStr.get());
                return OptionalInt.of(value);
            } catch (NumberFormatException ex) {
                logger.error("Bad integer content for " + propertyName 
                    + " in "
                    + testCaseDirectory + ": " + valueStr.get());
                return OptionalInt.empty();
            }
        } else {
            return OptionalInt.empty();
        }
    }       

    private Optional<Boolean> readExplicitBoolean(Path testCaseDirectory,
        String propertyName) {
        Optional<String> valueStr
             = readExplicitString(testCaseDirectory, propertyName);
        if (valueStr.isPresent()) {
            boolean value = (valueStr.get().toLowerCase().contains("true"))
                    || (valueStr.get().toLowerCase().contains("1"));
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }       

    private Optional<String> readExplicitString(Path testCaseDirectory,
        String propertyName) {
        Optional<File> propertyFile = FileUtils.findFile(testCaseDirectory,
            "." + propertyName);
        if (propertyFile.isPresent()) {
            return Optional.of(FileUtils.readTextFile(propertyFile.get())
                .trim());
        } else {
            return Optional.empty();
        }
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
            logger.error(message, e);
            throw new TestConfigurationError(message);
        }
    }

    /**
     * Load properties from a File.
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
            logger.error(message, e);
            throw new TestConfigurationError(message);
        }
    }

    private static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, 
            false);
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
            return "**Could not display value.**\n" + e.getMessage();
        }
    }



    


    /**
     * Compare by name.
     * @param right another test cast property collection
     * @return true if the names match
     */
    public boolean equals(Object right) {
        if (right instanceof TestCasePropertiesBase) {
            return name.equals(((TestCasePropertiesBase) right).name);
        } else {
            return false;
        }
    }

    /**
     * Hash code of name.
     * @return hash code
     */
    public int hashCode() {
        return name.hashCode();
    }

}
