package edu.odu.cs.zeil.codegrader.oracle;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;


public class TestNumericToken {
	
	@Test
	void testIntegerComparisons() {
		OracleProperties settings = new OracleProperties();
		NumberToken a = new NumberToken("42", settings);
		StringToken b = new StringToken("42", settings);
		assertThat (a, not(equalTo(b)));
		NumberToken c = new NumberToken("42", settings);
		assertThat (a, equalTo(c));
		NumberToken d = new NumberToken("43", settings);
		assertThat (a, not(equalTo(d)));
		NumberToken e = new NumberToken("42.0", settings);
		assertThat (a, not(equalTo(e)));
		NumberToken f = new NumberToken("+42", settings);
		assertThat (a, equalTo(f));
	    assertThat(new NumberToken("-42", settings), 
	    		equalTo(new NumberToken("-42", settings)));
	}

	@Test
	void testFloatingPointComparisons() {
		OracleProperties settings = new OracleProperties();
		NumberToken a = new NumberToken("42.00", settings);
		StringToken b = new StringToken("42", settings);
		assertThat (a, not(equalTo(b)));
		NumberToken c = new NumberToken("42.01", settings);
		assertThat (a, equalTo(c));
		c = new NumberToken("41.99", settings);
		assertThat (a, equalTo(c));
		c = new NumberToken("42.009", settings);
		assertThat (a, equalTo(c));
		c = new NumberToken("41.991", settings);
		assertThat (a, equalTo(c));
		c = new NumberToken("42.011", settings);
		assertThat (a, not(equalTo(c)));
		c = new NumberToken("42.899", settings);
		assertThat (a, not(equalTo(c)));
		c = new NumberToken("42", settings);
		assertThat (a, equalTo(c));
		assertThat (c, not(equalTo(a))); // Intentional asymmetry
		
	}

}