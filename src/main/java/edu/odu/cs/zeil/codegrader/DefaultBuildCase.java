package edu.odu.cs.zeil.codegrader;

import java.util.Optional;
import java.util.OptionalInt;

public class DefaultBuildCase {

    private static final int DEFAULT_BUILDER_TIME_LIMIT = 600;
    private Assignment assignment;
    private TestSuiteProperties suite;

    /**
     * Create a descriptor for a default builder case.
     * @param properties properties of the test suite
     * @param asst the assignment this should build
     */
    public DefaultBuildCase(TestSuiteProperties properties, 
                            Assignment asst) {
        this.assignment = asst;
        this.suite = properties;
    }

    /**
     * Construct the actual test case properties for the default builder.
     * @return properties of a default builder.
     */
    public TestCaseProperties generate() {
        TestCaseProperties builder = new TestCaseProperties(assignment,
             "builder");
        builder.kind = Optional.of("build");
        builder.description = Optional.of("Attempt to build the program.");
        builder.expected = Optional.of("");
        OracleProperties oracle = new OracleProperties();
        oracle.oracle = "status";
        builder.grading.clear();
        builder.grading.add(oracle);
        if (suite.build.command == null || suite.build.command.equals("")) {
            builder.launch = Optional.of("");
        } else {
            builder.launch = Optional.of(suite.build.command);
        }
        builder.weight = OptionalInt.of(suite.build.weight);
        builder.params = Optional.of("");
        builder.expected = Optional.of("");
        builder.timelimit = OptionalInt.of(DEFAULT_BUILDER_TIME_LIMIT);
        builder.stderr = Optional.of(true);
        builder.status = Optional.of(true);
        /*
        builder.onSuccess.add("test");
        builder.onSuccess.add("buildOK");
        builder.onFailure.add("test");
        builder.onFailure.add("buildFailed");
        */
        return builder;
    }

}
