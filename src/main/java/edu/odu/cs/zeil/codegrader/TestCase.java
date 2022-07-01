package edu.odu.cs.zeil.codegrader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
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

    /**
     * Runs the test case using the code in submission.
     * 
     * Standard out and standard err are captured and available as
     * getOutput() and getErr().  The status code is also available.
     * 
     * @param submission code to use when running the testcase
     */
    public void runTest(Submission submission) {
        List<String> launchCommand = parseCommand(properties.getLaunch() + ' ' + properties.getParams());
        ProcessBuilder pBuilder = new ProcessBuilder(launchCommand);
        capturedOutput = "";
        capturedError = "";
        crashed = false;
        statusCode = 0;
        onTime = true;
        try {
            Process process = pBuilder.start();
            onTime = process.waitFor(properties.getTimelimit(), TimeUnit.SECONDS);
            if (onTime) {
                capturedOutput = new String(process.getInputStream().readAllBytes());
                capturedError = new String(process.getErrorStream().readAllBytes());
                process.getInputStream().close();
                process.getErrorStream().close();
                statusCode = process.exitValue();
            } else {
                process.destroy();
                logger.warn("Shutting down execution of test case " + properties.getName() + " due to time out.");
            }
        } catch (IOException ex) {
            logger.error ("Could not launch test case " + properties.getName() + " with: " + properties.getLaunch(), ex);
            crashed = true;
        } catch (InterruptedException e) {
            logger.error ("Test case " + properties.getName() + " interrupted.", e);
            crashed = true;
        }
    }


    /**
     * Split a string into space-separated tokens, respecting "" and '' quoted substrings
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
            result.add (token.toString());
        }
        return result;
    }

    /**
     * Return the captured output from a runTest call.
     * @return captured std out
     */
	public String getOutput() {
        return capturedOutput;
    }

    /**
     * Return the captured error stream from a runTest call.
     * @return captured std err
     */
    public String getErr() {
        return capturedError;
    }

    /**
     * Time in seconds of last runTest call.
     * @return time
     */
    public int getTime() {
        return 0;
    }

    /**
     * Did the prior runTest call go too long?
     * @return true iff prior test was killed after exceeding time limit
     */
    public boolean timedOut() {
        return !onTime;
    }

    /**
     * Did the prior test exit with a non-zero status code?
     * @return true iff prior test failed/crashed
     */
    public boolean crashed() {
        return crashed || (statusCode != 0);
    }


}
