package edu.odu.cs.zeil.codegrader;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooleanParser implements Parser<Boolean> {

    /**
     * Error logging.
     */
    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /**
     * Convert a string into an object of the desired type.
     * 
     * @param input input string
     * @return value of the desired type
     */
    public Boolean parse(String input) {
        String trimmed = input.trim().toLowerCase();
        Boolean result = null;
        if (trimmed.equals("true") || trimmed.equals("t")) {
            result = true;
        } else if (trimmed.equals("false") || trimmed.equals("f")) {
            result = false;
        } else {
            try {
                int v = Integer.parseInt(trimmed);
                result = (v != 0);
            } catch (NumberFormatException ex) {
                logger.warn("Cannot parse \"" + trimmed + "\" as a boolean.",
                     ex);
            }
        }
        return result;
    }
}
