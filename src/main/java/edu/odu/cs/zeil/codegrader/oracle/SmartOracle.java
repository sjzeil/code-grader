package edu.odu.cs.zeil.codegrader.oracle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.TestCase;

/**
 * Compares expected and actual outputs to determine if they match.
 * 
 * @author zeil
 *
 */
public class SmartOracle extends Oracle {

	private static final Pattern WINDOWS_LINE_ENDINGS = Pattern.compile("\r\n");
	private static final Pattern MULTI_LF = Pattern.compile("\n\n+");
	private static final Pattern MULTI_WS = Pattern.compile("[ \t\r\f]+");
	private static final Pattern MULTI_WS_LF = Pattern.compile("[ \t\r\n\f]+");

	/**
	 * Create a new smart oracle.
	 * 
	 * @param config   configuration properties
	 * @param testCase the test case to which this oracle will apply
	 */
	public SmartOracle(OracleProperties config, TestCase testCase) {
		super(config, testCase);
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
		expected = preprocess(expected);
		actual = preprocess(actual);

		int tokenCount = 0;
		int correctTokens = 0;
		int correctLines = 0;
		String message = Oracle.PASSED_TEST_MESSAGE;
		boolean noFailuresSoFar = true;

		String[] expectedLines = expected.split("\n");
		String[] actualLines = actual.split("\n");
		int lineCount = Math.min(expectedLines.length, actualLines.length);

		for (int lineNum = 0; lineNum < lineCount; ++lineNum) {
			String expectedLine = expectedLines[lineNum];
			String actualLine = actualLines[lineNum];
			boolean lineOK = true;

			Scanner expectedTokens = new Scanner(expectedLine, this);
			Scanner actualTokens = new Scanner(actualLine, this);
			while (expectedTokens.hasNext() && actualTokens.hasNext()) {
				Token expectedToken = expectedTokens.next();
				Token actualToken = actualTokens.next();
				if (!expectedToken.equals(actualToken)) {
					lineOK = false;
					if (noFailuresSoFar) {
						message = "expected: " + expectedToken
								+ "\nobserved: " + actualToken;
						noFailuresSoFar = false;
					}
				} else {
					if (!(expectedToken instanceof WhiteSpaceToken)
							|| !getIgnoreWS()) {
						++correctTokens;
					}
				}
				if (!(expectedToken instanceof WhiteSpaceToken)
						|| !getIgnoreWS()) {
					++tokenCount;
				}
			}
			if (expectedTokens.hasNext()) {
				lineOK = false;
				if (noFailuresSoFar) {
					message = "Actual output ended early.";
					noFailuresSoFar = false;
					while ((expectedTokens.hasNext())) {
						Token expectedToken = expectedTokens.next();
						if (!(expectedToken instanceof WhiteSpaceToken)
								|| !getIgnoreWS()) {
							++tokenCount;
						}
					}
				}
			} else if (actualTokens.hasNext()) {
				lineOK = false;
				if (noFailuresSoFar) {
					message = "Actual output is too long.";
					noFailuresSoFar = false;
					while ((actualTokens.hasNext())) {
						Token actualToken = actualTokens.next();
						if (!(actualToken instanceof WhiteSpaceToken)
							|| !getIgnoreWS()) {
							++tokenCount;
							}
					}
				}
			}
			if (lineOK) {
				++correctLines;
			}
		}
		if (expectedLines.length > actualLines.length) {
			if (noFailuresSoFar) {
				message = "Actual output ends too soon.";
				noFailuresSoFar = false;
				lineCount += expectedLines.length - actualLines.length;
			}
		} else if (expectedLines.length < actualLines.length) {
			if (noFailuresSoFar) {
				message = "Actual output has too many lines.";
				noFailuresSoFar = false;
				lineCount += actualLines.length - expectedLines.length;
			}
		}
		if (getScoring() == ScoringOptions.All) {
			return new OracleResult((
				(noFailuresSoFar) ? getCap() : 0), message);
		} else if (getScoring() == ScoringOptions.ByLine) {
			double score = ((double) correctLines) / ((double) lineCount);
			score *= (double) getCap();
			int iScore = (int) Math.round(score);
			return new OracleResult(iScore, message);
		} else { // ByToken
			double score = ((double) correctTokens) / ((double) tokenCount);
			score *= (double) getCap();
			int iScore = (int) Math.round(score);
			return new OracleResult(iScore, message);
		}
	}

	private String preprocess(String str) {
		Matcher leMatcher = WINDOWS_LINE_ENDINGS.matcher(str);
		String result = leMatcher.replaceAll("\n");
		if (getNumbersOnly()) {
			StringBuilder numbersTokens = new StringBuilder();
			boolean first = true;
			Scanner scanner = new Scanner(result, this);
			while (scanner.hasNext()) {
				Token token = scanner.next();
				if (token instanceof NumberToken) {
					if (!first) {
						numbersTokens.append(' ');
					}
					numbersTokens.append(token.getLexeme());
				}
			}
			result = numbersTokens.toString();
		} else {
			if (getIgnorePunctuation()) {
				StringBuilder retainedTokens = new StringBuilder();
				Scanner scanner = new Scanner(result, this);
				while (scanner.hasNext()) {
					Token token = scanner.next();
					if ((token instanceof PunctuationToken)) {
						retainedTokens.append(' ');
					} else {
						retainedTokens.append(token.getLexeme());
					}
				}
				result = retainedTokens.toString();

			}
			if (getIgnoreCase()) {
				result = result.toLowerCase();
			}
			if (getIgnoreEmptyLines()) {
				Matcher lfMatcher = MULTI_LF.matcher(result);
				result = lfMatcher.replaceAll("\n");
			}
			if (getIgnoreWS()) {
				if (getScoring() == ScoringOptions.ByLine) {
					Matcher wsMatcher = MULTI_WS.matcher(result);
					result = wsMatcher.replaceAll(" ");
				} else {
					Matcher wsLfMatcher = MULTI_WS_LF.matcher(result);
					result = wsLfMatcher.replaceAll(" ");
				}
			}
		}
		return result;
	}

}
