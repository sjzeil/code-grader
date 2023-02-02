package edu.odu.cs.zeil.codegrader.oracle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;

/**
 * An oracle that scores based upon whether the test case launch command
 * succeeded or failed, according to its status code. 
 *  
 * @author zeil
 *
 */
public class StatusOracle extends Oracle {


    private static final int PERFECT_SCORE = 100;

        /**
     * error logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());


	/**
	 * Create a new oracle.
	 * 
	 * @param config   configuration properties
	 * @param testCase the test case to which this oracle will apply
	 * @param submission the submission being judged
	 * @param submitterStage the stage where the submitted code has been built
	 */
	public StatusOracle(OracleProperties config, TestCase testCase, 
			Submission submission, Stage submitterStage) {
		super(config, testCase, submission, submitterStage);
	}

	/**
	 * Compare two strings to see if one is an acceptable variant of the other.
	 * The precise meaning of "acceptable" depends on the settings.
	 * 
	 * @param expected the expected string
	 * @param actual   the string being examined
	 * @return true if actual is an acceptable variant of expected.
	 */
    @Override
    public OracleResult compare(String expected, String actual) {
        String message = getTestCase().getOutput() + "\n--------\n"
            + getTestCase().getErr();
        if (getTestCase().crashed()) {
            return new OracleResult(0, message);
        } else {
            return new OracleResult(PERFECT_SCORE, message);
        }
    }


}
