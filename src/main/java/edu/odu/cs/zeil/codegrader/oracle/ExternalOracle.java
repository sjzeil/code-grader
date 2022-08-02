package edu.odu.cs.zeil.codegrader.oracle;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.TestCase;

/**
 * Oracle that works by running an external command.
 * 
 * @author zeil
 *
 */
public class ExternalOracle extends Oracle {
	
	/**
	 * Create an oracle that launches an external command.
	 * 
	 * @param config properties
	 * @param testCase the test case on which it is applied
	 */
	public ExternalOracle(OracleProperties config, TestCase testCase) {
		super(config, testCase);
	}

	/**
	 * Compare two strings to see if one is an acceptable variant of the other.
	 * The precise meaning of "acceptable" depends on the settings.
	 * 
	 * @param expected  the expected string
	 * @param actual  the string being examined
	 * @return true if actual is an acceptable variant of expected.
	 */
	@Override
	public OracleResult compare(String expected, String actual) {
        //TODO
		return new OracleResult(OracleProperties.DEFAULT_POINT_CAP, "");
	}

}
