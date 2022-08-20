package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import edu.odu.cs.zeil.codegrader.oracle.Oracle;
import edu.odu.cs.zeil.codegrader.oracle.OracleFactory;
import edu.odu.cs.zeil.codegrader.oracle.OracleResult;

public class TestCase {

    /**
     * The collected properties for this test.
     */
    private TestCaseProperties properties;

    /**
     * Standard output from executing this test.
     */
    private String capturedOutput;

    /**
     * Standard error from executing this test.
     */
    private String capturedError;

    /**
     * True iff last test execution crashed (status code != 0).
     */
    private boolean crashed;

    /**
     * Status code of last execution.
     */
    private int statusCode;

    /**
     * True iff last test execution finished in an acceptable time.
     */
    private boolean onTime;
    
    /**
     * Time in seconds of last execution.
     */
    private int expiredTime;


    /**
     * Create a new test case.
     * 
     * @param testProperties properties for the test.
     */
    public TestCase(TestCaseProperties testProperties) {
        properties = testProperties;
        capturedOutput = "";
        capturedError = "";
        crashed = false;
        onTime = true;
        statusCode = 0;
        expiredTime  = -1;
    }


    /**
     * Runs the test case using the code in submission.
     * 
     * Standard out and standard err are captured and available as
     * getOutput() and getErr(). The status code is also available.
     * 
     * @param submission code to use when running the test case
     * @param stage staging area
     * @param buildStatus status code from build
     */
    public void executeTest(Submission submission, 
            Stage stage, int buildStatus) {
        String launchCommandStr = stage.getLaunchCommand(
                properties.getLaunch())  + ' '
                + properties.getParams();
        launchCommandStr = parameterSubstitution(launchCommandStr, 
            stage, submission);
        int timeLimit = properties.getTimelimit();
        if (timeLimit <= 0) {
            timeLimit = 1;
        }
        File stdIn = properties.getIn();
        ExternalProcess process = new ExternalProcess(
            stage.getStageDir(),
            launchCommandStr,
            timeLimit,
            stdIn, 
            "test case " + properties.getName());
        process.execute();
        capturedOutput = process.getOutput();
        capturedError = process.getErr();
        crashed = process.crashed();
        onTime = process.getOnTime();
        statusCode = process.getStatusCode();
        expiredTime  = process.getTime();

    }



    
    /**
     * Runs the test case and evaluates the results.
     * The actual output, score, and messages are recorded
     * in the recording directory.
     * 
     * @param submission to evaluate
     * @param asGold true if the gold version is being run, false if
     *                      student version is being run.
     * @param stage stage area in which code has been built
     * @param buildStatus status code from build 
     * @return test case score
     */
    public int performTest(Submission submission,
                boolean asGold,
                Stage stage, int buildStatus)
            throws TestConfigurationError {
        // Copy all test files into the stage.
        String testCaseName = properties.getName();
        executeTest(submission, stage, buildStatus);
        Path testRecordingDir = submission.getTestCaseDir(testCaseName);
        testRecordingDir.toFile().mkdirs();
        String testName = properties.getName();
        String outExtension = (asGold) ? ".expected" : ".out";
        String timeExtension = (asGold) ? ".timelimit" : ".time";
        FileUtils.writeTextFile(
            testRecordingDir.resolve(testName + outExtension), 
            getOutput());
        FileUtils.writeTextFile(
            testRecordingDir.resolve(testName + ".err"), 
            getErr());
        String time = "" + getTime() + "\n";
        FileUtils.writeTextFile(
            testRecordingDir.resolve(testName + timeExtension), 
            time);
        if (crashed()) {
            FileUtils.writeTextFile(
                testRecordingDir.resolve(testName + ".message"), 
                "***Program failed with status code " 
                    + statusCode + "\n");
            if (asGold) {
                throw new TestConfigurationError(
                    "Gold version crashed on test " + testName 
                    + ", status " + statusCode);
            } else {
                FileUtils.writeTextFile(
                    testRecordingDir.resolve(testName + ".score"), 
                    "0\n");
            }
            return 0;
        } else if (!onTime) {
            FileUtils.writeTextFile(
                testRecordingDir.resolve(testName + ".message"), 
                "***Program still running after " + properties.getTimelimit()
                + " seconds. Shut down.\n");
            if (asGold) {
                throw new TestConfigurationError(
                    "Gold version timed out on test " + testName 
                    + ", status " + statusCode);
            } else {
                FileUtils.writeTextFile(
                    testRecordingDir.resolve(testName + ".score"), 
                    "0\n");
            }
            return 0;
        } else if (!asGold) {
            int bestScore = -1;
            String firstMessage = "";
            for (OracleProperties option: properties.getGradingOptions()) {
                Oracle oracle = OracleFactory.getOracle(option, this);
                OracleResult evaluation = oracle.compare(
                    properties.getExpected(), 
                    getOutput());
                if (evaluation.score > bestScore) {
                    bestScore = evaluation.score;
                    if (!firstMessage.equals("")) {
                        firstMessage = evaluation.message;
                    }
                }
            }
            if (bestScore < 0) {
                // No options were explicitly specified. Fall back to default.
                Oracle oracle = OracleFactory.getOracle(
                    new OracleProperties(), this);
                OracleResult evaluation = oracle.compare(
                    properties.getExpected(), 
                    getOutput());
                float scoreScaling = ((float) oracle.getCap()) 
                    / ((float) OracleProperties.DEFAULT_POINT_CAP);
                evaluation.score = Math.round(
                    scoreScaling * (float) evaluation.score);
                bestScore = evaluation.score;
                firstMessage = evaluation.message;
            }
            FileUtils.writeTextFile(
                testRecordingDir.resolve(testName + ".score"), 
                "" + bestScore + "\n");
            FileUtils.writeTextFile(
                    testRecordingDir.resolve(testName + ".message"), 
                    firstMessage + "\n");
            return bestScore;
        } else {
            return -1;  // Gold code is not scored
        }
    }


