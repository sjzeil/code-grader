package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;

/**
 * Oracle that checks to see if a set of files has been created.
 * 
 * @author zeil
 *
 */
public class FilesExistOracle extends Oracle {

	
	private static final int OK_SCORE = 100;

	/**
	 * Error logging.
	 */
	private static Logger logger = LoggerFactory.getLogger(
			MethodHandles.lookup().lookupClass());

	private OracleProperties properties;

	/**
	 * Create an oracle that launches an external command.
	 * 
	 * @param config properties
	 * @param testCase the test case on which it is applied
	 * @param sub submission being evaluated
     * @param submitterStage stage where submitter code has been built
	 */
	public FilesExistOracle(OracleProperties config,
			TestCase testCase, Submission sub, Stage submitterStage) {
		super(config, testCase, sub, submitterStage);
		properties = config;
	}

	/**
	 * Check to see if the files listed in the oracle properties
     * exist. Score is the percentage of those files that exist. 
	 * 
	 * @param expected  the expected string: ignored
	 * @param actual  the string being examined: ignored
	 * @return score based on how many of the requested files exist.
	 */
	@Override
	public OracleResult compare(String expected, String actual) {
        if (properties.files.size() == 0) {
            logger.error("No files specified for 'files' oracle.");
            return new OracleResult(0, "Configuration error");
        }
        int countExisting = 0;
        String message = "OK";
        for (String pathStr: properties.files) {
            pathStr = getTestCase().parameterSubstitution(pathStr,
                getStage(), getSubmission());
            Path desiredFile = getStage().getStageDir().resolve(pathStr);
            if (Files.exists(desiredFile)) {
                ++countExisting;
            } else {
                if (message.equals("OK")) {
                    message = "Did not find file(s): ";
                }
                message = message + pathStr + "; ";
            }
        }
        return new OracleResult(
            countExisting * OK_SCORE / properties.files.size(), message);
	}


}
