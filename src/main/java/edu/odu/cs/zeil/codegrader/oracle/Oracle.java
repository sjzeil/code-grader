package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.TestCase;

/**
 * Compares expected and actual outputs to determine if they match.
 * 
 * @author zeil
 *
 */
public abstract class Oracle {

	/**
	 * Max number of points scored by a test case.
	 */
	private static final int DEFAULT_POINT_CAP = 100;

	/**
	 * Possible scoring options for test cases.
	 */
	public enum ScoringOptions {
		/**
		 * Entire output must be correct. Only possible scores are 0 or 100.
		 */
		All, 
		/**
		 * Each line is checked individually. Score is #correct/#lines.
		 */
		ByLine, 
		/**
		 * Each word/token is checked individually. Score is
	     * #correct_tokens/#tokens. Should only be used if WS is being ignored.
		 */
		ByToken
	}

	/**
	 * What message will be recorded when a test passes with 100 pts.
	 */
	public static final String PASSED_TEST_MESSAGE = "OK";

	/**
	 * Ignore upper/lower case when examining alphabetic strings.
	 */
	private boolean ignoreCase;

	/**
	 * How is this case being scored.
	 */
	private ScoringOptions scoring;

	/**
	 * How far floating point numbers may vary and still be considered equal.
	 * If negative, the comparison precision is derived from the expected
	 * value. E.g., if the expected value is printed with 2 digits after the
	 * decimal point, then the precision is 0.01.
	 */
	private double precision;

	/**
	 * Should differences in whitespace be ignored? If the scoring is not
	 * ByLine, then line terminators are also ignored.
	 */
	private boolean ignoreWS;

	/**
	 * Should empty lines in the output be ignored.
	 */
	private boolean ignoreEmptyLines;

	/**
	 * Should differences in punctuation be ignored.
	 * Does not affect decimal points or + - in numbers.
	 */
	private boolean ignorePunctuation;

	/**
	 * Should anything other than numbers be treated as significant.
	 */
	private boolean numbersOnly;

	/**
	 * An external command to be run as the oracle.
	 */
	private String command;

	/**
	 * Maximum score possible. Defaults to 100.
	 */
	private int cap;

	/**
	 * The test case being examined.
	 */
	private TestCase testCase;

	/**
	 * Logger.
	 */
	private static Logger logger 
		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Oracle(
			final OracleProperties properties, 
			final TestCase theTestCase) {
		this.testCase = theTestCase;
		ignoreCase = properties.caseSig.isPresent() ? 
			!properties.caseSig.get(): false;
		scoring = properties.scoring.isPresent() ? 
			properties.scoring.get(): ScoringOptions.All;
		precision = properties.precision.isPresent() ? 
			properties.precision.getAsDouble() : -1; 
		ignoreWS = properties.ws.isPresent() ?
			!properties.ws.get() : true;
		ignoreEmptyLines = properties.emptylines.isPresent() ?
			!properties.emptylines.get() : true;
		ignorePunctuation = properties.punctuation.isPresent() ?
			!properties.punctuation.get() : false;
		numbersOnly = properties.numbersonly.isPresent() ?
			!properties.numbersonly.get() : false;
		command = properties.command.isPresent() ?
			properties.command.get() : "";
		cap = properties.cap.isPresent() ?
			properties.cap.getAsInt() : DEFAULT_POINT_CAP;
	}


	private boolean parseAsBoolean(
			final String valueString,
			final boolean defaultValue) {
		String valueStr = valueString.toLowerCase();
		if (valueStr.equals("true") || valueStr.equals("t")
				|| valueStr.equals("yes") || valueStr.equals("y")
				|| valueStr.equals("1")) {
			return true;
		} else if (valueStr.equals("false") || valueStr.equals("f") 
				|| valueStr.equals("no") || valueStr.equals("n")
				|| valueStr.equals("0")) {
			return false;
		} else {
			return defaultValue;
		}
	}

	/**
	 * Compare two strings to see if one is an acceptable variant of the other.
	 * The precise meaning of "acceptable" depends on the oracle and its
	 * settings.
	 * 
	 * @param expected the expected string
	 * @param actual   the string being examined
	 * @return score and explanation
	 */
	public abstract OracleResult compare(String expected, String actual);

	/**
	 * 
	 * @return true iff upper/lower case differences are ignored.
	 */
	public boolean getIgnoreCase() {
		return ignoreCase;
	}

	/**
	 * 
	 * @return scoring option for this test case.
	 */
	public ScoringOptions getScoring() {
		return scoring;
	}

	/**
	 * How far floating point numbers may vary and still be considered equal.
	 * If negative, the comparison precision is derived from the expected
	 * value. E.g., if the expected value is printed with 2 digits after the
	 * decimal point, then the precision is 0.01.
	 *
	 * @return precision used when comparing floating point numbers.
	 */
	public double getPrecision() {
		return precision;
	}

	/**
	 * 
	 * @return true iff differences in whitespace are being ignored.
	 */
	public boolean getIgnoreWS() {
		return ignoreWS;
	}

	/**
	 * 
	 * @return true iff empty lines are ignored.
	 */
	public boolean getIgnoreEmptyLines() {
		return ignoreEmptyLines;
	}

	/**
	 * 
	 * @return true iff punctuation is ignored.
	 */
	public boolean getIgnorePunctuation() {
		return ignorePunctuation;
	}

	/**
	 * 
	 * @return true if only numbers in the output are being checked.
	 */
	public boolean getNumbersOnly() {
		return numbersOnly;
	}

	/**
	 * 
	 * @return command string to launch an external oracle.
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * 
	 * @return maximum number of points that can be scored.
	 */
	public int getCap() {
		return cap;
	}

	/**
	 * 
	 * @return the test case being examined.
	 */
	public TestCase getTestCase() {
		return testCase;
	}

}
