package edu.odu.cs.zeil.codegrader.oracle;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.endsWith;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestProperties;

public class TestScanner {

	public Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	public Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	
	Assignment asst;
	TestCase testCase;
	Oracle config;

	@BeforeEach
	public void setup() throws IOException {
		testSuitePath.toFile().getParentFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		testCase = new TestCase(new TestProperties(asst, "params"));
		config = new SmartOracle("", testCase);
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(testSuitePath);
	}
	

	@Test
	void testIsolatedString1() throws FileNotFoundException {
		String input = "abc";

		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertThat(token.getClass().getName(), endsWith("StringToken"));
		assertThat(token.getLexeme(), equalTo(input));
	}

	@Test
	void testPunctString1() throws FileNotFoundException {
		String input = "-abc";
		
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(true));
		assertTrue(token instanceof PunctuationToken);
		assertThat(token.getLexeme(), equalTo("-"));

		token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertTrue(token instanceof StringToken);
		assertThat(token.getLexeme(), equalTo("abc"));
	}

	@Test
	void testPunctString2() throws FileNotFoundException {
		String input = "+abc";
		
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		assertThat(scanner.hasNext(), equalTo(true));

		Token token = scanner.next(); 
		assertTrue(token instanceof PunctuationToken);
		assertThat(token.getLexeme(), equalTo("+"));

		token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertTrue(token instanceof StringToken);
		assertThat(token.getLexeme(), equalTo("abc"));
	}
	
	@Test
	void testIsolatedNumber() throws FileNotFoundException {
		String input = "+123.4";
		
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertThat(token.getClass().getName(), endsWith("NumberToken"));
		assertThat(token.getLexeme(), equalTo(input));
	}

	@Test
	void testNumberFollowedByLetters() throws FileNotFoundException {
		String input = "123min";
		
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
	void testNumberFollowedByWS() throws FileNotFoundException {
		String input = "123 ";
		
		Scanner scanner = new Scanner(input, config);
		assertThat(scanner.hasNext(), equalTo(true));
		Token token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(true));
		assertTrue(token instanceof NumberToken);
		assertThat(token.getLexeme(), equalTo("123"));

		token = scanner.next(); 
		assertThat(scanner.hasNext(), equalTo(false));
		assertTrue(token instanceof WhiteSpaceToken);
		assertThat(token.getLexeme(), equalTo(" "));

	}

	
	@Test
	void testNonWSExtraction() throws FileNotFoundException {
		String input = "traveling at -35 mph and\n"
				+ "then at 42.50 mph, for 120sec";
				Assignment asst = new Assignment();

		Scanner scanner = new Scanner(input, config);
		
		ArrayList<Token> tokens = new ArrayList<Token>();
		while (scanner.hasNext()) {
			tokens.add(scanner.next());
		}
		String[] expectedTokenClasses = {"StringToken", "WhiteSpaceToken", "StringToken", "WhiteSpaceToken", "NumberToken", "WhiteSpaceToken", "StringToken", "WhiteSpaceToken", "StringToken",
			"WhiteSpaceToken", "StringToken", "WhiteSpaceToken", "StringToken", "WhiteSpaceToken", "NumberToken", "WhiteSpaceToken", "StringToken", "PunctuationToken",
			"WhiteSpaceToken", "StringToken", "WhiteSpaceToken", "NumberToken", "StringToken"}; 
		String[] expectedLexemes = {"traveling", " ", "at", " ", "-35",  " ", "mph",  " ", "and", "\n",
				"then", " ", "at", " ", "42.50", " ", "mph", ",",  " ", "for",  " ", "120", "sec"};
		assertThat(tokens.size(), equalTo(expectedTokenClasses.length));
		for (int i = 0; i < tokens.size(); ++i) {
			assertThat(tokens.get(i).getClass().getName(), endsWith(expectedTokenClasses[i]));
			assertThat(tokens.get(i).getLexeme(), equalTo(expectedLexemes[i]));
		}
	}

}
