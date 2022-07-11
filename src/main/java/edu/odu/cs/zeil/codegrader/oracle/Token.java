package edu.odu.cs.zeil.codegrader.oracle;

public abstract class Token {
	
	private String lexeme;
	private Oracle settings;

	public Token(String theLexeme, Oracle theSettings) {
		lexeme = theLexeme;
		settings = theSettings;
	}

	@Override
	public abstract boolean equals(Object actual);

	public String getLexeme() {
		return lexeme;
	}
	
	public Oracle getSettings() {
		return settings;
	}
	
	public String toString() {
		return "\"" + lexeme + '"';
	}
}
