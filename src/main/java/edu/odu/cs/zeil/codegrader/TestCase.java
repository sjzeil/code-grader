package edu.odu.cs.zeil.codegrader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCase {

    private TestProperties properties;
    private Path testCaseDir;
    private String capturedOutput;
    private String capturedError;
    private boolean crashed;
    private int statusCode;
    private boolean onTime;

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public TestCase(TestProperties testProperties) {
        properties = testProperties;
        capturedOutput = "";
        capturedError = "";
        crashed = false;
        onTime = true;
        statusCode = 0;
    }

    class StreamReader extends Thread {
        public boolean finished;
        public boolean forceStop;
        private StringBuilder contents;
        private BufferedReader in;
        private final int CaptureLimit = 2000000;

        public StreamReader(InputStream inputStream) {
            finished = false;
            forceStop = false;
            contents = new StringBuilder();
            in = new BufferedReader(new InputStreamReader(inputStream));
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
                    if (contents.length() > CaptureLimit) {
                        contents.append ("** Test output clipped after " + contents.length() + " bytes.\n");
                        logger.warn ("** Test output clipped after " + contents.length() + " bytes.");
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
     * @param submission code to use when running the testcase
     */
    public void executeTest(Submission submission) {
        List<String> launchCommand = parseCommand(properties.getLaunch() + ' ' + properties.getParams());
        ProcessBuilder pBuilder = new ProcessBuilder(launchCommand);
        pBuilder.directory(properties.getAssignment().getStagingDirectory().toFile());
        capturedOutput = "";
        capturedError = "";
        crashed = false;
        statusCode = 0;
        onTime = true;
        try {
            int timeLimit = properties.getTimelimit();
            if (timeLimit <= 0)
                timeLimit = 1;
            Path stdIn = properties.getIn();
            if (!stdIn.toFile().isDirectory()) {
                pBuilder.redirectInput(stdIn.toFile());
            }

            Process process = pBuilder.start();

            // If there is no standard in content, close the input stream
            if (stdIn.toFile().isDirectory()) {
                OutputStream stdInStr = process.getOutputStream();
                stdInStr.close();
            }
            StreamReader stdInReader = new StreamReader(process.getInputStream());
            stdInReader.start();
            StreamReader stdErrReader = new StreamReader(process.getErrorStream());
            stdErrReader.start();

            onTime = process.waitFor(timeLimit, TimeUnit.SECONDS);
            if (onTime) {
                for (int t = 0; t < 10; ++t) { // Wait up to 1 sec for readers to finish
                    if (stdInReader.finished && stdErrReader.finished)
                        break;
                    Thread.sleep(100); // wait .1 sec then check again
                }
                if (!stdInReader.finished) {
                    stdInReader.forceStop = true;
                    stdErrReader.forceStop = true;
                }
                Thread.sleep(100); // wait .1 sec after signalling for a finish
                if (!stdErrReader.finished)
                    stdErrReader.interrupt();
                if (!stdInReader.finished)
                    stdInReader.interrupt();
                capturedOutput = stdInReader.getContents();
                capturedError = stdErrReader.getContents();
                statusCode = process.exitValue();
            } else {
                process.destroy();
                logger.warn("Shutting down execution of test case " + properties.getName() + " due to time out.");
            }
        } catch (IOException ex) {
            logger.error("Could not launch test case " + properties.getName() + " with: " + properties.getLaunch(),
                    ex);
            crashed = true;
        } catch (InterruptedException e) {
            logger.error("Test case " + properties.getName() + " interrupted.", e);
            crashed = true;
        }
    }

    /**
     * Split a string into space-separated tokens, respecting "" and '' quoted
     * substrings
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

}
