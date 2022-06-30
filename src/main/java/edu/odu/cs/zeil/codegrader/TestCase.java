package edu.odu.cs.zeil.codegrader;


public class TestCase {

	private TestProperties properties;
	
		
    public TestCase(TestProperties testProperties) {
    	properties = testProperties;
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
    }

    /**
     * Return the captured output from a runTest call.
     * @return captured std out
     */
	public String getOutput() {
        return null;
    }

    /**
     * Return the captured error stream from a runTest call.
     * @return captured std err
     */
    public String getErr() {
        return null;
    }

    /**
     * Time in seconds of last runTest call.
     * @return time
     */
    public int getTime() {
        return null;
    }

    /**
     * Did the prior runTest call go too long?
     * @return true iff prior test was killed after exceeding time limit
     */
    public boolean timedOut() {
        return null;
    }

    /**
     * Did the prior test exit with a non-zero status code?
     * @return true iff prior test failed/crashed
     */
    public boolean crashed() {
        return null;
    }


}
