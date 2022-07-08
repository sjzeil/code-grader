package edu.odu.cs.zeil.codegrader.oracle;

/**
 * Compares expected and actual outputs to determine if they match.
 * 
 * @author zeil
 *
 */
public abstract class Oracle {

	protected OracleProperties settings;
	
	Oracle(OracleProperties config) {
		settings = config;
	}

	/**
	 * Compare two strings to see if one is an acceptable variant of the other.
	 * The precise meaning of "acceptable" depends on the oracle and its settings.
	 * 
	 * @param expected  the expected string
	 * @param actual  the string being examined
	 * @return score and explanation 
	 */
	public abstract OracleResult compare(String expected, String actual);
	

}
