package edu.odu.cs.zeil.codegrader.oracle;

/**
 * A token denoting a number.
 */
public class NumberToken extends Token {

	private double delta;

	/**
	 * Create a token.
	 * @param lexeme the string denoting the number.
	 * @param position position of the token within the string.
	 * @param precision How close this must be to another number to be
	 *          judged equal.
	 */
	public NumberToken(String lexeme, int position, double precision) {
		super(lexeme, position);
		delta = precision;
	}

	/**
	 * Compares the numeric value of two numbers based upon the precision
	 * value recorded for the first number.
	 * @param actual another token
	 * @return true iff actual is a NumberToken and their values are
	 *             within the specified precision.
	 */
	public boolean equals(Object actual) {
		if (actual instanceof NumberToken) {
			NumberToken act = (NumberToken) actual;
			if (getLexeme().contains(".")) {
				if (delta < 0.0) {
					inferAbsoluteErrorBound(getLexeme());
				}
				Double d1 = Double.parseDouble(getLexeme());
				Double d2 = Double.parseDouble(act.getLexeme());
				return (Math.abs(d1 - d2) <= delta);
			} else {
				if (act.getLexeme().contains(".")) {
					// Integer to floating point comparison
					if (delta < 0.0) {
						return false;
					}
					Double d1 = Double.parseDouble(getLexeme());
					Double d2 = Double.parseDouble(act.getLexeme());
					return (Math.abs(d1 - d2) <= delta);
				} else {
					// Integer to integer comparison
					Integer n1 = Integer.parseInt(getLexeme());
					Integer n2 = Integer.parseInt(act.getLexeme());
					return n1.intValue() == n2.intValue();
				}
			}
		} else {
			return false;
		}
	}

	// Look at the number of digits after the decimal point to
	// infer the desired precision of comparison.
	private void inferAbsoluteErrorBound(String lexeme) {
		int decimalPos = lexeme.indexOf('.');
		delta = 1.0;
		int k = decimalPos + 1;
		while (k < lexeme.length()) {
			++k;
			delta /= 10.0;
		}
	}

	/**
	 * @return hash code.
	 */
	public int hashCode() {
		return getLexeme().hashCode();
	}
}
