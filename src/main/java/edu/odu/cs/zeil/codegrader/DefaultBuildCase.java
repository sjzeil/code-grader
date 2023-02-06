package edu.odu.cs.zeil.codegrader;

import java.util.Optional;
import java.util.OptionalInt;

import edu.odu.cs.zeil.codegrader.oracle.StatusOracle;

public class DefaultBuildCase {

    private Stage stage;
    private Assignment asst;
    private Submission submission;
    private TestSuiteProperties suite;

    public DefaultBuildCase(TestSuiteProperties properties, 
                            Stage stage,
                            Assignment asst,
                            Submission submission) {
        this.asst = asst;
        this.stage = stage;
        this.submission = submission;
        this.suite = properties;
    }

    public TestCaseProperties generate() {
        TestCaseProperties builder = new TestCaseProperties(asst, "builder");
        builder.kind = Optional.of("build");
        builder.description = Optional.of("Attempt to build the program.");
        builder.expected = Optional.of("");
        OracleProperties oracle = new OracleProperties();
        oracle.oracle = "status";
        builder.grading.add(oracle);
        if (suite.build.command == null || suite.build.command.equals("")) {
            builder.launch = Optional.of(stage.getBuildCommand());
        } else {
            builder.launch = Optional.of(suite.build.command);
        }
        builder.weight = OptionalInt.of(suite.build.weight);
        builder.params = Optional.of("");
        builder.expected = Optional.of("");
        builder.timelimit = OptionalInt.of(suite.build.timeLimit);
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
