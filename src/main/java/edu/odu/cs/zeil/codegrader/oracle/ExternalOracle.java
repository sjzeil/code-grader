package edu.odu.cs.zeil.codegrader.oracle;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.ExternalProcess;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.ParameterHandling;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;

/**
 * Oracle that works by running an external command.
 * 
 * @author zeil
 *
 */
public class ExternalOracle extends Oracle {

	
	private static final int OK_SCORE = 100;

	private static final int ORACLE_TIME_LIMIT = 30;

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
	public ExternalOracle(OracleProperties config,
			TestCase testCase, Submission sub, Stage submitterStage) {
		super(config, testCase, sub, submitterStage);
		properties = config;
	}

	/**
	 * Compare two strings to see if one is an acceptable variant of the other.
	 * The precise meaning of "acceptable" depends on the settings.
	 * 
	 * @param expected  the expected string
	 * @param actual  the string being examined
	 * @return true if actual is an acceptable variant of expected.
	 */
	@Override
	public OracleResult compare(String expected, String actual) {
		try {
			File expectedTmp = File.createTempFile("exp", ".txt");
			File observedTmp = File.createTempFile("obs", ".txt");
			FileUtils.writeTextFile(expectedTmp.toPath(), expected);
			FileUtils.writeTextFile(observedTmp.toPath(), actual);

			OracleResult result = executeOracle(expectedTmp, observedTmp);

			expectedTmp.delete();
			observedTmp.delete();

			return result;
		} catch (IOException ex) {
			throw new TestConfigurationError("Error running external oracle "
					+ properties.command + "\n" + ex.getMessage());
		}
	}

	/**
     * Runs the test case using the code in submission.
     * 
     * Standard out and standard err are captured and available as
     * getOutput() and getErr(). The status code is also available.
     * 
     * @param expected file with expected output
     * @param actual file with actual output
	 * @return result of executing the oracle
     */
    public OracleResult executeOracle(File expected, File actual) {
        String launchCommandStr = properties.command;
        TestCase tc = getTestCase();
        ParameterHandling subs = new ParameterHandling(
            tc.getProperties().getAssignment(), tc, getStage(),
            getSubmission(),
            expected, actual);
        launchCommandStr = subs.parameterSubstitution(launchCommandStr);
        logger.info("executeOracle using command: " + launchCommandStr);
        ExternalProcess process = new ExternalProcess(
            getSubmission().getRecordingDir(),
            launchCommandStr,
            ORACLE_TIME_LIMIT,
            null, 
            "oracle " + properties.command);
        process.execute(true);
        String capturedOutput = process.getOutput() + process.getErr();
        Optional<File> scoreFile = FileUtils
            .findFile(getSubmission().getRecordingDir(), ".score");
        if (scoreFile.isPresent()) {
            String scoreStr = FileUtils.readTextFile(scoreFile.get());
            if (scoreStr != null) {
                try {
                    int score = Integer.parseInt(scoreStr.trim());
                    return new OracleResult(score, capturedOutput);
                } catch (NumberFormatException ex) {
                    logger.warn("Could not parse score of " + scoreStr);
                }
            }
        }
        int statusCode = process.getStatusCode();
		if (statusCode >= 0 && statusCode <= OK_SCORE) {
			return new OracleResult(OK_SCORE - statusCode, capturedOutput);
		} else {
			return new OracleResult(0, capturedOutput);
		}
    }

	/**
     * Scans a string for shortcuts, replacing by the appropriate string.
     * Shortcuts are
     * <ul>
     * <li>@E the expected output file</li>
     * <li>@A the actual output file</li>
     * </ul>
     * A shortcut must be followed by a non-alphabetic character.
     * 
     * @param launchCommandStr a string describing a command to be run
     * @param expected file with expected output
     * @param actual file with actual output
     * @return the launchCommandStr with shortcuts replaced by the appropriate
     *         path/value
     */
    public String parameterSubstitution(
            String launchCommandStr, File expected, File actual) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < launchCommandStr.length()) {
            char c = launchCommandStr.charAt(i);
            if (c == '@') {
                if (i + 1 < launchCommandStr.length()) {
                    char c2 = launchCommandStr.charAt(i + 1);
                    if (c2 == 'S' || c2 == 'T' || c2 == 't' || c2 == 'R') {
                        boolean ok = (i + 2 >= launchCommandStr.length())
                                || !Character.isAlphabetic(
                                        launchCommandStr.charAt(i + 2));
                        if (ok) {
							i += 2;
							if (c2 == 'A') {
								result.append(actual.getAbsolutePath());
							} else if (c2 == 'E') {
								result.append(expected.getAbsolutePath());
							}
						} else {
                            i += 1;
                            result.append(c);
                        }
                    } else {
                        i += 1;
                        result.append(c);
                    }
                } else {
                    result.append(c);
                    ++i;
                }
            } else {
                result.append(c);
                ++i;
            }
        }
        return result.toString();
    }



}
