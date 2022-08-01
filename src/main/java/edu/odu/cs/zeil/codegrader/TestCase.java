package edu.odu.cs.zeil.codegrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Error logging.
     */
    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

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
     */
    public void executeTest(Submission submission) {
        String launchCommandStr = properties.getLaunch() + ' '
                + properties.getParams();
        Assignment asst = properties.getAssignment();
        launchCommandStr = asst.parameterSubstitution(launchCommandStr, 
            properties.getName());
        int timeLimit = properties.getTimelimit();
        if (timeLimit <= 0) {
            timeLimit = 1;
        }
        File stdIn = properties.getIn();
        ExternalProcess process = new ExternalProcess(
            properties.getStagingDirectory(),
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
     * @return path to the recorded results
     */
    public Path performTest(Submission submission, boolean asGold)
            throws TestConfigurationError {
        executeTest(submission);
        Path testRecordingDir = properties.getRecordingDirectory()
            .resolve(submission.getSubmittedBy())
            .resolve(properties.getName());
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
                "***Program crashed with status code " 
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
        }
        return testRecordingDir;
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

}
