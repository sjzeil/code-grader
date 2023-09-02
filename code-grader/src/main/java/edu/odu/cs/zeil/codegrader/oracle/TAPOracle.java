package edu.odu.cs.zeil.codegrader.oracle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;

/**
 * An oracle that ignores the expected string and scans the observed string
 * for a TAP (Test Anything Protocol) JUnit 5 report.
 * 
 * E.g.,
 * <pre>
 * 1..8
 * ok 1 - EncyclopediaAddCourse
 * ok 2 - EncyclopediaAssign
 * not ok 3 - EncyclopediaConstructor
 * ok 4 - EncyclopediaCopy
 * ok 5 - EncyclopediaRead
 * not ok 6 - EncyclopediaRemoveCourse
 * ok 7 - ResearchPlanAddPrior
 * </pre>
 * 
 * The score is the ratio of OK tests to the number tests.
 * 
 * @author zeil
 *
 */
public class TAPOracle extends Oracle {


    /**
	 * Error logging.
	 */
	private static Logger logger = LoggerFactory.getLogger(
        MethodHandles.lookup().lookupClass());

    private static final double PERFECT_SCORE = 100.0;


	/**
	 * Create a new oracle.
	 * 
	 * @param config   configuration properties
	 * @param testCase the test case to which this oracle will apply
	 * @param submission the submission being judged
	 * @param submitterStage the stage where the submitted code has been built
	 */
	public TAPOracle(OracleProperties config, TestCase testCase, 
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
        try (BufferedReader observed 
            = new BufferedReader(new StringReader(actual))) {
            String line = observed.readLine();
            int nStarted = -1;
            int nSuccessful = 0;
            while (line != null) {
                nStarted = parseAsPlan(line);
                line = observed.readLine();
                if (nStarted > 0) {
                    break;
                }
            }
            while (line != null) {
                boolean ok = parseAsReport(line);
                if (ok) {
                    ++nSuccessful;
                }
                line = observed.readLine();
            }
            if (nStarted > 0 && nSuccessful >= 0) {
                double score = (PERFECT_SCORE
                        * (double) nSuccessful) / ((double) nStarted);
                return new OracleResult((int) Math.round(score), actual);
            } else {
                logger.warn("Oracle could not parse test report");
                return new OracleResult(0, "Could not parse test report.");    
            }
        } catch (IOException ex) {
            logger.warn("Oracle could not read test report", ex);
            return new OracleResult(0, "Could not read test report.");
        }
    }

    static final Pattern OK_LINE = Pattern.compile("^ok [0-9]+ - .*$");
        
	private boolean parseAsReport(String line) {
        Matcher matcher = OK_LINE.matcher(line);
        return matcher.matches();
    }

    private int parseAsPlan(String line) {
        final String prefix = "1..";
        if (line.startsWith(prefix)) {
            int pos = prefix.length();
            while (pos < line.length() && Character.isDigit(line.charAt(pos))) {
                ++pos;
            }
            if (pos > prefix.length()) {
                try {
                    int n = Integer.parseInt(
                        line.substring(prefix.length(), pos));
                    return n;
                } catch (NumberFormatException ex) {
                    return -1;
                }
            }
        }
        return -1;
    }


}
