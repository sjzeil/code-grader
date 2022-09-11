package edu.odu.cs.zeil.codegrader.oracle;

//CHECKSTYLE:OFF

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;



public final class TestNumericToken {


	@Test
	void testIntegerComparisons() throws FileNotFoundException {

		NumberToken a = new NumberToken("42", 0, -1.0);
		StringToken b = new StringToken("42", 0);
		assertThat(a, not(equalTo(b)));
		NumberToken c = new NumberToken("42", 0, -1.0);
		assertThat(a, equalTo(c));
		NumberToken d = new NumberToken("43", 0, -1.0);
		assertThat(a, not(equalTo(d)));
		NumberToken e = new NumberToken("42.0", 0, -1.0);
		assertThat(a, not(equalTo(e)));
		NumberToken f = new NumberToken("+42", 0, -1.0);
		assertThat(a, equalTo(f));
	    assertThat(new NumberToken("-42", 0, -1.0), 
	    		equalTo(new NumberToken("-42", 0, 0.01)));
	}

	@Test
	void testFloatingPointComparisons() throws FileNotFoundException {
		NumberToken a = new NumberToken("42.00", 0, -1.0);
		StringToken b = new StringToken("42", 0);
		assertThat(a, not(equalTo(b)));
		NumberToken c = new NumberToken("42.01", 0, -1.0);
		assertThat(a, equalTo(c));
		c = new NumberToken("41.99", 0, -1.0);
		assertThat(a, equalTo(c));
		c = new NumberToken("42.009", 0, -1.0);
		assertThat(a, equalTo(c));
		c = new NumberToken("41.991", 0, -1.0);
		assertThat(a, equalTo(c));
		c = new NumberToken("42.011", 0, -1.0);
		assertThat(a, not(equalTo(c)));
		c = new NumberToken("42.899", 0, -1.0);
		assertThat(a, not(equalTo(c)));
		c = new NumberToken("42", 0, -1.0);
		assertThat(a, equalTo(c));
		assertThat(c, not(equalTo(a))); // Intentional asymmetry
		
	}

}
