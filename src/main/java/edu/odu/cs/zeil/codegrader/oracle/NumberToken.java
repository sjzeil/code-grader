package edu.odu.cs.zeil.codegrader.oracle;

public class NumberToken extends Token {

	public NumberToken(String lexeme, Oracle settings) {
		super(lexeme, settings);
	}

	public boolean equals(Object actual) {
		if (actual instanceof NumberToken) {
			NumberToken act = (NumberToken)actual;
			if (getLexeme().contains(".")) {
				// Floating point comparison
				double delta = inferAbsoluteErrorBound(getLexeme());
				Double d1 = Double.parseDouble(getLexeme());
				Double d2 = Double.parseDouble(act.getLexeme());
				return (Math.abs(d1-d2) <= delta);
			} else {
				if (act.getLexeme().contains(".")) {
					// Integer to floating point comparison
					return false;
				} else {
					// Integer to integer comparison
					Integer n1 = Integer.parseInt(getLexeme());
					Integer n2 = Integer.parseInt(act.getLexeme());
					return n1 == n2;
				}
			}
		} else {
			return false;
		}
	}

	// Look at the number of digits after the decimal point to
	// infer the desired precision of comparison.
	private double inferAbsoluteErrorBound(String lexeme) {
		int decimalPos = lexeme.indexOf('.');
		double delta = 1.0;
		int k = decimalPos + 1;
		while (k < lexeme.length()) {
			++k;
			delta /= 10.0;
		}
		return delta;
	}
}
