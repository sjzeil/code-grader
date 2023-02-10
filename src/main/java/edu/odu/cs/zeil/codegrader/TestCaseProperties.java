package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class TestCaseProperties implements Comparable<TestCaseProperties> {

    @JsonIgnore
    private Path testDirectory;
    @JsonIgnore
    private Assignment assignment;

    /**
     * The file to supply to standard input when the program under
     * evaluation is run.
     */
    @JsonIgnore
    private File stdIn;

    // Properties obtained from TestCasePropertiesBase

    private static final int DEFAULT_RUN_TIMELIMIT = 5;


//CHECKSTYLE:OFF

    /**
     * Identifies this test case.
     */
    public String name;

    /**
     * A tag name used to determine when (if ever) a test case should
     * be performed.  Default is "test".
     * 
     * A test suite begins by activating all cases of kind "build".
     */
    public String kind;


    /**
     * The command line parameters to be supplies when executing the
     * program under evaluation.
    */
    public String params;

    /**
     * How many points this test case is worth? 
     */
    public int weight;

    /**
     * The command string used to launch the program under evaluation.
     */
    public String launch;

    /**
     * A string containing expected output when the program under
     * evaluation is run.
     */
    public String expected;

    /**
     * How many seconds to allow a test execution to run before concluding
     * that the program is hanging, caught in an infinite loop, or simply
     * unacceptably slow.
     */
    public int timelimit;

    /**
     * Should the standard error stream be captures and included in the
     * program output being evaluated?
     */
    public boolean stderr;

    /**
     * Should the status code be checked for evidence that a program
     * has crashed? Defaults to false because students aren't likely to
     * be all that careful about returning status codes.
     */
    public boolean status;


    /**
     * The grading criteria for this test case.
     */
    public List<OracleProperties> grading;

    /**
     * Description of this test case (for display in student's grade report)
     */
    public String description;


    /**
     * What tags should be activated if this case fails (score < 100)
     */
    public List<String> onFail;

    /**
     * What tags should be activated if this case succeeds (score == 100)
     */
    public List<String> onSuccess;

    /**
     * If this tag is active, then this case should fail, unrun, with
     *  score of zero.
     */
    public String failIf;

//CHECKSTYLE:ON



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
        TestSuiteProperties suiteProperties;
        try {
            File defaultsFile = FileUtils.findFile(
                    asst.getTestSuiteDirectory(),
                    ".yaml").get();
            suiteProperties = TestSuiteProperties.loadYAML(defaultsFile);
        } catch (NoSuchElementException ex) {
            suiteProperties = new TestSuiteProperties();
        }
        TestCasePropertiesBase suiteDefaults = suiteProperties.test;
        TestCasePropertiesBase caseProperties = new TestCasePropertiesBase();
        try {
            File yamlFile = FileUtils.findFile(testDirectory,
                    ".yaml").get();
            caseProperties = TestCasePropertiesBase.loadYAML(yamlFile);
        } catch (NoSuchElementException ex) {
            caseProperties = new TestCasePropertiesBase();
        }
        TestCasePropertiesBase defaults = TestCasePropertiesBase.defaults();
        TestCasePropertiesBase explicit 
            = new TestCasePropertiesBase(testDirectory);
        resolveDefaults(explicit, caseProperties, suiteDefaults, defaults);
    }


    private void resolveDefaults(
            TestCasePropertiesBase explicit,
            TestCasePropertiesBase tCase,
            TestCasePropertiesBase suite,
            TestCasePropertiesBase defaults) {
        kind = selectValue(explicit.kind, tCase.kind,
                suite.kind, defaults.kind, "_unknown_");
        params = selectValue(explicit.params, tCase.params,
                suite.params, defaults.params, "");
        weight = selectIntValue(explicit.weight, tCase.weight,
                suite.weight, defaults.weight, 1);
        launch = selectValue(explicit.launch, tCase.launch,
                suite.launch, defaults.launch, "");
        expected = selectValue(explicit.expected, tCase.expected,
            suite.expected, defaults.expected, "");
        timelimit = selectIntValue(explicit.timelimit, tCase.timelimit,
            suite.timelimit, defaults.timelimit, DEFAULT_RUN_TIMELIMIT);
        stderr = selectValue(explicit.stderr, tCase.stderr,
            suite.stderr, defaults.stderr, false);
        status = selectValue(explicit.status, tCase.status,
            suite.status, defaults.status, false);
        description = selectValue(explicit.description, tCase.description,
            suite.description, defaults.description, "");
        grading = selectValue(explicit.grading, tCase.grading,
            suite.grading, defaults.grading, new ArrayList<>());
        onFail = selectValue(explicit.onFail, tCase.onFail,
            suite.onFail, defaults.onFail, new ArrayList<>());
        onSuccess = selectValue(explicit.onSuccess, tCase.onSuccess,
            suite.onSuccess, defaults.onSuccess, new ArrayList<>());
        failIf = selectValue(explicit.failIf, tCase.failIf,
            suite.failIf, defaults.failIf, "");

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
        name = null;
        assignment = null;
        kind = null;
        params = null;
        weight = 1;
        launch = null;
        expected = null;
        timelimit = 1;
        stderr = false;
        status = false;
        grading = null;
        description = null;
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

    
    private <T> T selectValue(
        Optional<T> explicitValue,
        Optional<T> suiteValue,
        Optional<T> caseValue,
        Optional<T> defaultValue,
        T fallback) {
    if (explicitValue.isPresent()) {
        return explicitValue.get();
    } else if (caseValue.isPresent()) {
        return caseValue.get();
    } else if (suiteValue.isPresent()) {
        return suiteValue.get();
    } else if (defaultValue.isPresent()) {
        return defaultValue.get();
    } else {
        return fallback;
    }
}

    private int selectIntValue(
            OptionalInt explicitValue,
            OptionalInt suiteValue,
            OptionalInt caseValue,
            OptionalInt defaultValue,
            int fallback) {
        if (explicitValue.isPresent()) {
            return explicitValue.getAsInt();
        } else if (caseValue.isPresent()) {
            return caseValue.getAsInt();
        } else if (suiteValue.isPresent()) {
            return suiteValue.getAsInt();
        } else if (defaultValue.isPresent()) {
            return defaultValue.getAsInt();
        } else {
            return fallback;
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
        return expected;
    }



    /**
     * @return command line parameters for running the test case.
     */
    public String getParams() {
        return params;
    }




    /**
     * 
     * @return the location of the test case information
     */
    public Path getTestCaseDirectory() {
        return testDirectory;
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

    /**
     * Compare two TC properties by name.
     * @param right another TC properties
     * @return ordering of the names
     */
    public int compareTo(TestCaseProperties right) {
        return name.compareTo(right.name);
    }

    /**
     * Compare by name.
     * @param right another test cast property collection
     * @return true if the names match
     */
    public boolean equals(Object right) {
        if (right instanceof TestCaseProperties) {
            return name.equals(((TestCaseProperties) right).name);
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
