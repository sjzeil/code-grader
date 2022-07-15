package edu.odu.cs.zeil.codegrader;

import java.util.Optional;
import java.util.OptionalInt;

import com.fasterxml.jackson.annotation.JsonProperty;

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
     * Create a set of oracle properties with the defaults preset.
     */
    public OracleProperties() {
        cap = OptionalInt.of(100);
        oracle = Optional.of(SmartOracle.class.getName());
        ws = Optional.of(false);
    }

}
