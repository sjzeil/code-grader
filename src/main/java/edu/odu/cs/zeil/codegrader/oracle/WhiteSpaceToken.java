package edu.odu.cs.zeil.codegrader.oracle;

public class WhiteSpaceToken extends Token {

	public WhiteSpaceToken(String lexeme, OracleProperties settings) {
		super(lexeme, settings);
	}

	public boolean equals(Object actual) {
		if (actual instanceof WhiteSpaceToken) {
			WhiteSpaceToken other = (WhiteSpaceToken)actual;
			return getLexeme().equals(other.getLexeme());
		} else
			return false;
	}
}
