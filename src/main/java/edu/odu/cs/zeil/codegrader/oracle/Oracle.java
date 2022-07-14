package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected Oracle(final String properties, final TestCase theTestCase) {
		this.testCase = theTestCase;
		setProperties(properties);
	}

	/**
	 * Parse a properties specification line and set the Oracle properties
	 * accordingly. Properties are written as a comma-separated
	 * list of assignments property=value. Values may be quoted with "" or ''.
	 * 
	 * @param properties properties specification
	 */
	protected void setProperties(final String properties) {
		// Defaults
		ignoreCase = false;
		scoring = ScoringOptions.All;
		precision = -1; // precision is based on the expected string
		ignoreWS = true;
		ignoreEmptyLines = true;
		ignorePunctuation = false;
		numbersOnly = false;
		command = "";
		cap = DEFAULT_POINT_CAP;

		ArrayList<String> assignments = new ArrayList<>();
		StringBuilder soFar = new StringBuilder();
		boolean inSQuote = false;
		boolean inDQuote = false;
		int i = 0;
		while (i < properties.length()) {
			char c = properties.charAt(i);
			if ((!inSQuote) && c == '"') {
				inDQuote = !inDQuote;
			} else if ((!inDQuote) && c == '\'') {
				inSQuote = !inSQuote;
			} else if ((!inDQuote) && (!inSQuote) && c == ',') {
				if (soFar.length() > 0) {
					assignments.add(soFar.toString());
					soFar = new StringBuilder();
				}
			} else {
				soFar.append(c);
			}
			++i;
		}
		if ((!inDQuote) && (!inSQuote) && soFar.length() > 0) {
			assignments.add(soFar.toString());
		}

		for (String assignment : assignments) {
			int k = assignment.indexOf('=');
			if (k >= 0) {
				String propertyName = assignment.substring(0, k).toLowerCase();
				String valueStr = assignment.substring(k + 1);
				if (propertyName.equals("case")) {
					ignoreCase = !parseAsBoolean(valueStr, ignoreCase);
				} else if (propertyName.equals("scoring")) {
					valueStr = valueStr.toLowerCase();
					if (valueStr.equals("all")) {
						scoring = ScoringOptions.All;
					} else if (valueStr.equals("byline")) {
						scoring = ScoringOptions.ByLine;
					} else if (valueStr.equals("bytoken")) {
						scoring = ScoringOptions.ByToken;
					} else {
						logger.warn("Could not parse scoring value:" 
							+ valueStr);
					}
				} else if (propertyName.equals("precision")) {
					try {
						precision = Double.parseDouble(valueStr);
					} catch (NumberFormatException ex) {
						logger.warn("Could not parse precision value:"
							+ valueStr);
					}
				} else if (propertyName.equals("ws")) {
					ignoreWS = !parseAsBoolean(valueStr, ignoreWS);
				} else if (propertyName.equals("emptylines")) {
					ignoreEmptyLines = !parseAsBoolean(valueStr,
						ignoreEmptyLines);
				} else if (propertyName.equals("punctuation")) {
					ignorePunctuation = !parseAsBoolean(valueStr,
						ignoreEmptyLines);
				} else if (propertyName.equals("numbersonly")) {
					numbersOnly = parseAsBoolean(valueStr, ignoreEmptyLines);
					ignoreWS = true;
				} else if (propertyName.equals("command")) {
					command = testCase.parameterSubstitution(valueStr);
				} else if (propertyName.equals("cap")) {
					try {
						cap = Integer.parseInt(valueStr);
					} catch (NumberFormatException ex) {
						logger.warn("Could not parse cap value:" + valueStr);
					}
				} else {
					logger.warn("Did not recognize property name: "
						+ propertyName);
				}
			} else {
				logger.warn("Property not set with '=': " + assignment);
			}
		}
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
