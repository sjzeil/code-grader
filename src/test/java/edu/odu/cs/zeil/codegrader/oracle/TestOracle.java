package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;


public class TestOracle {

	@Test
	void testTextCompare() {
		String expected = "Twas brillig and the slithy toves\ndid gyre and gimble in the wabe;";
		String actual = "Twas brillig and the slithy toves\ndid gyre and gimble in the wabe;";
		OracleProperties config = new OracleProperties();
		Oracle oracle = new Oracle(config);
		assertThat (oracle.compare(expected, actual), equalTo(true));

		String actualCaseVariant = "twas Brillig and the slithy toves\ndid gyre and gimble in the wabe;";
		assertThat (oracle.compare(expected, actualCaseVariant), equalTo(false));
		config.caseSensitive = false;
		assertThat (oracle.compare(expected, actualCaseVariant), equalTo(true));

	}

	@Test
	void testNumericCompare() {
		String expected = "If train A leaves New York travelling at 35 mph and\n"
				+ "train B leaves Boston travelling at 42.50 mph,";
		OracleProperties config = new OracleProperties();
		Oracle oracle = new Oracle(config);
		assertThat (oracle.compare(expected, expected), equalTo(true));
		
		String reformatted = "If train A leaves New York travelling at 35.0 mph and\n"
				+ "train B leaves Boston travelling at 42.5 mph,";
		assertThat (oracle.compare(expected, reformatted), equalTo(false));

		String highOK = "If train A leaves New York travelling at 35 mph and\n"
				+ "train B leaves Boston travelling at 42.51 mph,";
		assertThat (oracle.compare(expected, highOK), equalTo(true));
	
		String lowOK = "If train A leaves New York travelling at 35 mph and\n"
				+ "train B leaves Boston travelling at 42.49 mph,";
		assertThat (oracle.compare(expected, lowOK), equalTo(true));
	
		String tooHigh1 = "If train A leaves New York travelling at 36 mph and\n"
				+ "train B leaves Boston travelling at 42.4 mph,";
		assertThat (oracle.compare(expected, tooHigh1), equalTo(false));

		String tooHigh2 = "If train A leaves New York travelling at 35 mph and\n"
				+ "train B leaves Boston travelling at 42.61 mph,";
		assertThat (oracle.compare(expected, tooHigh2), equalTo(false));
	
		String tooLow1 = "If train A leaves New York travelling at 34 mph and\n"
				+ "train B leaves Boston travelling at 42.5 mph,";
		assertThat (oracle.compare(expected, tooLow1), equalTo(false));

		String tooLow2 = "If train A leaves New York travelling at 35 mph and\n"
				+ "train B leaves Boston travelling at 42.45 mph,";
		assertThat (oracle.compare(expected, tooLow2), equalTo(false));
	}

}
