package edu.odu.cs.zeil.codegrader.oracle;

public class LineBreakToken extends Token {

	public LineBreakToken(String lexeme, Oracle settings) {
		super(lexeme, settings);
	}

	public boolean equals(Object actual) {
		return actual instanceof LineBreakToken;
	}
}
