package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.Message;
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
public class SmartOracle extends Oracle {

	private static final int CONTEXT_SIZE = 64;
	private static final Pattern WINDOWS_LINE_ENDINGS = Pattern.compile("\r\n");
	private static final Pattern MULTI_LF = Pattern.compile("\n\n+");
	private static final Pattern MULTI_WS = Pattern.compile("[ \t\r\f]+");
	private static final Pattern MULTI_WS_LF = Pattern.compile("[ \t\r\n\f]+");


	/**
     * error logging.
     */
    private static final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());


	/**
	 * Create a new smart oracle.
	 * 
	 * @param config   configuration properties
	 * @param testCase the test case to which this oracle will apply
	 * @param submission the submission being judged
	 * @param submitterStage the stage where the submitted code has been built
	 */
	public SmartOracle(OracleProperties config, TestCase testCase, 
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
		logger.trace("Oracle expected: ", expected);
		logger.trace("Oracle actual: ", actual);
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
		if (lineCount > 0 
		  && expectedLines[lineCount - 1].equals(Message.END_OF_OUTPUT_LINE)
		  && actualLines[lineCount - 1].equals(Message.END_OF_OUTPUT_LINE)) {
			--lineCount;
		}
		
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
						message = differenceMessage(lineNum,  "",
							expectedToken, 
							expectedLine,
							actualToken, 
							actualLine);
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
					message = differenceMessage(lineNum,  
						" ended early",
						expectedTokens.next(), 
						expectedLine,
						new WhiteSpaceToken("\n", actualLine.length()), 
						actualLine);
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
					message = differenceMessage(
						lineNum, " is too long",
						new WhiteSpaceToken("\n", expectedLine.length()), 
						expectedLine,
						actualTokens.next(), 
						actualLine);
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
				message = differenceMessage(actualLines.length, 
					" is last line of output, but more is expected",
					expectedLines[actualLines.length] + "\n",
					"[end of actual output]");
				noFailuresSoFar = false;
				lineCount += expectedLines.length - actualLines.length;
			}
		} else if (expectedLines.length < actualLines.length) {
			if (noFailuresSoFar) {
				message = "Actual output has too many lines.";
				message = differenceMessage(actualLines.length, 
					" is not the last line of output, is expected to be",
					"[end of expected output]",
					actualLines[expectedLines.length] + "\n");
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

	private static final String B_I = Message.HTML_PASSTHROUGH + "<i>"
		+ Message.HTML_PASSTHROUGH;
	private static final String E_I = Message.HTML_PASSTHROUGH + "</i>"
		+ Message.HTML_PASSTHROUGH;

	private String differenceMessage(int lineNumber, String msg, 
		String expected, String actual) {
			StringBuilder result = new StringBuilder();
			result.append("Output line ");
			result.append("" + lineNumber);
			result.append(msg);
			result.append("\n  " + B_I + "expected: " + E_I);
			result.append(contextEncode(limitedLength(expected, CONTEXT_SIZE)));
			result.append("\n  " + B_I + "observed: " + E_I);
			result.append(contextEncode(limitedLength(actual, CONTEXT_SIZE)));
			return result.toString();
	}

	private static String contextEncode(String msg) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < msg.length(); ++i) {
			char c = msg.charAt(i);
			if (c < ' ') {
				if (c == '\n') {
					result.append("\\n");
				} else if (c == '\r') {
					result.append("\\r");
				} else if (c == '\t') {
					result.append("\\t");
				} else if (c == Message.HTML_PASSTHROUGH.charAt(0)) {
					result.append(c);
				} else {
					result.append("?");
				}
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private String limitedLength(String msg, int len) {
		if (msg.length() > len) {
			msg = msg.subSequence(0, len) + "...";
		}
		return msg;
	}

	private String differenceMessage(
		int lineNumber, String postNumberStr,
		Token expectedToken, String expectedLine,
		Token actualToken, String actualLine
		) {
		String msg = "Output line " + lineNumber + postNumberStr + ":" 
			+ "\n  " + B_I + "expected: " + E_I
			+ displayExpectedInContext(expectedToken, expectedLine)
			+ "\n  " + B_I + "observed: " + E_I
			+ displayObservedInContext(actualToken, actualLine);
		return msg;
	}

	private String displayObservedInContext(Token actualToken,
	  String actualLine) {
		return displayInContext(actualToken, actualLine,
			BEGIN_OBSERVED, END_OBSERVED);
	}

	private String displayExpectedInContext(Token expectedToken, 
	  String expectedLine) {
		return displayInContext(expectedToken, expectedLine,
			BEGIN_EXPECTED, END_EXPECTED);
	}

	public static final String BEGIN_EXPECTED = Message.HTML_PASSTHROUGH
		+ "<span class='expected'>" + Message.HTML_PASSTHROUGH;
	public static final String END_EXPECTED = Message.HTML_PASSTHROUGH
		+ "</span>" + Message.HTML_PASSTHROUGH;
	public static final String BEGIN_OBSERVED = Message.HTML_PASSTHROUGH
		+ "<span class='observed'>" + Message.HTML_PASSTHROUGH;
	public static final String END_OBSERVED = Message.HTML_PASSTHROUGH
		+ "</span>" + Message.HTML_PASSTHROUGH;

	/**
	 * Display a token within the line where it occurs.
	 * @param token a token
	 * @param line the line in which it occurs
	 * @param startMarker string to display just before the changed token
	 * @param endMarker string to display just after the changed token
	 * @return a string containing parts of the line before and after
	 * 			the token itself.
	 */
	public static String displayInContext(Token token, String line, 
		String startMarker, String endMarker) {
		int start = Math.max(0, token.getPosition() - CONTEXT_SIZE);
		int stop = Math.min(token.getPosition(), line.length());
		String prefix = "";
		if (start <= stop) {
			prefix = line.substring(start, stop);
		}
		start = Math.min(token.getPosition() + token.getLexeme().length(), 
			line.length());
		stop = Math.min(start + CONTEXT_SIZE, line.length());
		String suffix = "";
		if (start <= stop) {
			suffix = line.substring(start, stop);
		}
		String result = prefix + startMarker + token.getLexeme() 
			+ endMarker + suffix;
		return contextEncode(result);
	}

	private String preprocess(String str) {
		if (getScoring() != Oracle.ScoringOptions.ByToken) {
			if (!str.contains(Message.END_OF_OUTPUT_MARKER)) {
				str = str + Message.END_OF_OUTPUT_MARKER;
			}
		}
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
