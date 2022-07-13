package edu.odu.cs.zeil.codegrader.oracle;

public class PunctuationToken extends Token {


	public PunctuationToken(String lexeme, Oracle settings) {
		super(lexeme, settings);
	}

	public boolean equals(Object actual) {
		if (actual instanceof PunctuationToken) {
			String thisLexeme = getLexeme();
			PunctuationToken act = (PunctuationToken)actual;
			return thisLexeme.equals(act.getLexeme()); 
		} else 
			return false;
	}

	public int hashCode() {
		return getLexeme().hashCode();
	}

}
