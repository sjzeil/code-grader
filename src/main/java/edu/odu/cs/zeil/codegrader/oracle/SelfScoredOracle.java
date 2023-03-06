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
 * An oracle that scans the output for the score. 
 * 
 * Each line is matched against a pattern that must supply
 * a named capturing group "pts" and, optionally, a named capturing
 * group "poss".
 * 
 * If the pattern provides only pts, the match is considered to be the score.
 * If the pattern provides both pts and poss, the score is 100*pts/poss.
 * 
 * The patten is applied to each line of the out. It can match a substring
 * of that line. The last line of observed output containing a match
 * to the pattern is the one that is used.
 *  
 * @author zeil
 *
 */
public class SelfScoredOracle extends Oracle {


    private static final double PERFECT_SCORE = 100.0;

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
	public SelfScoredOracle(OracleProperties config, TestCase testCase, 
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

        Pattern pattern;
        try {
            pattern = Pattern.compile(getPattern());
        } catch (PatternSyntaxException ex) {
            throw new TestConfigurationError("Cannot parse \"" 
                + getPattern() + "\" as a Pattern.\n"
                + ex.getMessage());
        }
        String message = "";
        try (BufferedReader observed 
            = new BufferedReader(new StringReader(actual))) {
            String line = observed.readLine();
            int points = -1;
            int possible = -1;
            while (line != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    points = -1;
                    possible = -1;
                    String ptsStr = "";
                    try {
                        ptsStr = matcher.group("pts");
                    } catch (IllegalArgumentException ex) {
                        throw new TestConfigurationError("Oracle pattern "
                            + getPattern() + " does not capture <pts>.");
                    }
                    points = parseAsNumber(ptsStr);
                    if (points >= 0) {
                        message = line;
                    }
                    try {
                        String possStr = matcher.group("poss");
                        possible = parseAsNumber(possStr);
                    } catch (IllegalArgumentException ex) {
                        // Do nothing - this is optional.
                    }
                }
                line = observed.readLine();
            }
            if (points >= 0 && possible > 0) {
                double score = (PERFECT_SCORE
                        * (double) points) / ((double) possible);
                return new OracleResult((int) Math.round(score), message);
            } else if (points >= 0) {
                return new OracleResult(points, message);
            } else {
                LOG.warn("Oracle could not read test report");
                return new OracleResult(0, "Could not parse test report.");    
            }
        } catch (IOException ex) {
            LOG.warn("Oracle could not read test report", ex);
            return new OracleResult(0, "Could not read test report.");
        }
    }

    private int parseAsNumber(String ptsStr) {
        int points = -1;
        if (ptsStr.length() > 0) {
            try {
                double d = Double.parseDouble(ptsStr);
                points = (int) Math.round(d);
            } catch (NumberFormatException ex) {
                LOG.warn("Oracle pattern matched non-numeric " + ptsStr);
            }
        }
        return points;
    }


}