    /**
     * Return the captured output from a runTest call.
     * 
     * @return captured std out
     */
    public String getOutput() {
        return capturedOutput;
    }

    /**
     * Return the captured error stream from a runTest call.
     * 
     * @return captured std err
     */
    public String getErr() {
        return capturedError;
    }

    /**
     * Time in seconds of last runTest call.
     * 
     * @return time
     */
    public int getTime() {
        return expiredTime;
    }

    /**
     * Did the prior runTest call go too long?
     * 
     * @return true iff prior test was killed after exceeding time limit
     */
    public boolean timedOut() {
        return !onTime;
    }

    /**
     * Did the prior test exit with a non-zero status code?
     * 
     * @return true iff prior test failed/crashed
     */
    public boolean crashed() {
        return crashed || (statusCode != 0);
    }

    /**
     * 
     * @return the properties for this test case.
     */
    public TestCaseProperties getProperties() {
        return properties;
    }

    /**
     * Scans a string for shortcuts, replacing by the appropriate string.
     * Shortcuts are
     * <ul>
     * <li>@P the test command line parameters</li>
     * <li>@S the staging directory</li>
     * <li>@T the test suite directory</li>
     * <li>@t the test case name</li>
     * <li>@R the reporting directory</li>
     * </ul>
     * A shortcut must be followed by a non-alphabetic character.
     * 
     * @param launchCommandStr a string describing a command to be run
     * @param stage the stage where the command will be executed
         * @param submission
     * @return the launchCommandStr with shortcuts replaced by the appropriate
     *         path/value
     */
    public String parameterSubstitution(
            String launchCommandStr, 
            Stage stage, 
            Submission submission) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < launchCommandStr.length()) {
            char c = launchCommandStr.charAt(i);
            if (c == '@') {
                if (i + 1 < launchCommandStr.length()) {
                    char c2 = launchCommandStr.charAt(i + 1);
                    if (c2 == 'P') {
                        boolean ok = (i + 2 >= launchCommandStr.length())
                                || !Character.isAlphabetic(
                                        launchCommandStr.charAt(i + 2));
                        if (ok) {
                            i += 2;
                            result.append(properties.getParams());
                        } else {
                            i += 1;
                            result.append(c);
                        }
                    } else {
                        i += 1;
                        result.append(c);
                    }
                } else {
                    result.append(c);
                    ++i;
                }
            } else {
                result.append(c);
                ++i;
            }
        }
        launchCommandStr = result.toString();
        result = new StringBuilder();
        i = 0;
        while (i < launchCommandStr.length()) {
            char c = launchCommandStr.charAt(i);
            if (c == '@') {
                if (i + 1 < launchCommandStr.length()) {
                    char c2 = launchCommandStr.charAt(i + 1);
                    if (c2 == 'S' || c2 == 'T' || c2 == 't' || c2 == 'R') {
                        boolean ok = (i + 2 >= launchCommandStr.length())
                                || !Character.isAlphabetic(
                                        launchCommandStr.charAt(i + 2));
                        if (ok) {
                            i += 2;
                            try {
                                if (c2 == 'S') {
                                    result.append(
                                            stage.getStageDir()
                                                    .toRealPath().toString());
                                } else if (c2 == 'T') {
                                    result.append(
                                            submission.getTestSuiteDir()
                                                    .toRealPath().toString());
                                } else if (c2 == 't') {
                                    result.append(properties.getName());
                                } else if (c2 == 'R') {
                                    result.append(
                                            submission.getRecordingDir()
                                                    .toRealPath().toString());
                                }
                            } catch (IOException ex) {
                                // Path has not been set
                                i -= 1;
                                result.append(c);
                            }
                        } else {
                            i += 1;
                            result.append(c);
                        }
                    } else {
                        i += 1;
                        result.append(c);
                    }
                } else {
                    result.append(c);
                    ++i;
                }
            } else {
                result.append(c);
                ++i;
            }
        }
        return result.toString();
    }




}
