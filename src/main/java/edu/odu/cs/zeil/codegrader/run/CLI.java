package edu.odu.cs.zeil.codegrader.run;

import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;
import edu.odu.cs.zeil.codegrader.TestSuite;

public class CLI {

    private Assignment assignment;

    private Option iSrc;
    private Option suite;
    private Option stage;
    private Option submissions;
    private Option recording;
    private Option gold;

    private Option gradeSheet;

    private Option student;
    private Option test;

    private Option help;

    private String selectedStudent;

    private String selectedTest;

    /**
     * Error logging.
     */
    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /**
     * Create the CLI processor.
     * 
     * @param args command-line arguments
     */
    public CLI(String[] args) {
        assignment = new Assignment();
        CommandLineParser parser = new DefaultParser();
        Options options = setUpOptions();

        try {
            // parse the command line arguments
            CommandLine cli = parser.parse(options, args);

            if (cli.hasOption(help)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(
                    "java -cp path-to-jar edu.odu.cs.zeil.codegrader.run.CLI",
                    options);
                return;
            }

            if (cli.hasOption(suite)) {
                assignment.setTestSuiteDirectory(
                        Paths.get(cli.getOptionValue(suite)));
            } else {
                String msg = "Test suite not specified.";
                logger.error(msg);
                throw new TestConfigurationError(msg);
            }

            if (cli.hasOption(iSrc)) {
                assignment.setInstructorCodeDirectory(
                        Paths.get(cli.getOptionValue(iSrc)));
            }

            if (cli.hasOption(recording)) {
                assignment.setRecordingDirectory(
                        Paths.get(cli.getOptionValue(recording))
                            .resolve("grades"));
            } else {
                String msg = "Recording area not specified.";
                logger.error(msg);
                throw new TestConfigurationError(msg);
            }

            if (cli.hasOption(stage)) {
                assignment.setStagingDirectory(
                        Paths.get(cli.getOptionValue(stage)));
            } else {
                assignment.setStagingDirectory(
                    assignment.getRecordingDirectory().resolve("stage")
                );
            }

            if (cli.hasOption(gold)) {
                assignment.setGoldDirectory(
                        Paths.get(cli.getOptionValue(gold)));
            }

            if (cli.hasOption(submissions)) {
                assignment.setSubmissionsDirectory(
                        Paths.get(cli.getOptionValue(submissions)));
            } else {
                String msg = "Submissions area not specified.";
                logger.error(msg);
                throw new TestConfigurationError(msg);
            }


            if (cli.hasOption(gradeSheet)) {
                assignment.setGradingTemplate(
                    (Paths.get(cli.getOptionValue(gradeSheet))));
            }

            if (cli.hasOption(student)) {
                selectedStudent = cli.getOptionValue(student);
            } else {
                selectedStudent = "";
            }
            if (cli.hasOption(test)) {
                selectedTest = cli.getOptionValue(test);
            } else {
                selectedTest = "";
            }

        } catch (ParseException exp) {
            throw new TestConfigurationError(exp.getMessage());
        }
    }

    /**
     * Run the grader via CLI arguments.
     * 
     * @param args command lien arguments
     */
    public static void main(String[] args) {
        CLI run = new CLI(args);
        run.go();
    }

    /**
     * Run the CLI handler, launching the program.
     */
    public void go() {
        TestSuite testSuite = new TestSuite(assignment);
        if (!selectedTest.equals("")) {
            List<String> selections = new ArrayList<>();
            selections.add(selectedTest);
            testSuite.setSelectedTests(selections);
        }
        if (!selectedStudent.equals("")) {
            List<String> submissionList = new ArrayList<>();
            submissionList.add(selectedStudent);
            testSuite.setSelectedSubmissions(submissionList);
        }
        testSuite.performTests();
        System.out.println("Done");
    }

    private Options setUpOptions() {
        Options result = new Options();

        iSrc = Option.builder("isrc")
                .argName("path")
                .hasArgs()
                .desc("Location of instructor's source code (optional)")
                .build();
        result.addOption(iSrc);
        suite = Option.builder("suite")
                .argName("path")
                .hasArgs()
                .desc("Location of test suite directory")
                .build();
        result.addOption(suite);
        stage = Option.builder("stage")
                .argName("path")
                .hasArgs()
                .desc("Location of staging area directory")
                .build();
        result.addOption(stage);
        recording = Option.builder("recording")
                .argName("path")
                .hasArgs()
                .desc("Directory in which to record results")
                .build();
        result.addOption(recording);
        submissions = Option.builder("submissions")
                .argName("path")
                .hasArgs()
                .desc("Directory containing student submissions")
                .build();
        result.addOption(submissions);
        gold = Option.builder("gold")
                .argName("path")
                .hasArgs()
                .desc("Directory containing instructor's solution"
                        + " (optional)")
                .build();
        result.addOption(gold);
        gradeSheet = Option.builder("gradesheet")
                .argName("path")
                .hasArgs()
                .desc("Excel spreadsheet for computing assignment grades."
                        + " (optional)")
                .build();
        result.addOption(gradeSheet);
        student = Option.builder("student")
                .argName("identifier")
                .hasArgs()
                .desc("Which student's submission to grade "
                        + "(optional, defaults to all)")
                .build();
        result.addOption(student);
        test = Option.builder("test")
                .argName("identifier")
                .hasArgs()
                .desc("Which test case to run "
                        + "(optional, defaults to all)")
                .build();
        result.addOption(test);
        help = Option.builder("help")
                .desc("Print CLI help")
                .build();
        result.addOption(help);
        return result;
    }
}
