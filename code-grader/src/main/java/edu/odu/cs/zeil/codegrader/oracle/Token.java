package edu.odu.cs.zeil.codegrader.oracle;

public abstract class Token {
	
	private String lexeme;
	private int position;

	/**
	 * Create a token.
	 * @param theLexeme lexeme for this token.
	 * @param pos position of this lexeme within the string
	 */
	public Token(String theLexeme, int pos) {
		lexeme = theLexeme;
		position = pos;
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

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
}
