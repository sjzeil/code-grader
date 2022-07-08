package edu.odu.cs.zeil.codegrader.oracle;

/**
 * Compares expected and actual outputs to determine if they match.
 * 
 * @author zeil
 *
 */
public class SmartOracle extends Oracle {

	private OracleProperties settings;
	
	public SmartOracle(OracleProperties config) {
		super(config);
	}

	/**
	 * Compare two strings to see if one is an acceptable variant of the other.
	 * The precise meaning of "acceptable" depends on the settings.
	 * 
	 * @param expected  the expected string
	 * @param actual  the string being examined
	 * @return true if actual is an acceptable variant of expected.
	 */
	public OracleResult compare(String expected, String actual) {
		Scanner expectedTokens = new Scanner(expected, settings);
		Scanner actualTokens = new Scanner(actual, settings);
		while (expectedTokens.hasNext() && actualTokens.hasNext()) {
			Token expectedToken = expectedTokens.next();
			Token actualToken = actualTokens.next();
			if (!expectedToken.equals(actualToken)) {
				return new OracleResult(0, "expected: " + expectedToken + "\nobserved: " + actualToken);
			}
		}
		if (expectedTokens.hasNext()) {
			return new OracleResult(0, "Actual output ended early.");
		} else if (actualTokens.hasNext()) {
			return new OracleResult(0, "Actual output is too long.");
		}
		return new OracleResult(100, "");
	}

}
