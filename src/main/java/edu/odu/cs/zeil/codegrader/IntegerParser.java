package edu.odu.cs.zeil.codegrader;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerParser implements Parser<Integer> {

	/**
	 * Error logging.
	 */
	private static Logger logger = LoggerFactory.getLogger(
			MethodHandles.lookup().lookupClass());

            /**
     * Convert a string into an object of the desired type.
     * @param input input string
     * @return value of the desired type
     */
    public Integer parse(String input) {
        String trimmed = input.trim();
        try {
            Integer result = Integer.parseInt(trimmed);
            return result;
        } catch (NumberFormatException ex) {
            logger.warn("Cannot parse \"" + trimmed + "\" as an integer.", ex);
            return null;
        }
    }
}
