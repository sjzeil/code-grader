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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Launches external commands.
 */
public class ExternalProcess {

    private static final long ONE_SEC = 1000L;
    private static final long ONE_HALF_SEC = 500L;

    /**
     * Standard output from executing this process.
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

    private Path context;
    private String commandStr;
    private int maxSeconds;
    private File stdIn;
    private String processDescription;

    /**
     * Error logging.
     */
    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /**
     * Create a external process runner.
     * 
     * @param cwd working directory in which to run the process
     * @param commandLine the command to run
     * @param timeLimit max time in seconds to allow this to run
     * @param stdInFile file to supply as standard input. Null if no input
     *           is desired.
     * @param description description of the process (used only in error
     *           messages and log entries). If "", uses commandLine.
     */
    public ExternalProcess(Path cwd, String commandLine, 
            int timeLimit, File stdInFile, String description) {
        context = cwd;
        commandStr = commandLine;
        maxSeconds = timeLimit;
        stdIn = stdInFile;
        processDescription = description;
        if (processDescription.equals("")) {
            processDescription = commandLine;
        }
        capturedOutput = "";
        capturedError = "";
        crashed = false;
        onTime = true;
        statusCode = 0;
        expiredTime  = -1;
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
                } else if (token.length() != 0) {
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
     * Threaded reader for accumulating standard output and standard error
     * from a running process.
     */
    static class StreamReader extends Thread {

        /**
         * True iff the thread has stopped due to the data stream being closed.
         */
        private boolean finished;

        /**
         * @return true iff the thread has stopped due to the data stream 
         *      being closed.
         */
        public boolean isFinished() {
            return finished;
        }

        /**
         * Force a stop after the next input is read.
         */
        public void setForceStop() {
            this.forceStop = true;
        }

        /**
         * Setting this to true will force a stop after the next input is read.
         */
        private boolean forceStop;

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
     * Runs the external command.
     * 
     * Standard out and standard err are captured and available as
     * getOutput() and getErr(). The status code is also available.
     * 
     */
    public void execute() {
        List<String> launchCommand = parseCommand(commandStr);
        ProcessBuilder pBuilder = new ProcessBuilder(launchCommand);
        pBuilder.directory(context.toFile());
        capturedOutput = "";
        capturedError = "";
        crashed = false;
        statusCode = 0;
        onTime = true;
        try {
            int timeLimit = maxSeconds;
            if (timeLimit <= 0) {
                timeLimit = 1;
            }
            if (stdIn != null && !stdIn.toString().equals("")) {
                pBuilder.redirectInput(stdIn);
            }

            Instant startTime = Instant.now();
            Process process = pBuilder.start();

            // If there is no standard in content, close the input stream
            if (stdIn == null || stdIn.toString().equals("")) {
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
            Instant stopTime = Instant.now();
            long elapsed = Duration.between(startTime, stopTime).toMillis();
            expiredTime = (int) 
                ((elapsed + ONE_HALF_SEC) / ONE_SEC); // round to closest 
                                                      // sec.
            
            if (onTime) {
                final int tenthSeconds = 100;
                final int tenthSecondsPerSecond = 10;
                for (int t = 0; t < tenthSecondsPerSecond; ++t) { 
                    // Wait up to 1 sec for readers to finish
                    if (stdInReader.finished && stdErrReader.finished) {
                        break;
                    }
                    Thread.sleep(tenthSeconds); // wait .1 sec then check again
                }
                if (!stdInReader.finished) {
                    stdInReader.forceStop = true;
                    stdErrReader.forceStop = true;
                }
                Thread.sleep(tenthSeconds); // wait .1 sec after signaling
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
                logger.warn("Shutting down execution of "
                        + processDescription + " due to time out.");
            }
        } catch (IOException ex) {
            logger.warn("Could not launch " + processDescription
                    + " with: " + commandStr);
            crashed = true;
            statusCode = -1;
        } catch (InterruptedException e) {
            logger.warn(processDescription + " interrupted.");
            crashed = true;
            statusCode = -2;
        }
    }


    /**
     * Return the captured output.
     * 
     * @return captured std out
     */
    public String getOutput() {
        return capturedOutput;
    }

    /**
     * Return the captured error stream.
     * 
     * @return captured std err
     */
    public String getErr() {
        return capturedError;
    }

    /**
     * Time in seconds of execution.
     * 
     * @return time
     */
    public int getTime() {
        return expiredTime;
    }

    /**
     * Did the prior execution go too long?
     * 
     * @return true iff execution was killed after exceeding time limit
     */
    public boolean timedOut() {
        return !onTime;
    }

    /**
     * Did the prior execution exit with a non-zero status code?
     * 
     * @return true iff prior execution failed/crashed
     */
    public boolean crashed() {
        return crashed || (statusCode != 0);
    }

    /**
     * @return true iff last test execution finished in an acceptable time.
     */
    public boolean getOnTime() {
        return onTime;
    }

    /**
     * @return status code of last execution (usually 0 if successful)
     */
    public int getStatusCode() {
        return statusCode;
    }

    
}
