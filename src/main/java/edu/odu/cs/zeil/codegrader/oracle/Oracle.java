package edu.odu.cs.zeil.codegrader.oracle;

/**
 * Compares expected and actual outputs to determine if they match.
 * 
 * @author zeil
 *
 */
public class Oracle {

	private OracleProperties settings;
	
	public Oracle(OracleProperties config) {
		settings = config;
	}

	/**
	 * Compare two strings to see if one is an acceptable variant of the other.
	 * The precise meaning of "acceptable" depends on the settings.
	 * 
	 * @param expected  the expected string
	 * @param actual  the string being examined
	 * @return true if actual is an acceptable variant of expected.
	 */
	public boolean compare(String expected, String actual) {
		Scanner expectedTokens = new Scanner(expected, settings);
		Scanner actualTokens = new Scanner(actual, settings);
		while (expectedTokens.hasNext() && actualTokens.hasNext()) {
			Token expectedToken = expectedTokens.next();
			Token actualToken = actualTokens.next();
			if (!expectedToken.equals(actualToken)) {
				return false;
			}
		}
		return (!expectedTokens.hasNext()) && (!actualTokens.hasNext());
	}

}
