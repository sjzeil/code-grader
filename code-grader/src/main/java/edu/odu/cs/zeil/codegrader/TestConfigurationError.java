package edu.odu.cs.zeil.codegrader;

/**
 * Indicates an error in the test configuration significant enough that
 * continuing to execute would likely result in inappropriate scores
 * being assigned.
 * 
 * This is derived from RuntimeException rather than Exception because this
 * is intended for situations where it is safest to stop the program. This
 * should probably only be caught at the "main()" level.
 */
public class TestConfigurationError extends RuntimeException {

    /**
     * Create an exception.
     * 
     * @param message explanation for the exception.
     */
    public TestConfigurationError(String message) {
        super(message);
    }

}
