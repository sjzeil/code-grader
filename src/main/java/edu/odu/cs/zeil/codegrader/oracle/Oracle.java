package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.TestCase;

/**
 * Compares expected and actual outputs to determine if they match.
 * 
 * @author zeil
 *
 */
public abstract class Oracle {

	public enum ScoringOptions {All, ByLine, ByNonEmptyLine, ByToken}

	public static final String PassedTestMessage = "OK";

	private boolean ignoreCase;
    private ScoringOptions scoring;
    private double precision;
    private boolean ignoreWS;
	private boolean ignoreEmptyLines;
	private String command;
	private int cap;

	private TestCase testCase;
	private String testName;

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Oracle(String properties, TestCase testCase) {
		this.testCase = testCase;
		setProperties(properties);
	}

	/**
	 * Parse a properties specification line and set the Oracle properties
	 * accordingly.  Properties are written as a comma-separated
	 * list of assignments property=value.   Values may be quoted with "" or ''.
	 * 
	 * @param properties properties specification
	 */
	protected void setProperties (String properties) {
		// Defaults
		ignoreCase = false;
		scoring = ScoringOptions.All;
		precision = -1; // precision is based on the expected string
		ignoreWS = true;
		ignoreEmptyLines = true;
		command = "";
		cap = 100;

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

		for (String assignment: assignments) {
			int k = assignment.indexOf('=');
			if (k >= 0) {
				String propertyName = assignment.substring(0, k).toLowerCase();
				String valueStr = assignment.substring(k+1);
				if (propertyName.equals("ignorecase")) {
					ignoreCase = parseAsBoolean(valueStr, ignoreCase);
				} else if (propertyName.equals("scoring")) {
					valueStr = valueStr.toLowerCase();
					if (valueStr.equals("all"))
						scoring = ScoringOptions.All;
					else if (valueStr.equals("byline"))
						scoring = ScoringOptions.ByLine;
					else if (valueStr.equals("bytoken"))
						scoring = ScoringOptions.ByToken;
					else
						logger.warn("Could not parse scoring value:" +  valueStr);
				} else if (propertyName.equals("precision")) {
					try {
						precision = Double.parseDouble(valueStr);
					} catch (NumberFormatException ex) {
						logger.warn("Could not parse precision value:" +  valueStr);
					}
				} else if (propertyName.equals("ignorews")) {
					ignoreWS = parseAsBoolean(valueStr, ignoreWS);
				} else if (propertyName.equals("ignoreemptylines")) {
					ignoreEmptyLines = parseAsBoolean(valueStr, ignoreEmptyLines);
				} else if (propertyName.equals("command")) {
					command = testCase.parameterSubstitution(valueStr);
				} else if (propertyName.equals("cap")) {
					try {
						cap = Integer.parseInt(valueStr);
					} catch (NumberFormatException ex) {
						logger.warn("Could not parse cap value:" +  valueStr);
					}
				} else {
					logger.warn ("Did not recognize property name: " + propertyName);
				}
			} else {
				logger.warn ("Property not set with '=': " + assignment);
			}
		}
	}

	private boolean parseAsBoolean(String valueStr, boolean defaultValue) {
		valueStr = valueStr.toLowerCase();
		if (valueStr.equals("true") || valueStr.equals("t") || valueStr.equals("yes") || valueStr.equals("y") || valueStr.equals("1")) {
			return true;
		} else if (valueStr.equals("false") || valueStr.equals("f") || valueStr.equals("no") || valueStr.equals("n") || valueStr.equals("0")) {
			return false;
		} else {
			return defaultValue;
		}
	}

	/**
	 * Compare two strings to see if one is an acceptable variant of the other.
	 * The precise meaning of "acceptable" depends on the oracle and its settings.
	 * 
	 * @param expected  the expected string
	 * @param actual  the string being examined
	 * @return score and explanation 
	 */
	public abstract OracleResult compare(String expected, String actual);
	
	public boolean getIgnoreCase() {
		return ignoreCase;
	}

    public ScoringOptions getScoring() {
		return scoring;
	}

    public double getPrecision() {
		return precision;
	}

    public boolean getIgnoreWS() {
		return ignoreWS;
	}

	public boolean getIgnoreEmptyLines () {
		return ignoreEmptyLines;
	}

	public boolean getIgnorePunctuation() {
		//TODO
		return false;
	}


	public boolean getNumbersOnly() {
		//TODO
		return false;
	}

	public String getCommand () {
		return command;
	}

	public int getCap() {
		return cap;
	}



	public Oracle setIgnoreCase(boolean value) {
		ignoreCase = value;
		return this;
	}

    public Oracle setScoring(ScoringOptions value) {
		scoring = value;
		return this;
	}

    public Oracle setPrecision(double value) {
		precision = value;
		return this;
	}

    public Oracle setIgnoreWS(boolean value) {
		ignoreWS = value;
		return this;
	}

	public Oracle setIgnoreEmptyLines (boolean value) {
		ignoreEmptyLines = value;
		return this;
	}

	public  Oracle setCommand (String value) {
		command = value;
		return this;
	}

	public Oracle setCap(int value) {
		cap = value;
		return this;
	}

	public TestCase getTestCase() {
		return testCase;
	}


}
