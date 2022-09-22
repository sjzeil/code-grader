package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestCaseProperties;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;
import edu.odu.cs.zeil.codegrader.TestSuitePropertiesBase;

//CHECKSTYLE:OFF

public class TestSmartOracle {

	private  Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	private Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	private Submission sub;
	
	private Assignment asst;
	private TestCase testCase;
	private OracleProperties prop;

	private String expected = 
		"76 trombones led the big parade,\nWith 110.00 cornets close at hand.";
	private String badWord1 = 
		"76 trumpets led the big parade,\nWith 110.00 cornets close at hand.";
	private String badWord3 =
		"76 trumpets led the big parade,\nWith 110.00 corners closed at hand.";
	private String numberFormat =
	  "76.0 trombones led the big parade,\nWith 110.00 cornets close at hand.";
	private String badNumber1 =
	  "76.1 trombones led the big parade,\nWith 110.00 cornets close at hand.";
	private String badFloatHigh =
	  "76 trombones led the big parade,\nWith 110.02 cornets close at hand.";
	private String badFloatLow =
	  "76 trombones led the big parade,\nWith 109.98 cornets close at hand.";
	private String caseVariant =
		"76 Trombones led the big parade,\nwith 110.00 cornets close at hand.";
	private String ws =
	  "76  trombones led the big parade,\nWith\t110.00 cornets close at hand.";
	private String lineBreak =
	  "76 trombones led the big parade, With 110.00 cornets close at hand.";
	private String emptyLine =
	  "76 trombones led the big parade,\n\nWith 110.00 cornets close at hand.";
	private String badPunct =
	  "76 trombones led the big parade\nWith 110.00 cornets close at hand.";

	private static final int OK = OracleProperties.DEFAULT_POINT_CAP;

	private Stage stage;
	
	@BeforeEach
	private void setup() throws IOException, TestConfigurationError {
        Path testData = Paths.get("build", "test-data");
        if (testData.toFile().exists()) {
            FileUtils.deleteDirectory(testData);
        }

		testSuitePath.toFile().getParentFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, null, null,
			StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		asst.setStagingDirectory(testSuitePath.resolve("stage"));

		testCase = new TestCase(new TestCaseProperties(asst, "params"));
		prop = new OracleProperties();
		sub = new Submission(asst, "student1", 
            testSuitePath.resolve("submissions"));
		stage = new Stage(asst, sub, new TestSuitePropertiesBase());
	}
	

	@Test
	void testDefaults() throws FileNotFoundException {
		Oracle oracle = new SmartOracle(new OracleProperties(), testCase,
			sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OracleProperties.DEFAULT_POINT_CAP));

