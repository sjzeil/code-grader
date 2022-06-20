package edu.odu.cs.zeil.codegrader.oracle;

public abstract class Token {
	
	private String lexeme;
	private OracleProperties settings;

	public Token(String theLexeme, OracleProperties theSettings) {
		lexeme = theLexeme;
		settings = theSettings;
	}

	@Override
	public abstract boolean equals(Object actual);

	public String getLexeme() {
		return lexeme;
	}
	
	public OracleProperties getSettings() {
		return settings;
	}
	
	public String toString() {
		return "\"" + lexeme + '"';
	}
}
