package edu.odu.cs.zeil.codegrader.oracle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;

/**
 * An oracle that ignores the expected string and scans the observed string
 * for a JUnit 5 report, as generated by running junit-platform-console-*.jar.
 * 
 * E.g.,
 * <pre>
 * Test run finished after 64 ms
 * [         3 containers found      ]
 * [         0 containers skipped    ]
 * [         3 containers started    ]
 * [         0 containers aborted    ]
 * [         3 containers successful ]
 * [         0 containers failed     ]
 * [        10 tests found           ]
 * [         0 tests skipped         ]
 * [        10 tests started         ]
 * [         0 tests aborted         ]
 * [         6 tests successful      ]
 * [         4 tests failed          ]
 * </pre>
 * 
 * The score is the ratio of successful tests to started tests.
 * 
 * @author zeil
 *
 */
public class JUnit5Oracle extends Oracle {


    private static final String STARTED_STRING = "tests started";
    private static final String PASSED_STRING = "tests successful";

    private static final double PERFECT_SCORE = 100.0;

    /**
	 * Error logging.
	 */
	private static Logger logger = LoggerFactory.getLogger(
        MethodHandles.lookup().lookupClass());

	/**
	 * Create a new oracle.
	 * 
	 * @param config   configuration properties
	 * @param testCase the test case to which this oracle will apply
	 * @param submission the submission being judged
	 * @param submitterStage the stage where the submitted code has been built
	 */
	public JUnit5Oracle(OracleProperties config, TestCase testCase, 
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
            int nSuccessful = -1;
            while (line != null) {
                if (line.contains(STARTED_STRING)) {
                    nStarted = extractNumber(line);
                } else if (line.contains(PASSED_STRING)) {
                    nSuccessful = extractNumber(line);
                }
                if (nStarted >= 0 && nSuccessful >= 0) {
                    break;
                }
                line = observed.readLine();
            }
            if (nStarted > 0 && nSuccessful >= 0) {
                double score = (PERFECT_SCORE
                        * (double) nSuccessful) / ((double) nStarted);
                return new OracleResult((int) Math.round(score), actual);
            } else {
                logger.warn("Oracle could not parse test report");
                return new OracleResult(0, "Could not parse test report.\n(Code had compilation errors or crashed before tests could be run.)");    
            }
        } catch (IOException ex) {
            logger.warn("Oracle could not read test report", ex);
            return new OracleResult(0, "Could not read test report.\n(Tests were not run?)");
        }
    }

	private int extractNumber(String line) {
        java.util.Scanner in = new java.util.Scanner(line);
        int result = 0;
        while (in.hasNext() && !in.hasNextInt()) {
            in.next();
        }
        if (in.hasNextInt()) {
            result = in.nextInt();
        }
        in.close();
        return result;
    }


}
