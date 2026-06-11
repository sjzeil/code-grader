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
     * @param config         configuration properties
     * @param testCase       the test case to which this oracle will apply
     * @param submission     the submission being judged
     * @param submitterStage the stage where the submitted code has been built
     */
    public SmartOracle(OracleProperties config, TestCase testCase,
            Submission submission, Stage submitterStage) {
        super(config, testCase, submission, submitterStage);
    }

    private Token filterToken(Token rawToken) {
        Token token = rawToken;
        if (token instanceof WhiteSpaceToken && getIgnoreWS()) {
            token = null;
        } else if (token instanceof PunctuationToken && getIgnorePunctuation()) {
            token = null;
        } else if (!(token instanceof NumberToken) && getNumbersOnly()) {
            token = null;
        } else if (getIgnoreCase()) {
            token = new StringToken(token.getLexeme().toLowerCase(), token.getPosition());
        }
        return token;
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
        int expectedLinesLen = expectedLines.length;
        int actualLinesLen = actualLines.length;
        if (getScoring() == ScoringOptions.ByLine) {
            if (expectedLinesLen > 0 && expectedLines[expectedLinesLen - 1].equals(Message.END_OF_OUTPUT_LINE)) {
                --expectedLinesLen;
            }
            if (actualLinesLen > 0 && actualLines[actualLinesLen - 1].equals(Message.END_OF_OUTPUT_LINE)) {
                --actualLinesLen;
            }
        }

        int lineCount = expectedLinesLen;

        int expectedLineNum = 0;
        int actualLineNum = 0;
        while (expectedLineNum < expectedLinesLen && actualLineNum < actualLinesLen) {
            String expectedLine = expectedLines[expectedLineNum];
            if (expectedLine.equals("") && getIgnoreEmptyLines()) {
                ++expectedLineNum;
                continue;
            }
            String actualLine = actualLines[actualLineNum];
            if (actualLine.equals("") && getIgnoreEmptyLines()) {
                ++actualLineNum;
                continue;
            }
            boolean lineOK = true;

            Scanner expectedTokens = new Scanner(expectedLine, this);
            Scanner actualTokens = new Scanner(actualLine, this);
            Token expectedToken = null;
            Token actualToken = null;
            while (true) {
                if (expectedToken == null) {
                    if (!expectedTokens.hasNext()) {
                        if (getIgnoreLineEndings()) {
                            if (expectedLineNum < expectedLinesLen - 1) {
                                ++expectedLineNum;
                                expectedLine = expectedLines[expectedLineNum];
                                expectedTokens = new Scanner(expectedLine, this);
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        expectedToken = filterToken(expectedTokens.next());
                    }
                }
                if (expectedToken != null) {
                    if (actualToken == null)
                        if (!actualTokens.hasNext()) {
                            if (getIgnoreLineEndings()) {
                                if (actualLineNum < actualLines.length - 1) {
                                    ++actualLineNum;
                                    actualLine = actualLines[actualLineNum];
                                    actualTokens = new Scanner(actualLine, this);
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            actualToken = filterToken(actualTokens.next());
                        }
                }
                if (expectedToken == null || actualToken == null) {
                    continue;
                }

                if (!expectedToken.equals(actualToken)) {
                    lineOK = false;
                    ++tokenCount;
                    if (noFailuresSoFar) {
                        message = composeDifferenceMessage(expectedLineNum,
                                actualLineNum,
                                "",
                                expectedToken,
                                expectedLine,
                                actualToken,
                                actualLine,
                                expectedLines,
                                actualLines);
                    }
                    noFailuresSoFar = false;
                    expectedToken = null;
                    actualToken = null;
                } else {
                    ++correctTokens;
                    ++tokenCount;
                    expectedToken = null;
                    actualToken = null;
                }
            }
            if (expectedToken != null) {
                lineOK = false;
                if (noFailuresSoFar) {
                    message = composeDifferenceMessage(
                            expectedLineNum,
                            actualLineNum,
                            " ended early",
                            expectedToken,
                            expectedLine,
                            new WhiteSpaceToken("\\n", actualLine.length()),
                            actualLine,
                            expectedLines,
                            actualLines);
                    noFailuresSoFar = false;
                    while ((expectedTokens.hasNext())) {
                        Token nextExpectedToken = expectedTokens.next();
                        if (!(nextExpectedToken instanceof WhiteSpaceToken)
                                || !getIgnoreWS()) {
                            ++tokenCount;
                        }
                    }
                }
            } else if (actualTokens.hasNext() || actualLineNum < actualLines.length - 1) {
                while (actualToken == null) {
                    if (!actualTokens.hasNext()) {
                        if (getIgnoreLineEndings()) {
                            if (actualLineNum < actualLines.length - 1) {
                                ++actualLineNum;
                                actualLine = actualLines[actualLineNum];
                                actualTokens = new Scanner(actualLine, this);
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        actualToken = filterToken(actualTokens.next());
                    }
                }
                if (actualToken != null) {
                    lineOK = false;
                    if (noFailuresSoFar) {
                        message = composeDifferenceMessage(
                                expectedLineNum,
                                actualLineNum,
                                " is too long",
                                new WhiteSpaceToken("\\n", expectedLine.length()),
                                expectedLine,
                                actualToken,
                                actualLine,
                                expectedLines,
                                actualLines);
                        noFailuresSoFar = false;
                        while ((actualTokens.hasNext())) {
                            Token nextActualToken = actualTokens.next();
                            if (!(nextActualToken instanceof WhiteSpaceToken)
                                    || !getIgnoreWS()) {
                                ++tokenCount;
                            }
                        }
                    }
                }
            }
            if (lineOK) {
                ++correctLines;
            }
            ++expectedLineNum;
            ++actualLineNum;
        }
        if (expectedLineNum < expectedLinesLen) {
            if (noFailuresSoFar) {
                message = differenceMessage(actualLines.length,
                        " is last line of output, but more is expected",
                        expectedLines[expectedLineNum] + "\n",
                        "[end of actual output]");
                noFailuresSoFar = false;
                lineCount += expectedLinesLen - expectedLineNum;
            }
        } else if (actualLineNum < actualLinesLen) {
            if (noFailuresSoFar) {
                message = "Actual output has too many lines.";
                message = differenceMessage(actualLines.length,
                        " is not the last line of output, is expected to be",
                        "[end of expected output]",
                        actualLines[actualLineNum] + "\n");
                noFailuresSoFar = false;
                lineCount += actualLines.length - actualLineNum;
            }
        }
        if (getScoring() == ScoringOptions.All) {
            return new OracleResult(((noFailuresSoFar) ? getCap() : 0), message);
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

    private String composeDifferenceMessage(int expectedLineNumber,
            int actualLineNumber,
            String postNumberStr,
            Token expectedToken, String expectedLine,
            Token actualToken, String actualLine,
            String[] expectedLines, String[] actualLines) {
        String precedingExpectedLine = (expectedLineNumber > 0) ? expectedLines[expectedLineNumber - 1] : "";
        String followingExpectedLine = (expectedLineNumber < expectedLines.length - 1)
                ? expectedLines[expectedLineNumber + 1]
                : "";
        String precedingActualLine = (actualLineNumber > 0) ? actualLines[actualLineNumber - 1] : "";
        String followingActualLine = (actualLineNumber < actualLines.length - 1) ? actualLines[actualLineNumber + 1]
                : "";
        return differenceMessage(actualLineNumber, postNumberStr,
                expectedToken, expectedLine,
                actualToken, actualLine,
                precedingExpectedLine, followingExpectedLine,
                precedingActualLine, followingActualLine);
    }

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
                    result.append(c);
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

    private static String limitedLength(String msg, int len) {
        if (msg.length() > len) {
            msg = msg.subSequence(0, len) + "...";
        }
        return msg;
    }

    private String differenceMessage(
            int lineNumber, String postNumberStr,
            Token expectedToken, String expectedLine,
            Token actualToken, String actualLine,
            String expectedPrecedingLine, String expectedFollowingLine,
            String actualPrecedingLine, String actualFollowingLine) {
        String msg = "Output line " + lineNumber + postNumberStr + ":"
                + "\n  " + B_I + "expected: " + E_I + "\n"
                + displayExpectedInContext(expectedToken, expectedLine, expectedPrecedingLine, expectedFollowingLine)
                + "\n\n  " + B_I + "observed: " + E_I + "\n"
                + displayObservedInContext(actualToken, actualLine,
                        actualPrecedingLine, actualFollowingLine);
        return msg;
    }

    private String displayObservedInContext(Token actualToken,
            String actualLine, String actualPrecedingLine, String actualFollowingLine) {
        return displayInContext(actualToken, actualLine,
                BEGIN_OBSERVED, END_OBSERVED,
                actualPrecedingLine, actualFollowingLine);
    }

    private String displayExpectedInContext(Token expectedToken,
            String expectedLine, String expectedPrecedingLine, String expectedFollowingLine) {
        return displayInContext(expectedToken, expectedLine,
                BEGIN_EXPECTED, END_EXPECTED,
                expectedPrecedingLine, expectedFollowingLine);
    }

    public static final String BEGIN_EXPECTED = Message.HTML_PASSTHROUGH
            + "<span class='expected'>" + Message.HTML_PASSTHROUGH;
    public static final String END_EXPECTED = Message.HTML_PASSTHROUGH
            + "</span>" + Message.HTML_PASSTHROUGH;
    public static final String BEGIN_OBSERVED = Message.HTML_PASSTHROUGH
            + "<span class='observed'>" + Message.HTML_PASSTHROUGH;
    public static final String END_OBSERVED = Message.HTML_PASSTHROUGH
            + "</span>" + Message.HTML_PASSTHROUGH;
    public static int LineLimit = 120;

    /**
     * Display a token within the line where it occurs.
     * 
     * @param token         a token
     * @param line          the line in which it occurs
     * @param startMarker   string to display just before the changed token
     * @param endMarker     string to display just after the changed token
     * @param precedingLine the line preceding the line in which the token occurs.
     * @param followingLine the line following the line in which the token occurs.
     * @return a string containing parts of the line before and after
     *         the token itself.
     */
    public static String displayInContext(Token token, String line,
            String startMarker, String endMarker, String precedingLine, String followingLine) {

        String preceding = "    " + limitedLength(precedingLine, LineLimit) + "\n    ";
        String following = "\n    " + limitedLength(followingLine, LineLimit) + "\n\n";
        if (token == null) {
            System.err.println("Token is null");
        }
        String prefix = limitedLength(line.substring(0, token.getPosition()), LineLimit);
        String suffix = "";
        try {
            suffix = limitedLength(line.substring(token.getPosition() + token.getLexeme().length()), LineLimit);
        } catch (IndexOutOfBoundsException e) {
            suffix = "";
        }
        String result = preceding + prefix + startMarker + token.getLexeme()
                + endMarker + suffix + following;
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
        return result;
    }

}
