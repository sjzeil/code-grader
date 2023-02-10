package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.oracle.Oracle;
import edu.odu.cs.zeil.codegrader.oracle.OracleFactory;
import edu.odu.cs.zeil.codegrader.oracle.OracleResult;

public class TestCase {

    private static final String BUILD_KIND = "build";

    private static final int MIN_RUNTIME_LIMIT = 2;

    private static final int INSTRUCTORS_TIME_MULTIPLIER = 4;

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
     * @param stage staging area
     */
    public void executeTest(Submission submission, 
            Stage stage) {
        String launch = properties.launch;
        if (launch == null || launch.equals("")) {
            if (properties.kind.equals(BUILD_KIND)) {
                launch = stage.getBuildCommand();
            } else {
                launch = stage.getLaunchCommand(properties.launch);
            }
        }
        if (!launch.equals("")) {
            if (launch.charAt(0) != '@') {
                // Normal case: launch an external process
                executeExternalTestCommand(submission, stage, launch);
            } else {
                // Launch an internal process 
                executeInternalTestCommand(submission, stage, launch);
            }
        }
    }

    private void executeInternalTestCommand(Submission submission,
            Stage stage, String launch) {
        String paramString = properties.getParams();
        ParameterHandling subs = new ParameterHandling(
            properties.getAssignment(), this, stage, submission, null, null);
        paramString = subs.parameterSubstitution(paramString);

        try {
            Class<?> launcherClassName = Class.forName(launch.substring(1));
            Class<?>[] argTypes = {TestCaseProperties.class, Stage.class};
            Object[] args = {properties, stage };

            Constructor<?> constructor = launcherClassName.getConstructor(
                    argTypes);
            InternalTestLauncher launcher = (InternalTestLauncher)
                constructor.newInstance(args);
            runTCProcess(launcher);
        } catch (ClassNotFoundException e) {
            logger.error("invalid process class name: " + launch);
        } catch (NoSuchMethodException | SecurityException
                | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            logger.error("Could not instantiate: " + launch.substring(1), e);
        }
    }

    private void executeExternalTestCommand(Submission submission,
            Stage stage, String launch) {
        String launchCommandStr = stage.getLaunchCommand(launch)  + ' '
                + properties.getParams();
        ParameterHandling subs = new ParameterHandling(
            properties.getAssignment(), this, stage, submission, null, null);
        launchCommandStr = subs.parameterSubstitution(launchCommandStr);
        logger.info("executeTest using command: " + launchCommandStr);
        int timeLimit = Math.max(getTimeLimit(submission), MIN_RUNTIME_LIMIT);
        File stdIn = properties.getIn();
        logger.info(stage.getStageDir().toString() + " " + timeLimit);
        ExternalProcess process = new ExternalProcess(
            stage.getStageDir(),
            launchCommandStr,
            timeLimit,
            stdIn, 
            "test case " + properties.name);
        runTCProcess(process);
    }


