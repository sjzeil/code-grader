package edu.odu.cs.zeil.codegrader;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.odu.cs.zeil.codegrader.oracle.Oracle;
import edu.odu.cs.zeil.codegrader.oracle.SmartOracle;

public class OracleProperties {

    /**
     * Name of the oracle to be employed. This may be a shortcut for one
     * of the pre-supplied oracles or a fully qualified Java class name.
     * 
     * defaults to SmartOracle.
     */
    public Optional<String> oracle;

    /**
     * Maximum percentage (0..100) available when applying this oracle.
     * Defaults to 100.
     */
    public OptionalInt cap;

    /**
     * True iff ws is considered significant - a test can be failed because
     * of extra/missing/incorrect whitespace characters.
     * 
     * Default is false;
     */
    public Optional<Boolean> ws;

    /**
     * True iff upper/lower case is considered significant when examining
     * alphabetic strings - a test can be failed because of differences in
     * upper/lower case.
     * 
     * Default is false;
     */
    @JsonProperty("case")
    public Optional<Boolean> caseSig;

    /**
	 * Scoring option for this test case.
	 */
    public Optional<Oracle.ScoringOptions> scoring;


	/**
	 * How far floating point numbers may vary and still be considered equal.
	 * If negative, the comparison precision is derived from the expected
	 * value. E.g., if the expected value is printed with 2 digits after the
	 * decimal point, then the precision is 0.01.
	 */
    public OptionalDouble precision;

	/**
	 * Are empty lines counted as part of the evaluation?
	 */
	public Optional<Boolean> emptylines;

	/**
	 * Is punctuation checked during the evaluation?
	 */
	public Optional<Boolean> punctuation;

	/**
	 * Should only numbers be checked, ignoring everything else?
	 */
	public Optional<Boolean> numbersonly;

	/**
	 * Command string to launch an external oracle.
	 */
	public Optional<String> command;





    /**
     * Create a set of oracle properties with the defaults preset.
     */
    public OracleProperties() {
        cap = OptionalInt.of(100);
        oracle = Optional.of(SmartOracle.class.getName());
        ws = Optional.of(false);
    }

}
