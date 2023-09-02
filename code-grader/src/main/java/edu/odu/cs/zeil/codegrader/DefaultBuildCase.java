package edu.odu.cs.zeil.codegrader;

import java.util.ArrayList;

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
        builder.setKind("build");
        builder.setDescription("Attempt to build the program.");
        builder.setExpected("");
        OracleProperties oracle = new OracleProperties();
        oracle.oracle = "status";
        builder.grading.clear();
        builder.grading.add(oracle);
        if (suite.build.command == null || suite.build.command.equals("")) {
            builder.setLaunch("");
        } else {
            builder.setLaunch(suite.build.command);
        }
        builder.setWeight(suite.build.weight);
        builder.setParams("");
        builder.setExpected("");
        builder.setTimelimit(DEFAULT_BUILDER_TIME_LIMIT);
        builder.setStderr(true);
        builder.setStatus(true);
        
        builder.onSuccess = new ArrayList<>();
        builder.onSuccess.add("test");
        builder.onSuccess.add("buildOK");
        builder.onFail = new ArrayList<>();
        builder.onFail.add("test");
        builder.onFail.add("buildFailed");
        
        return builder;
    }

}
