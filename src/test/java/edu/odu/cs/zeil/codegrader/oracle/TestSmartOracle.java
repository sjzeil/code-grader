package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestProperties;


public class TestSmartOracle {

	public Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	public Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	
	Assignment asst;
	TestCase testCase;

	String expected = "76 trombones led the big parade,\nWith 110.00 cornets close at hand.";
	String badWord1 = "76 trumpets led the big parade,\nWith 110.00 cornets close at hand.";
	String badWord3 = "76 trumpets led the big parade,\nWith 110.00 corners closed at hand.";
	String numberFormat = "76.0 trombones led the big parade,\nWith 110.00 cornets close at hand.";
	String badNumber1 = "76.1 trombones led the big parade,\nWith 110.00 cornets close at hand.";
	String badFloatHigh = "76 trombones led the big parade,\nWith 110.02 cornets close at hand.";
	String badFloatLow = "76 trombones led the big parade,\nWith 109.98 cornets close at hand.";
	String caseVariant = "76 Trombones led the big parade,\nwith 110.00 cornets close at hand.";
	String ws = "76  trombones led the big parade,\nWith\t110.00 cornets close at hand.";
	String lineBreak = "76 trombones led the big parade, With 110.00 cornets close at hand.";
	String emptyLine = "76 trombones led the big parade,\n\nWith 110.00 cornets close at hand.";
	String badPunct = "76 trombones led the big parade\nWith 110.00 cornets close at hand.";;
	

	@BeforeEach
	public void setup() throws IOException {
		testSuitePath.toFile().getParentFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		testCase = new TestCase(new TestProperties(asst, "params"));
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(testSuitePath);
	}
	

	@Test
	void testDefaults() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, badWord1);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("Trombones"), is(true));

		result = oracle.compare(expected, ws);
		assertThat (result.score, equalTo(100));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat (result.score, equalTo(100));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat (result.score, equalTo(100));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat (result.score, equalTo(0));  

	}

	@Test
	void testNumericCompare() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, numberFormat);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("76.0"), is(true));

		result = oracle.compare(expected, badNumber1);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("76.1"), is(true));

		result = oracle.compare(expected, badFloatHigh);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("110.00"), is(true));
		assertThat (result.message.contains("110.02"), is(true));

		result = oracle.compare(expected, badFloatLow);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("110.00"), is(true));
		assertThat (result.message.contains("109.98"), is(true));
	}

	@Test
	void testIgnoreCase() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("case=0", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, badWord1);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat (result.score, equalTo(100));
		assertThat(result.message, is(Oracle.PASSED_TEST_MESSAGE));

		result = oracle.compare(expected, ws);
		assertThat (result.score, equalTo(100));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat (result.score, equalTo(100));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat (result.score, equalTo(100));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat (result.score, equalTo(0));  

	}


	@Test
	void testIgnoreWS() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("WS=true", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, badWord1);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("Trombones"), is(true));

		result = oracle.compare(expected, ws);
		assertThat (result.score, equalTo(0));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat (result.score, equalTo(0));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat (result.score, equalTo(100));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat (result.score, equalTo(0));  

	}


	@Test
	void testIgnoreEmptyLines() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("WS=1,emptyLines=1", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, badWord1);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("Trombones"), is(true));

		result = oracle.compare(expected, ws);
		assertThat (result.score, equalTo(0));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat (result.score, equalTo(0));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat (result.score, equalTo(0));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat (result.score, equalTo(0));  

	}

	@Test
	void testIgnorePunct() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("punctuation=false", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, badWord1);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("Trombones"), is(true));

		result = oracle.compare(expected, ws);
		assertThat (result.score, equalTo(100));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat (result.score, equalTo(100));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat (result.score, equalTo(100));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat (result.score, equalTo(100));  

	}


	@Test
	void testNumericPrecision1() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("precision=0.02", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, numberFormat);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, badNumber1);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("76.1"), is(true));

		result = oracle.compare(expected, badFloatHigh);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, badFloatLow);
		assertThat (result.score, equalTo(100));
	}


	@Test
	void testNumbersOnly() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("ws=0,numbersOnly=true", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		String expected1 = "a 42 \n 1.414";
		String actual1 = "b 42 1.413";

		result = oracle.compare(expected1, actual1);
		assertThat (result.score, equalTo(100));

		String actual2 = "b 42 1.412";

		result = oracle.compare(expected1, actual2);
		assertThat (result.score, equalTo(0));

	}



	@Test
	void testScoringByLine() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("scoring=byLine", testCase);
		OracleResult result = oracle.compare(expected, expected);
		assertThat (result.score, equalTo(100));

		result = oracle.compare(expected, badWord1);
		assertThat (result.score, equalTo(50));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat (result.score, equalTo(0));
		assertThat (result.message.contains("trombones"), is(true));
		assertThat (result.message.contains("trumpets"), is(true));
	}

	@Test
	void testScoringByToken() throws FileNotFoundException {
		Oracle oracle = new SmartOracle("scoring=byToken,ignoreWS=true", testCase);
		OracleResult result = oracle.compare("1 2 3 a b", "1 2 z a b");

		assertThat (result.score, equalTo(80));
		assertThat (result.message.contains("3"), is(true));
		assertThat (result.message.contains("z"), is(true));
	}


}
