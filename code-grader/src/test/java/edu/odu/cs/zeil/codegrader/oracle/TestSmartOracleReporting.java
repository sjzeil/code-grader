package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.OracleProperties;



public class TestSmartOracleReporting {

	@Test
    void testDifferenceReporting() {
        String expected = "line 1\nline 2\nline 3\nline 4\nline 5";
        String actual = "line 1\nline 2\nline xy\nline 4\nline 5";

        OracleProperties props = new OracleProperties();
        SmartOracle oracle = new SmartOracle(props, null, null, null);

        OracleResult success = oracle.compare(expected, expected);
        assertThat(success.score, is(100));
        assertThat(success.message, containsString("OK"));

        OracleResult failure = oracle.compare(expected, actual);
        assertThat(failure.score, is(0));
        assertThat(failure.message, containsString("expected:"));
        assertThat(failure.message, containsString("observed:"));
        assertThat(failure.message, containsString("line 2\n"));
        assertThat(failure.message, containsString("line 4\n"));
        assertThat(failure.message, not(containsString("line 1\n")));
        assertThat(failure.message, not(containsString("line 5\n")));
    }

	@Test
	void testSimple() {
        String line = "Hello world!";
        String lexeme = "world";
        Token token = new StringToken(lexeme, line.indexOf(lexeme));

        String display = SmartOracle.displayInContext(token, line, "[", "]", "previous line", "next line");
        assertThat(display, containsString("Hello [world]!"));
	}

	@Test
	void testBeginning() {
        String line = "Hello world!";
        String lexeme = "Hello";
        Token token = new StringToken(lexeme, line.indexOf(lexeme));

        String display = SmartOracle.displayInContext(token, line, "[", "]", "previous line", "next line");
        assertThat(display, containsString("[Hello] world!"));
	}

    @Test
	void testEnding() {
        String line = "Hello world!";
        String lexeme = "!";
        Token token = new StringToken(lexeme, line.indexOf(lexeme));

        String display = SmartOracle.displayInContext(token, line, "[", "]", "previous line", "next line");
        assertThat(display, containsString("Hello world[!]"));
	}

    @Test
	void testEncoding() {
        String line = "Hello\tworld!";
        String lexeme = "\t";
        Token token = new StringToken(lexeme, line.indexOf(lexeme));

        String display = SmartOracle.displayInContext(token, line, "[", "]", "previous line", "next line");
        assertThat(display, containsString("Hello[\\t]world!"));
	}

}
