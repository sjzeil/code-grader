package edu.odu.cs.zeil.codegrader.oracle;

public class Scanner {
	
	private String source;
	private OracleProperties settings;
	private int pos = 0;
	private Token next;

	public Scanner(String input, OracleProperties settings) {
		source = input;
		this.settings = settings;
		fetchNext();
	}

	private int skipWhiteSpace (String source, int startingAt) {
		char c = source.charAt(startingAt);
		while (Character.isWhitespace(c) && startingAt < source.length()) {
			++startingAt;
			if (startingAt < source.length()) {
				c = source.charAt(startingAt);
			}
		}
		return startingAt;
	}
	
	private Token scanForNumber() {
		if (pos >= source.length()) {
			return null;
		}
		char c = source.charAt(pos);
		int start = pos;
		if (c == '+' || c == '-' || c == '.') {
			pos++;
			if (pos < source.length()) {
				c = source.charAt(pos);
			}
		}
		while (pos < source.length() && canParseAsNumber(source.substring(start, pos+1)) ) {
			pos++;
		}
		if (pos > start) {
			c = source.charAt(pos-1);
			while (Character.isWhitespace(c) && pos > start) {
				pos--;
				c = source.charAt(pos-1);
			}
		}
		String lexeme = source.substring(start, pos);
		if (canParseAsNumber(lexeme)) {
			return new NumberToken(lexeme, settings);
		} else {
			pos = start;
			return null;
		}
	}
	
	private Token scanForString() {
		if (pos >= source.length()) {
			return null;
		}
		char c = source.charAt(pos);
		int start = pos;
		while (pos < source.length() && !Character.isWhitespace(c) ) {
			pos++;
			if (pos < source.length()) {
				c = source.charAt(pos);
			}
		}
		String lexeme = source.substring(start, pos);
		return new StringToken(lexeme, settings);
	}
	
	private void fetchNext() {
		if (pos >= source.length()) {
			next = null;
			return;
		}
		pos = skipWhiteSpace(source, pos);
		if (pos >= source.length()) {
			next = null;
			return;
		}
		Token token = scanForNumber();
		if (token == null) {
			token = scanForString();
		}
		next = token;
	}


	private boolean canParseAsNumber(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public boolean hasNext() {
		return next != null;
	}

	public Token next() {
		Token tok = next;
		fetchNext();
		return tok;
	}


}
