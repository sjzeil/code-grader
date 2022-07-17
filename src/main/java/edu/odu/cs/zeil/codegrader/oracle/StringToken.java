package edu.odu.cs.zeil.codegrader.oracle;

/**
 * A token denoting an alphabetic string.
 */
public class StringToken extends Token {

	/**
	 * Create a token.
	 * @param lexeme the string of alphabetic characters
	 */
	public StringToken(String lexeme) {
		super(lexeme);
	}

	/**
	 * Compare two tokens.
	 * @param actual the other token
	 * @return true iff actual is a StringToken and the lexemes match.
	 */
	public boolean equals(Object actual) {
		if (actual instanceof StringToken) {
			String thisLexeme = getLexeme();
			StringToken act = (StringToken) actual;
			String actualLexeme = act.getLexeme();
			return thisLexeme.equals(actualLexeme); 
		} else {
			return false;
		}
	}

	/**
	 * @return hash code for this token
	 */
	@Override
	public int hashCode() {
		return getLexeme().hashCode();
	}

}
