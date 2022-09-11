package edu.odu.cs.zeil.codegrader;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.odu.cs.zeil.codegrader.oracle.Oracle;
import edu.odu.cs.zeil.codegrader.oracle.SmartOracle;
import edu.odu.cs.zeil.codegrader.oracle.Oracle.ScoringOptions;

public class OracleProperties implements Cloneable {

    /**
	 * Max number of points scored by a test case.
	 */
	public static final int DEFAULT_POINT_CAP = 100;

    //CHECKSTYLE:OFF

    /**
     * Name of the oracle to be employed. This may be a shortcut for one
     * of the pre-supplied oracles or a fully qualified Java class name.
     * 
     * defaults to SmartOracle.
     */
    public String oracle;

    /**
     * Maximum percentage (0..100) available when applying this oracle.
     * Defaults to 100.
     */
    public int cap;

    /**
     * True iff ws is considered significant - a test can be failed because
     * of extra/missing/incorrect whitespace characters.
     * 
     * Default is false;
     */
    public boolean ws;

    /**
     * True iff upper/lower case is considered significant when examining
     * alphabetic strings - a test can be failed because of differences in
     * upper/lower case.
     * 
     * Default is false;
     */
    @JsonProperty("case")
    public boolean caseSig;

    /**
	 * Scoring option for this test case.
	 */
    public Oracle.ScoringOptions scoring;


	/**
	 * How far floating point numbers may vary and still be considered equal.
	 * If negative, the comparison precision is derived from the expected
	 * value. E.g., if the expected value is printed with 2 digits after the
	 * decimal point, then the precision is 0.01.
	 */
    public double precision;

	/**
	 * Are empty lines counted as part of the evaluation?
	 */
	public boolean emptyLines;

	/**
	 * Is punctuation checked during the evaluation?
	 */
	public boolean punctuation;

	/**
	 * Should only numbers be checked, ignoring everything else?
	 */
	public boolean numbersOnly;

	/**
	 * Command string to launch an external oracle.
	 */
	public String command;


    //CHECKSTYLE:ON



    /**
     * Create a set of oracle properties with the defaults preset.
     */
    public OracleProperties() {
        cap = DEFAULT_POINT_CAP;
        oracle = SmartOracle.class.getName();
        ws = false;
        caseSig = true;
        scoring = ScoringOptions.All;
        precision = -1.0;
        emptyLines = false;
        punctuation = true;
        numbersOnly = false;
        command = "";
    }

    /**
     * Make a copy of this set of properties.
     */
    @Override
    public OracleProperties clone() {
        OracleProperties result = new OracleProperties();
        result.cap = cap;
        result.oracle = oracle;
        result.ws = ws;
        result.caseSig = caseSig;
        result.scoring = scoring;
        result.precision = precision;
        result.emptyLines = emptyLines;
        result.punctuation = punctuation;
        result.numbersOnly = numbersOnly;
        result.command = command;

        return result;
    }



}