    private void runTCProcess(TCProcess process) {
        process.execute(true);
        capturedOutput = process.getOutput();
        capturedError = process.getErr();
        if (properties.stderr) {
            capturedOutput = capturedOutput + "\n--- std err---\n" 
                + capturedError;
            capturedError = "";
        }
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
     * @return test case score
     */
    public int performTest(Submission submission,
                boolean asGold,
                Stage stage)
            throws TestConfigurationError {
        // Copy all test files into the stage.
        String testCaseName = properties.name;
        executeTest(submission, stage);
        Path testRecordingDir = submission.getTestCaseDir(testCaseName);
        if (!testRecordingDir.toFile().exists()) {
            boolean ok = testRecordingDir.toFile().mkdirs();
            if (!ok) {
                logger.warn("Unable to create directory " + testRecordingDir);
            }
        }
        String testName = properties.name;
        String outExtension = (asGold) ? ".expected" : ".out";
        String timeExtension = (asGold) ? ".timelimit" : ".time";
        FileUtils.writeTextFile(
            testRecordingDir.resolve(testName + outExtension), 
            getOutput());
        FileUtils.writeTextFile(
            testRecordingDir.resolve(testName + ".err"), 
            getErr());
        int time0 = getTime();
        if (asGold) {
            time0 = Math.max(MIN_RUNTIME_LIMIT, 
                INSTRUCTORS_TIME_MULTIPLIER * time0);
        }
        String time = "" + time0 + "\n";
        if ((!asGold) && (!properties.kind.equals(BUILD_KIND))) {
            FileUtils.writeTextFile(
                testRecordingDir.resolve(testName + timeExtension), 
                time);
        }
        if (properties.status && crashed()) {
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
                "***Program still running after " + getTimeLimit(submission)
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
            String actualOutput = getOutput();
            if (properties.stderr) {
                String errorOut = getErr();
                if (!errorOut.equals("")) {
                    actualOutput = actualOutput + "\non std err:\n" + errorOut;
                }
            }
            for (OracleProperties option: properties.getGradingOptions()) {
                Oracle oracle = OracleFactory.getOracle(option,
                    this, submission, stage);
                OracleResult evaluation = oracle.compare(
                    getExpected(submission),
                    actualOutput);
                if (evaluation.score > bestScore) {
                    bestScore = evaluation.score;
                    if (firstMessage.equals("")) {
                        firstMessage = evaluation.message;
                    }
                }
            }
            if (bestScore < 0) {
                // No options were explicitly specified. Fall back to default.
                Oracle oracle = OracleFactory.getOracle(
                    new OracleProperties(), this, submission, stage);
                OracleResult evaluation = oracle.compare(
                    getExpected(submission), 
                    actualOutput);
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
     * Fail a test without running it.
     * 
     * @param submission to evaluate
     * @param message explanation of failure
     * @return test case score
     */
    public int failTest(Submission submission,
                String message)
            throws TestConfigurationError {
        // Copy all test files into the stage.
        String testCaseName = properties.name;
        
        Path testRecordingDir = submission.getTestCaseDir(testCaseName);
        if (!testRecordingDir.toFile().exists()) {
            boolean ok = testRecordingDir.toFile().mkdirs();
            if (!ok) {
                logger.warn("Unable to create directory " + testRecordingDir);
            }
        }
        String testName = properties.name;
        String outExtension = ".out";
        String timeExtension = ".time";
        FileUtils.writeTextFile(
            testRecordingDir.resolve(testName + outExtension), 
            message);
        FileUtils.writeTextFile(
            testRecordingDir.resolve(testName + ".err"), 
            "");
        int time0 = 0;
        String time = "" + time0 + "\n";
        if (!properties.kind.equals(BUILD_KIND)) {
            FileUtils.writeTextFile(
                testRecordingDir.resolve(testName + timeExtension), 
                time);
        }
        FileUtils.writeTextFile(
            testRecordingDir.resolve(testName + ".score"), 
            "0\n");
        return 0;
    }

    private String getExpected(Submission submission) {
        Optional<File> expectedFile 
            = FileUtils.findFile(submission.getTestCaseDir(
                properties.name), 
                ".expected");
        if (expectedFile.isPresent()) {
            return FileUtils.readTextFile(expectedFile.get());
        } else {
            return properties.getExpected();
        }
    }

    private int getTimeLimit(Submission submission) {
        if (submission == null) {
            return properties.timelimit;
        } else {
            Optional<File> limitFile = FileUtils.findFile(
                    submission.getTestCaseDir(properties.name),
                    ".timelimit");
            if (limitFile.isPresent()) {
                String text = FileUtils.readTextFile(limitFile.get());
                try {
                    return Integer.parseInt(text.trim());
                } catch (NumberFormatException ex) {
                    return properties.timelimit;
                }
            } else {
                return properties.timelimit;
            }
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
                                    result.append(properties.name);
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
