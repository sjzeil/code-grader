package edu.odu.cs.zeil.codegrader;



/**
 * Launches external commands.
 */
public interface TCProcess {



    boolean IS_WINDOWS = System.getProperty("os.name")
        .contains("Windows");


    /**
     * Runs the external command.
     * 
     * Standard out and standard err are captured and available as
     * getOutput() and getErr(). The status code is also available.
     * 
     * @param quiet do not log failures to run program
     */
    void execute(boolean quiet);

    /**
     * Runs the external command.
     * 
     * Equivalent to execute(false);
     * 
     */
    void execute();

    /**
     * Return the captured output.
     * 
     * @return captured std out
     */
    String getOutput();

    /**
     * Return the captured error stream.
     * 
     * @return captured std err
     */
    String getErr();

    /**
     * Time in seconds of execution.
     * 
     * @return time
     */
    int getTime();

    /**
     * Did the prior execution go too long?
     * 
     * @return true iff execution was killed after exceeding time limit
     */
    boolean timedOut();

    /**
     * Did the prior execution exit with a non-zero status code?
     * 
     * @return true iff prior execution failed/crashed
     */
    boolean crashed();

    /**
     * @return true iff last test execution finished in an acceptable time.
     */
    boolean getOnTime();

    /**
     * @return status code of last execution (usually 0 if successful)
     */
    int getStatusCode();

    
}
