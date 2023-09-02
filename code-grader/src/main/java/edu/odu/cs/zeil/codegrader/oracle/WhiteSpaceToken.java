package edu.odu.cs.zeil.codegrader.oracle;

/**
 * A token denoting a single whitespace character.
 */
public class WhiteSpaceToken extends Token {

	/**
	 * Create a token.
	 * @param lexeme the whitespace string
	 * @param position position of the token within the string
	 */
	public WhiteSpaceToken(String lexeme, int position) {
		super(lexeme, position);
	}

	/**
	 * @param actual another token
	 * @return true iff actual is a WhiteSpaceToken and their lexemes match.
	 */
	@Override
	public boolean equals(Object actual) {
		if (actual instanceof WhiteSpaceToken) {
			WhiteSpaceToken other = (WhiteSpaceToken)actual;
			return getLexeme().equals(other.getLexeme());
		} else {
			return false;
		}
	}

	/**
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return getLexeme().hashCode();
	}

}
