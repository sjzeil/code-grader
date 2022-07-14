package edu.odu.cs.zeil.codegrader;

/**
 * Indicates an error in the test configuration significant enough that
 * continuing to execute would likely result in inappropriate scores
 * being assigned.
 */
public class TestConfigurationError extends Exception {

    /**
     * Create an exception.
     * 
     * @param message explanation for the exception.
     */
    public TestConfigurationError(String message) {
        super(message);
    }

}