		result = oracle.compare(expected, badWord1);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("Trombones"), is(true));

		result = oracle.compare(expected, ws);
		assertThat(result.score, 
			equalTo(OK));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat(result.score, 
			equalTo(OK));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat(result.score,
			equalTo(OK));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat(result.score, equalTo(0));  

	}

	@Test
	void testDefaults2() throws FileNotFoundException {
		Oracle oracle = new SmartOracle(new OracleProperties(),
			testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OracleProperties.DEFAULT_POINT_CAP));

		String mixed = "The square root of 3.00 is 1.7321.\n";
		result = oracle.compare(mixed, mixed);
		assertThat(result.score, equalTo(100));
		
	}

	@Test
	void testNumericCompare() throws FileNotFoundException {
		Oracle oracle = new SmartOracle(new OracleProperties(),
			testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, numberFormat);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("76.0"), is(true));

		result = oracle.compare(expected, badNumber1);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("76.1"), is(true));

		result = oracle.compare(expected, badFloatHigh);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("110.00"), is(true));
		assertThat(result.message.contains("110.02"), is(true));

		result = oracle.compare(expected, badFloatLow);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("110.00"), is(true));
		assertThat(result.message.contains("109.98"), is(true));
	}

	@Test
	void testIgnoreCase() throws FileNotFoundException {
		prop.caseSig = false;
		Oracle oracle = new SmartOracle(prop, testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, badWord1);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat(result.score, equalTo(OK));
		assertThat(result.message, is(Oracle.PASSED_TEST_MESSAGE));

		result = oracle.compare(expected, ws);
		assertThat(result.score,
			equalTo(OK));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat(result.score,
			equalTo(OK));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat(result.score,
			equalTo(OK));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat(result.score, equalTo(0));  

	}


	@Test
	void testIgnoreWS() throws FileNotFoundException {
		prop.ws = true;
		Oracle oracle = new SmartOracle(prop, testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, badWord1);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("Trombones"), is(true));

		result = oracle.compare(expected, ws);
		assertThat(result.score, 
			equalTo(0));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat(result.score, 
			equalTo(0));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat(result.score, 
			equalTo(OK));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat(result.score, equalTo(0));  

	}


	@Test
	void testIgnoreEmptyLines() throws FileNotFoundException {
		prop.ws = true;
		prop.emptyLines = true;
		Oracle oracle = new SmartOracle(prop, testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, badWord1);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("Trombones"), is(true));

		result = oracle.compare(expected, ws);
		assertThat(result.score, 
			equalTo(0));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat(result.score, 
			equalTo(0));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat(result.score, 
			equalTo(0));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat(result.score, equalTo(0));  

	}

	@Test
	void testIgnorePunct() throws FileNotFoundException {
		prop.punctuation = false;
		Oracle oracle = new SmartOracle(prop, testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, badWord1);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, caseVariant);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("Trombones"), is(true));

		result = oracle.compare(expected, ws);
		assertThat(result.score, 
			equalTo(OK));  // By default, whitespace is ignored

		result = oracle.compare(expected, lineBreak);
		assertThat(result.score, 
			equalTo(OK));  // By default, whitespace is ignored

		result = oracle.compare(expected, emptyLine);
		assertThat(result.score, 
			equalTo(OK));  // By default, empty lines are ignored

		result = oracle.compare(expected, badPunct);
		assertThat(result.score, equalTo(OK));  

	}


	@Test
	void testNumericPrecision1() throws FileNotFoundException {
		prop.precision = 0.02;
		Oracle oracle = new SmartOracle(prop, testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, numberFormat);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, badNumber1);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("76.1"), is(true));

		result = oracle.compare(expected, badFloatHigh);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, badFloatLow);
		assertThat(result.score, equalTo(OK));
	}


	@Test
	void testNumbersOnly() throws FileNotFoundException {
		prop.ws = false;
		prop.numbersOnly = true;
		Oracle oracle = new SmartOracle(prop, testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OK));

		String expected1 = "a 42 \n 1.414";
		String actual1 = "b 42 1.413";

		result = oracle.compare(expected1, actual1);
		assertThat(result.score, equalTo(OK));

		String actual2 = "b 42 1.412";

		result = oracle.compare(expected1, actual2);
		assertThat(result.score, equalTo(0));

	}



	@Test
	void testScoringByLine() throws FileNotFoundException {
		prop.scoring = Oracle.ScoringOptions.ByLine;
		Oracle oracle = new SmartOracle(prop, testCase, sub, stage);
		OracleResult result = oracle.compare(expected, expected);
		assertThat(result.score, equalTo(OK));

		result = oracle.compare(expected, badWord1);
		assertThat(result.score, equalTo(50));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));

		result = oracle.compare(expected, badWord3);
		assertThat(result.score, equalTo(0));
		assertThat(result.message.contains("trombones"), is(true));
		assertThat(result.message.contains("trumpets"), is(true));
	}

	@Test
	void testScoringByToken() throws FileNotFoundException {
		prop.scoring = Oracle.ScoringOptions.ByToken;
		prop.ws = false;
		Oracle oracle = new SmartOracle(prop, testCase, sub, stage);
		OracleResult result = oracle.compare("1 2 3 a b", "1 2 z a b");

		assertThat(result.score, equalTo(80));
		assertThat(result.message.contains("3"), is(true));
		assertThat(result.message.contains("z"), is(true));
	}


}
