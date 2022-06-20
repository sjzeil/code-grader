package edu.odu.cs.zeil.codegrader.oracle;

public class StringToken extends Token {


	public StringToken(String lexeme, OracleProperties settings) {
		super(lexeme, settings);
	}

	public boolean equals(Object actual) {
		if (actual instanceof StringToken) {
			String thisLexeme = getLexeme();
			StringToken act = (StringToken)actual;
			String actualLexeme = act.getLexeme();
			if (!getSettings().caseSensitive) {
				thisLexeme = thisLexeme.toLowerCase();
				actualLexeme = actualLexeme.toLowerCase();
			}
			return thisLexeme.equals(actualLexeme); 
		} else 
			return false;
	}
}
