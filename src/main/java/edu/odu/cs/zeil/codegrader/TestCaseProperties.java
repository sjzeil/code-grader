package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.NoSuchElementException;
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

public class TestCaseProperties {

    private static final int DEFAULT_RUN_TIMELIMIT = 5;

    @JsonIgnore
    private Path testDirectory;
    private String name;

    @JsonIgnore
    private Assignment assignment;


 //CHECKSTYLE:OFF

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
     * Description of this test case (for display in student's grade report)
     */
    public Optional<String> description;

    //CHECKSTYLE:ON
    

    /**
     * The file to supply to standard input when the program under
     * evaluation is run.
     */
    private File stdIn;


    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /**
     * Create a test case directory based upon information in testDirectory and
     * in the assignment directory above it.
     * 
     * @param asst     an assignment
     * @param testName name of this test case. Must match a subdirectory
     *                 of the test suite.
     * @throws FileNotFoundException if the assignment's test suite directory
     *                               does not exist or
     *                               does not contain a subdirectory matching
     *                               testName
     */
    public TestCaseProperties(Assignment asst, String testName)
            throws TestConfigurationError {
        this.name = testName;
        this.assignment = asst;
        this.testDirectory = asst.getTestSuiteDirectory().resolve(testName);
        if (!this.testDirectory.toFile().isDirectory()) {
            logger.error("Could not find " + testDirectory.toString());
            throw new TestConfigurationError("Could not find "
                    + testDirectory.toString());
        }
        TestSuiteProperties suiteProperties;
        try {
            File defaultsFile = FileUtils.findFile(
                    asst.getTestSuiteDirectory(),
                    ".yaml").get();
            suiteProperties = TestSuiteProperties.loadYAML(defaultsFile);
        } catch (NoSuchElementException ex) {
            suiteProperties = new TestSuiteProperties();
        }
        TestCaseProperties suiteDefaults = suiteProperties.test;
        TestCaseProperties caseProperties = new TestCaseProperties();
        try {
            File yamlFile = FileUtils.findFile(testDirectory,
                    ".yaml").get();
            caseProperties = TestCaseProperties.loadYAML(yamlFile);
        } catch (NoSuchElementException ex) {
            caseProperties = new TestCaseProperties();
        }
        TestCaseProperties defaults = TestCaseProperties.defaults();
        TestCaseProperties explicit = new TestCaseProperties(testDirectory);
        resolveDefaults(explicit, caseProperties, suiteDefaults, defaults);
    }

    /**
     * Generate the default properties for each property.
     * 
     * @return a properties set consisting only of default values.
     */
    static TestCaseProperties defaults() {
        TestCaseProperties result = new TestCaseProperties();
        result.params = Optional.of("");
        result.weight = OptionalInt.of(1);
        result.launch = Optional.of("");
        result.expected = Optional.of("");
        result.timelimit = OptionalInt.of(DEFAULT_RUN_TIMELIMIT);
        result.stderr = Optional.of(false);
        result.status = Optional.of(false);
        result.grading = new ArrayList<>();
        result.description = Optional.of("");
        return result;
    }

    private void resolveDefaults(
            TestCaseProperties explicit,
            TestCaseProperties tCase, 
            TestCaseProperties suite, 
            TestCaseProperties defaults) {
        params = selectStringValue(explicit.params, tCase.params,
            suite.params, defaults.params);
        weight = selectIntValue(explicit.weight, tCase.weight,
            suite.weight, defaults.weight);
        launch = selectStringValue(explicit.launch, tCase.launch,
            suite.launch, defaults.launch);
        expected = selectStringValue(explicit.expected, tCase.expected,
            suite.expected, defaults.expected);
        timelimit = selectIntValue(explicit.timelimit, tCase.timelimit,
            suite.timelimit, defaults.timelimit);
        stderr = selectBooleanValue(explicit.stderr, tCase.stderr,
            suite.stderr, defaults.stderr);
        status = selectBooleanValue(explicit.status, tCase.status,
            suite.status, defaults.status);
        description = selectStringValue(explicit.description, tCase.description,
            suite.description, defaults.description);

        if (tCase.grading.size() > 0) {
            grading = tCase.grading;
        } else {
            grading = suite.grading;
        }

        try {
            stdIn = FileUtils.findFile(testDirectory, ".in").get();
        } catch (NoSuchElementException e) {
            stdIn = null;
        }
    }

    /**
     * Create an empty properties set.
     */
    public TestCaseProperties() {
        testDirectory = null;
        name = "_internal";
        assignment = null;
        params = Optional.empty();
        weight = OptionalInt.empty();
        launch = Optional.empty();
        expected = Optional.empty();
        timelimit = OptionalInt.empty();
        stderr = Optional.empty();
        status = Optional.empty();
        grading = new ArrayList<>();
        description = Optional.empty();
    }

    /**
     * Load property values that are stored in explicit files, e.g.,
     * file anything.params can contain values for the "params" property.
     * 
     * @param testCaseDirectory directory containing configuration info
     *      for this test case
     */
    private TestCaseProperties(Path testCaseDirectory) {
        params = readExplicitString(testCaseDirectory, "params");
        weight = readExplicitInt(testCaseDirectory, "weight");
        launch = readExplicitString(testCaseDirectory, "launch");
        expected = readExplicitString(testCaseDirectory, "expected");
        timelimit = readExplicitInt(testCaseDirectory, "timelimit");
        stderr = readExplicitBoolean(testCaseDirectory, "stderr");
        status = readExplicitBoolean(testCaseDirectory, "status");
        grading = new ArrayList<>();
        description = readExplicitString(testCaseDirectory, "description");
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
    public static TestCaseProperties loadYAML(String input)
        throws TestConfigurationError {
        ObjectMapper mapper = getMapper();
        try {
            TestCaseProperties result = mapper.readValue(input, 
                TestCaseProperties.class);
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
    public static TestCaseProperties loadYAML(File input)
        throws TestConfigurationError {
        ObjectMapper mapper = getMapper();
        try {
            TestCaseProperties result = mapper.readValue(input, 
                TestCaseProperties.class);
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

    
    private Optional<String> selectStringValue(
            Optional<String> explicitValue,
            Optional<String> suiteValue,
            Optional<String> caseValue,
            Optional<String> defaultValue) {
        if (explicitValue.isPresent()) {
            return explicitValue;
        } else if (caseValue.isPresent()) {
            return caseValue;
        } else if (suiteValue.isPresent()) {
            return suiteValue;
        } else {
            return defaultValue;
        }
    }

    private OptionalInt selectIntValue(
            OptionalInt explicitValue,
            OptionalInt suiteValue,
            OptionalInt caseValue,
            OptionalInt defaultValue) {
        if (explicitValue.isPresent()) {
            return explicitValue;
        } else if (caseValue.isPresent()) {
            return caseValue;
        } else if (suiteValue.isPresent()) {
            return suiteValue;
        } else {
            return defaultValue;
        }
    }

    private Optional<Boolean> selectBooleanValue(
            Optional<Boolean> explicitValue,
            Optional<Boolean> suiteValue,
            Optional<Boolean> caseValue,
            Optional<Boolean> defaultValue) {
        if (explicitValue.isPresent()) {
            return explicitValue;
        } else if (caseValue.isPresent()) {
            return caseValue;
        } else if (suiteValue.isPresent()) {
            return suiteValue;
        } else {
            return defaultValue;
        }
    }



    /**
     * The file to be sent to the standard input of a running test.
     * 
     * @return the input path
     */
    public File getIn() {
        return stdIn;
    }


    /**
     * The expected output from the program.
     * 
     * @return the input path
     */
    public String getExpected() {
        return expected.get();
    }

    /**
     * Should the contents of the standard error stream be appended to
     * the actual output?  Otherwise output sent by the program to the
     * standard error stream will be ignored.
     * 
     * @return true iff the standard error stream should be appended.
     */
    public boolean getStderr() {
        return stderr.get();
    }


    /**
     * Should the contents of the standard error stream be appended to
     * the actual output?  Otherwise output sent by the program to the
     * standard error stream will be ignored.
     * 
     * @param captureErr true iff the standard error stream should be appended.
     */
    public void setStderr(boolean captureErr) {
        stderr = Optional.of(captureErr);
    }

    /**
     * Should the status code of a submitted program be checked
     * to see if the program has crashed? Defaults to false; 
     * 
     * @return true iff the status code should be checked.
     */
    public boolean getStatus() {
        return status.get();
    }


    /**
     * @return command line parameters for running the test case.
     */
    public String getParams() {
        return params.get();
    }

    /**
     * @return how many points (weight) this test case is worth
     */
    public int getWeight() {
        return weight.getAsInt();
    }

    /**
     * @return the command string used to launch the program under evaluation.
     */
    public String getLaunch() {
        return launch.get();
    }

    /**
     * Change the command string used to launch the program under evaluation.
     * 
     * @param command string
     */
    public void setLaunch(String command) {
        launch = Optional.of(command);
    }

    /**
     * How many seconds to allow a test execution to run before concluding
     * that the program is hanging, caught in an infinite loop, or simply
     * unacceptably slow.
     * 
     * @return the time limit in seconds
     */
    public int getTimelimit() {
        return timelimit.getAsInt();
    }

    /**
     * 
     * @return the location of the test case information
     */
    public Path getTestCaseDirectory() {
        return testDirectory;
    }

    /**
     * 
     * @return the name of this test case
     */
    public String getName() {
        return name;
    }

    /**
     * @return Directory in which a submission will be compiled and tested.
     */

    public Path getStagingDirectory() {
        return assignment.getStagingDirectory();
    }

    /**
     * @return Where to place information about the points earned for
     *         passing tests.
     */
    public Path getRecordingDirectory() {
        return assignment.getRecordingDirectory();
    }

    /**
     * @return Collection of all test cases.
     */

    public Path getTestSuiteDirectory() {
        return assignment.getTestSuiteDirectory();
    }


    /**
     * Permits iteration over the grading options specified
     * for this test case.
     * @return iterable over OracleProperties
     */
    public Iterable<OracleProperties> getGradingOptions() {
        return grading;
    }

    /**
     * 
     * @return the assignment settings
     */
    public Assignment getAssignment() {
        return assignment;
    }


}
