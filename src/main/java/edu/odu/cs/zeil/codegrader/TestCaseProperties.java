package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseProperties {

    private Path testDirectory;
    private String name;

    private Assignment assignment;

    /**
     * Command line parameters for running the test case.
     */
    private String params;

    /**
     * How many points this test case is worth?
     */
    private int points;

    /**
     * The command string used to launch the program under evaluation.
     */
    private String launch;

    /**
     * The file to supply to standard input when the program under
     * evaluation is run.
     */
    private File stdIn;

    /**
     * A string containing expected output when the program under
     * evaluation is run.
     */
    private String expected;

    /**
     * Should the standard error stream be captures and included in the
     * program output being evaluated?
     */
    private boolean stderr;

    /**
     * How many seconds to allow a test execution to run before concluding
     * that the program is hanging, caught in an infinite loop, or simply
     * unacceptably slow.
     */
    private int timelimit;

    /**
     * The grading criteria for this test case.
     */
    private List<OracleProperties> grading;

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
        TestSuitePropertiesBase defaults;
        try {
            File defaultsFile = FileUtils.findFile(
                    asst.getTestSuiteDirectory(),
                    ".yaml").get();
            defaults = TestSuitePropertiesBase.loadYAML(defaultsFile);
        } catch (NoSuchElementException ex) {
            defaults = new TestSuitePropertiesBase();
        }
        TestCasePropertiesBase caseProperties = new TestCasePropertiesBase();
        try {
            File yamlFile = FileUtils.findFile(testDirectory,
                    ".yaml").get();
            caseProperties = TestCasePropertiesBase.loadYAML(yamlFile);
        } catch (NoSuchElementException ex) {
            defaults = new TestSuitePropertiesBase();
        }

        params = selectStringValue(defaults.test.params,
                caseProperties.params, "", "params");

        points = selectIntValue(defaults.test.points,
                caseProperties.points, 1, "points");

        launch = selectStringValue(defaults.test.launch,
                caseProperties.launch, "", "launch");

        expected = selectStringValue(defaults.test.expected,
                caseProperties.expected, "", "expected");

        timelimit = selectIntValue(defaults.test.timelimit,
                caseProperties.timelimit, 1, "timelimit");

        stderr = selectBooleanValue(defaults.test.stderr,
                caseProperties.stderr, false, "stdErr");

        if (caseProperties.grading.size() > 0) {
            grading = caseProperties.grading;
        } else {
            grading = defaults.test.grading;
        }

        try {
            stdIn = FileUtils.findFile(testDirectory, ".in").get();
        } catch (NoSuchElementException e) {
            stdIn = null;
        }
    }

    private String selectStringValue(Optional<String> suiteValue,
            Optional<String> caseValue, String defaultValue,
            String propertyName) {
        Optional<File> propertyFile = FileUtils.findFile(testDirectory,
                "." + propertyName);
        if (propertyFile.isPresent()) {
            return FileUtils.readTextFile(propertyFile.get()).trim();
        } else if (caseValue.isPresent()) {
            return caseValue.get();
        } else if (suiteValue.isPresent()) {
            return suiteValue.get();
        } else {
            return defaultValue;
        }
    }

    private int selectIntValue(OptionalInt suiteValue,
            OptionalInt caseValue, int defaultValue,
            String propertyName) {
        Optional<File> propertyFile = FileUtils.findFile(testDirectory,
                "." + propertyName);
        if (propertyFile.isPresent()) {
            String value = FileUtils.readTextFile(propertyFile.get()).trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                logger.warn("Could not parse '" + value + "' as an integer\n"
                        + "for " + propertyName + " in test case "
                        + name + ".\n"
                        + "Using " + defaultValue);
                return defaultValue;
            }
        } else if (caseValue.isPresent()) {
            return caseValue.getAsInt();
        } else if (suiteValue.isPresent()) {
            return suiteValue.getAsInt();
        } else {
            return defaultValue;
        }
    }

    private boolean selectBooleanValue(Optional<Boolean> suiteValue,
            Optional<Boolean> caseValue, boolean defaultValue,
            String propertyName) {
        Optional<File> propertyFile = FileUtils.findFile(testDirectory,
                "." + propertyName);
        if (propertyFile.isPresent()) {
            String value = FileUtils.readTextFile(propertyFile.get()).trim();
            try {
                return Boolean.parseBoolean(value);
            } catch (NumberFormatException ex) {
                logger.warn("Could not parse '" + value + "' as a boolean\n"
                        + "for " + propertyName + " in test case "
                        + name + ".\n"
                        + "Using " + defaultValue);
                return defaultValue;
            }
        } else if (caseValue.isPresent()) {
            return caseValue.get().booleanValue();
        } else if (suiteValue.isPresent()) {
            return suiteValue.get().booleanValue();
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
        return expected;
    }

    /**
     * Should the contents of the standard error stream be appended to
     * the actual output?  Otherwise output sent by the program to the
     * standard error stream will be ignored.
     * 
     * @return true iff the standard error stream should be appended.
     */
    public boolean getStderr() {
        return stderr;
    }


    /**
     * @return command line parameters for running the test case.
     */
    public String getParams() {
        return params;
    }

    /**
     * @return how many points this test case is worth
     */
    public int getPoints() {
        return points;
    }

    /**
     * @return the command string used to launch the program under evaluation.
     */
    public String getLaunch() {
        return launch;
    }

    /**
     * Change the command string used to launch the program under evaluation.
     * 
     * @param command string
     */
    public void setLaunch(String command) {
        launch = command;
    }

    /**
     * How many seconds to allow a test execution to run before concluding
     * that the program is hanging, caught in an infinite loop, or simply
     * unacceptably slow.
     * 
     * @return the time limit in seconds
     */
    public int getTimelimit() {
        return timelimit;
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

}
