package edu.odu.cs.zeil.codegrader.oracle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.endsWith;

import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.oracle.OracleProperties;
import edu.odu.cs.zeil.codegrader.oracle.Scanner;
import edu.odu.cs.zeil.codegrader.oracle.Token;

public class TestScanner {
	
	@Test
	void testIsolatedString1() {
		String input = "abc";
		OracleProperties config = new OracleProperties();
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertThat(token.getClass().getName(), endsWith("StringToken"));
		assertThat(token.getLexeme(), equalTo(input));
	}

	@Test
	void testIsolatedString2() {
		String input = "-abc";
		OracleProperties config = new OracleProperties();
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertThat(token.getClass().getName(), endsWith("StringToken"));
		assertThat(token.getLexeme(), equalTo(input));
	}

	@Test
	void testIsolatedString3() {
		String input = "+abc";
		OracleProperties config = new OracleProperties();
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertThat(token.getClass().getName(), endsWith("StringToken"));
		assertThat(token.getLexeme(), equalTo(input));
	}
	
	@Test
	void testIsolatedNumber() {
		String input = "+123.4";
		OracleProperties config = new OracleProperties();
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertThat(token.getClass().getName(), endsWith("NumberToken"));
		assertThat(token.getLexeme(), equalTo(input));
	}

	@Test
	void testNumberFollowedByLetters() {
		String input = "123min";
		OracleProperties config = new OracleProperties();
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(true));
		assertThat(token.getClass().getName(), endsWith("NumberToken"));
		assertThat(token.getLexeme(), equalTo("123"));
		
		token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertThat(token.getClass().getName(), endsWith("StringToken"));
		assertThat(token.getLexeme(), equalTo("min"));
	}

	@Test
	void testNumberFollowedByWS() {
		String input = "123 ";
		OracleProperties config = new OracleProperties();
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertThat(token.getClass().getName(), endsWith("NumberToken"));
		assertThat(token.getLexeme(), equalTo("123"));
	}

	
	@Test
	void testNonWSExtraction() {
		String input = "travelling at -35 mph and\n"
				+ "then at 42.50 mph, for 120sec";
		OracleProperties config = new OracleProperties();
		Scanner scanner = new Scanner(input, config);
		
		ArrayList<Token> tokens = new ArrayList<Token>();
		while (scanner.hasNext()) {
			tokens.add(scanner.next());
		}
		String[] expectedTokenClasses = {"StringToken", "StringToken", "NumberToken", "StringToken", "StringToken",
				"StringToken", "StringToken", "NumberToken", "StringToken",
				"StringToken", "NumberToken", "StringToken"}; 
		String[] expectedLexemes = {"travelling", "at",  "-35",  "mph",  "and",
				"then", "at", "42.50", "mph,",  "for",  "120", "sec"};
		assertThat(tokens.size(), equalTo(expectedTokenClasses.length));
		for (int i = 0; i < tokens.size(); ++i) {
			assertThat(tokens.get(i).getClass().getName(), endsWith(expectedTokenClasses[i]));
			assertThat(tokens.get(i).getLexeme(), equalTo(expectedLexemes[i]));
		}
	}

}
