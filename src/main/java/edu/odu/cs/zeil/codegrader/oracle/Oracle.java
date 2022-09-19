package edu.odu.cs.zeil.codegrader.oracle;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;

/**
 * Compares expected and actual outputs to determine if they match.
 * 
 * @author zeil
 *
 */
public abstract class Oracle {


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
	 * The submission being examined.
	 */
	private Submission submission;

	private Stage stage;

	protected Oracle(
			final OracleProperties properties, 
			final TestCase theTestCase,
			final Submission sub,
			final Stage submitterStage) {
		this.testCase = theTestCase;
		submission = sub;
		ignoreCase = !properties.caseSig;
		scoring = properties.scoring;
		precision = properties.precision;
		ignoreWS = !properties.ws;
		ignoreEmptyLines = !properties.emptyLines;
		ignorePunctuation = !properties.punctuation;
		numbersOnly = properties.numbersOnly;
		command = properties.command;
		cap = properties.cap;
		stage = submitterStage;
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


	/**
	 * @return the submission
	 */
	public Submission getSubmission() {
		return submission;
	}


	/**
	 * @return the stage
	 */
	public Stage getStage() {
		return stage;
	}

	

}
