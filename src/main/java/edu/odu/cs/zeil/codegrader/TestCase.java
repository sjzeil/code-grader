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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    /**
     * Threaded reader for accumulating standard output and standard error
     * from a running process.
     */
    static class StreamReader extends Thread {
        /**
         * True iff the thread has stopped due to the data stream being closed.
         */
        public boolean finished;

        /**
         * Setting this to true will force a stop after the next input is read.
         */
        public boolean forceStop;

        /**
         * Data accumulated so far.
         */
        private StringBuilder contents;

        /**
         * The reader attached to the process standard output/error.
         */
        private BufferedReader in;

        /**
         * Maximum number of characters that will be accumulated before deciding
         * that the process is stuck in a loop.
         */
        private static final int CAPTURE_LIMIT = 2000000;

        /**
         * Create the reader thread.
         * @param inputStream  Stream form which it will read.
         */
        StreamReader(InputStream inputStream) {
            finished = false;
            forceStop = false;
            contents = new StringBuilder();
            in = new BufferedReader(new InputStreamReader(inputStream,
                    Charset.forName("UTF-8")));
        }

        public String getContents() {
            return contents.toString();
        }

        public void run() {
            try {
                String line = in.readLine();
                while ((!forceStop) && (line != null)) {
                    contents.append(line);
                    contents.append("\n");
                    if (contents.length() > CAPTURE_LIMIT) {
                        contents.append("** Test output clipped after "
                                + contents.length() + " bytes.\n");
                        logger.warn("** Test output clipped after "
                                + contents.length() + " bytes.");
                        break;
                    }
                    line = in.readLine();
                }
                in.close();
                finished = true;
            } catch (IOException ex) {
                finished = true;
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
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
        launchCommandStr = parameterSubstitution(launchCommandStr);
        List<String> launchCommand = parseCommand(launchCommandStr);
        ProcessBuilder pBuilder = new ProcessBuilder(launchCommand);
        pBuilder.directory(properties.getStagingDirectory().toFile());
        capturedOutput = "";
        capturedError = "";
        crashed = false;
        statusCode = 0;
        onTime = true;
        try {
            int timeLimit = properties.getTimelimit();
            if (timeLimit <= 0) {
                timeLimit = 1;
            }
            File stdIn = properties.getIn();
            if (stdIn != null) {
                pBuilder.redirectInput(stdIn);
            }

            Process process = pBuilder.start();

            // If there is no standard in content, close the input stream
            if (stdIn != null) {
                OutputStream stdInStr = process.getOutputStream();
                stdInStr.close();
            }
            StreamReader stdInReader = new StreamReader(
                    process.getInputStream());
            stdInReader.start();
            StreamReader stdErrReader = new StreamReader(
                    process.getErrorStream());
            stdErrReader.start();

            onTime = process.waitFor(timeLimit, TimeUnit.SECONDS);
            if (onTime) {
                final int deciSeconds = 100;
                final int deciSecondsPerSecond = 10;
                for (int t = 0; t < deciSecondsPerSecond; ++t) { 
                    // Wait up to 1 sec for readers to finish
                    if (stdInReader.finished && stdErrReader.finished) {
                        break;
                    }
                    Thread.sleep(deciSeconds); // wait .1 sec then check again
                }
                if (!stdInReader.finished) {
                    stdInReader.forceStop = true;
                    stdErrReader.forceStop = true;
                }
                Thread.sleep(deciSeconds); // wait .1 sec after signaling
                                           // for a finish
                if (!stdErrReader.finished) {
                    stdErrReader.interrupt();
                }
                if (!stdInReader.finished) {
                    stdInReader.interrupt();
                }
                capturedOutput = stdInReader.getContents();
                capturedError = stdErrReader.getContents();
                statusCode = process.exitValue();
            } else {
                process.destroy();
                logger.warn("Shutting down execution of test case "
                        + properties.getName() + " due to time out.");
            }
        } catch (IOException ex) {
            logger.error("Could not launch test case " + properties.getName()
                    + " with: " + properties.getLaunch(),
                    ex);
            crashed = true;
        } catch (InterruptedException e) {
            logger.error("Test case " + properties.getName() + " interrupted.",
                    e);
            crashed = true;
        }
    }

    /**
     * Scans a string for shortcuts, replacing by the appropriate string.
     * Shortcuts are
     * <ul>
     * <li>@S the staging directory</li>
     * <li>@T the test suite directory</li>
     * <li>@t the test case name</li>
     * <li>@R the reporting directory</li>
     * </ul>
     * A shortcut must be followed by a non-alphabetic character.
     * 
     * @param launchCommandStr a string describing a command to be run
     * @return the launchCommandStr with shortcuts replaced by the appropriate
     *         path/value
     */
    public String parameterSubstitution(String launchCommandStr) {
        StringBuilder result = new StringBuilder();
        int i = 0;
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
                                            properties.getStagingDirectory()
                                                    .toRealPath().toString());
                                } else if (c2 == 'T') {
                                    result.append(
                                            properties.getTestSuiteDirectory()
                                                    .toRealPath().toString());
                                } else if (c2 == 't') {
                                    result.append(properties.getName());
                                } else if (c2 == 'R') {
                                    result.append(
                                            properties.getRecordingDirectory()
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

    /**
     * Runs the test case and evaluates the results.
     * The actual output, score, and messages are recorded
     * in the recording directory.
     * 
     * @param submission to evaluate
     * @return path to the recorded results
     */
    public Path performTest(Submission submission)
            throws TestConfigurationError {
        // TODO
        return null;
    }

    /**
     * Split a string into space-separated tokens, respecting "" and '' quoted
     * substrings.
     * 
     * @param launch a command line
     * @return tokenized versions of the command line
     */
    private List<String> parseCommand(String launch) {
        ArrayList<String> result = new ArrayList<>();
        StringBuffer token = new StringBuffer();
        boolean inQuotes1 = false;
        boolean inQuotes2 = false;
        for (int i = 0; i < launch.length(); ++i) {
            char c = launch.charAt(i);
            if (c != ' ') {
                token.append(c);
                if (c == '"') {
                    if (inQuotes2) {
                        inQuotes2 = false;
                    } else if (!inQuotes1) {
                        inQuotes2 = true;
                    }
                } else if (c == '\'') {
                    if (inQuotes1) {
                        inQuotes1 = false;
                    } else if (!inQuotes2) {
                        inQuotes1 = true;
                    }
                }
            } else {
                if (inQuotes1 || inQuotes2) {
                    token.append(c);
                } else {
                    result.add(token.toString());
                    token = new StringBuffer();
                }
            }
        }
        if (token.length() > 0) {
            result.add(token.toString());
        }
        return result;
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
        return 0;
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
