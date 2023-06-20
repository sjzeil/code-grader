package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;



public class TestSmartOracleReporting {

		

	@Test
	void testSimple() {
        String line = "Hello world!";
        String lexeme = "world";
        Token token = new StringToken(lexeme, line.indexOf(lexeme));

        String display = SmartOracle.displayInContext(token, line, "[", "]");
        assertThat(display, is("Hello [world]!"));
	}

	@Test
	void testBeginning() {
        String line = "Hello world!";
        String lexeme = "Hello";
        Token token = new StringToken(lexeme, line.indexOf(lexeme));

        String display = SmartOracle.displayInContext(token, line, "[", "]");
        assertThat(display, is("[Hello] world!"));
	}

    @Test
	void testEnding() {
        String line = "Hello world!";
        String lexeme = "!";
        Token token = new StringToken(lexeme, line.indexOf(lexeme));

        String display = SmartOracle.displayInContext(token, line, "[", "]");
        assertThat(display, is("Hello world[!]"));
	}

    @Test
	void testEncoding() {
        String line = "Hello\tworld!";
        String lexeme = "\t";
        Token token = new StringToken(lexeme, line.indexOf(lexeme));

        String display = SmartOracle.displayInContext(token, line, "[", "]");
        assertThat(display, is("Hello[\\t]world!"));
	}

}
