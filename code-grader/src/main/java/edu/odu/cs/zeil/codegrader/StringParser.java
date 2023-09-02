package edu.odu.cs.zeil.codegrader;
public class StringParser implements Parser<String> {

    private boolean shouldBeTrimmed;

    /**
     * Create a string parser in which all whitespace is included in the value.
     */
    public StringParser() {
        shouldBeTrimmed = false;
    }

    /**
     * Create a string parser with the option of trimming leading and ending
     * whitespace.
     * @param trim should whitespace be trimmed?
     */
    public StringParser(boolean trim) {
        shouldBeTrimmed = trim;
    }

    /**
     * Convert a string into an object of the desired type.
     * @param input input string
     * @return value of the desired type
     */
    public String parse(String input) {
        if (shouldBeTrimmed) {
            return input.trim();
        } else {
            return input;
        }
    }
}
