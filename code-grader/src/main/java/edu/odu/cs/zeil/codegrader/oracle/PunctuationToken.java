package edu.odu.cs.zeil.codegrader.oracle;

/**
 * A token representing a punctuation character.
 */
public class PunctuationToken extends Token {

	/**
	 * Create a token.
	 * @param lexeme the punctuation string
	 * @param position position of the token within the string
	 */
	public PunctuationToken(String lexeme, int position) {
		super(lexeme, position);
	}

	/**
	 * Compare two tokens.
	 * @param actual the other token
	 * @return true iff actual is a PunctuationToken and their lexemes match.
	 */
	public boolean equals(Object actual) {
		if (actual instanceof PunctuationToken) {
			String thisLexeme = getLexeme();
			PunctuationToken act = (PunctuationToken) actual;
			return thisLexeme.equals(act.getLexeme()); 
		} else {
			return false;
		}
	}

	/**
	 * @return hash code
	 */
	public int hashCode() {
		return getLexeme().hashCode();
	}

}
