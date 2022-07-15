package edu.odu.cs.zeil.codegrader.oracle;

public class Scanner {

	private String source;
	private Oracle settings;
	private int pos = 0;
	private Token next;

	public Scanner(String input, Oracle settings) {
		source = input;
		this.settings = settings;
		fetchNext();
	}

	private int skipWhiteSpace(String source, int startingAt) {
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
		while (pos < source.length() && canParseAsNumber(source.substring(start, pos + 1))) {
			pos++;
		}
		if (pos > start) {
			c = source.charAt(pos - 1);
			while (Character.isWhitespace(c) && pos > start) {
				pos--;
				c = source.charAt(pos - 1);
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

	private Token scanForWhiteSpace() {
		if (pos >= source.length()) {
			return null;
		}
		char c = source.charAt(pos);
		int start = pos;
		while (pos < source.length() && Character.isWhitespace(c)) {
			pos++;
			if (pos < source.length()) {
				c = source.charAt(pos);
			}
		}
		if (pos > start) {
			String lexeme = source.substring(start, pos);
			return new WhiteSpaceToken(lexeme, settings);
		} else {
			return null;
		}
	}

	private Token scanForAlpha() {
		if (pos >= source.length()) {
			return null;
		}
		char c = source.charAt(pos);
		int start = pos;
		while (pos < source.length() && Character.isAlphabetic(c)) {
			pos++;
			if (pos < source.length()) {
				c = source.charAt(pos);
			}
		}
		if (pos > start) {
			String lexeme = source.substring(start, pos);
			return new StringToken(lexeme, settings);
		} else {
			return null;
		}
	}

	private Token scanForPunctuation() {
		if (pos >= source.length()) {
			return null;
		}
		String lexeme = source.substring(pos, pos + 1);
		++pos;
		return new PunctuationToken(lexeme, settings);
	}

	private void fetchNext() {
		if (pos >= source.length()) {
			next = null;
			return;
		}

		if (pos >= source.length()) {
			next = null;
			return;
		}
		Token token = scanForNumber();
		if (token == null) {
			token = scanForWhiteSpace();
			if (token == null) {
				token = scanForAlpha();
				if (token == null) {
					token = scanForPunctuation();
				}
			}
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