package edu.odu.cs.zeil.codegrader.oracle;

public abstract class Token {
	
	private String lexeme;

	/**
	 * Create a token.
	 * @param theLexeme lexeme for this token.
	 */
	public Token(String theLexeme) {
		lexeme = theLexeme;
	}

	@Override
	public abstract boolean equals(Object actual);

	/**
	 * Get the lexeme for this token.
	 * @return the lexeme
	 */
	public String getLexeme() {
		return lexeme;
	}
	
	/**
	 * Display this token as a string.
	 * @return the string representation.
	 */
	public String toString() {
		return lexeme;
	}
}
