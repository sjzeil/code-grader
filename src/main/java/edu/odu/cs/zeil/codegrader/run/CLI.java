package edu.odu.cs.zeil.codegrader.run;

import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;

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
                System.exit(0);
            }

            if (cli.hasOption(suite)) {
                assignment.setTestSuiteDirectory(
                    Paths.get(cli.getOptionValue(suite)));
            } else {
                logger.error("Test suite not specified.");
                System.exit(2);
            }

            if (cli.hasOption(iSrc)) {
                assignment.setInstructorCodeDirectory(
                    Paths.get(cli.getOptionValue(iSrc)));
            }

            if (cli.hasOption(stage)) {
                assignment.setStagingDirectory(
                    Paths.get(cli.getOptionValue(stage)));
            } else {
                logger.error("Staging area not specified.");
                System.exit(3);
            }

            if (cli.hasOption(gold)) {
                assignment.setGoldDirectory(
                    Paths.get(cli.getOptionValue(gold)));
            }

            if (cli.hasOption(submissions)) {
                assignment.setSubmissionsDirectory(
                    Paths.get(cli.getOptionValue(submissions)));
            } else {
                logger.error("Submissions area not specified.");
                System.exit(4);
            }

            if (cli.hasOption(recording)) {
                assignment.setRecordingDirectory(
                    Paths.get(cli.getOptionValue(recording)));
            } else {
                logger.error("Recording area not specified.");
                System.exit(3);
            }

            if (cli.hasOption(gradeSheet)) {
                assignment.setGradingTemplate((
                    Paths.get(cli.getOptionValue(gradeSheet))));
            }

            if (cli.hasOption(student)) {
                selectedStudent = cli.getOptionValue(student);
            }
            if (cli.hasOption(test)) {
                selectedTest = cli.getOptionValue(test);
            }

        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            System.exit(1);
        }
    }

    /**
     * Run the grader via CLI arguments.
     * @param args command lien arguments
     */
    public static void main(String[] args) {
        CLI run = new CLI(args);
        run.go();
    }

    private void go() {
        TestSuite testSuite = new TestSuite(assignment);
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
        suite = Option.builder("suite")
            .argName("path")
            .hasArgs()
            .desc("Location of test suite directory")
            .build();
        stage = Option.builder("stage")
            .argName("path")
            .hasArgs()
            .desc("Location of staging area directory")
            .build();
        recording = Option.builder("recording")
            .argName("path")
            .hasArgs()
            .desc("Directory in which to record results")
            .build();
        submissions = Option.builder("submissions")
            .argName("path")
            .hasArgs()
            .desc("Directory containing student submissions")
            .build();
        gold = Option.builder("gold")
            .argName("path")
            .hasArgs()
            .desc("Directory containing instructor's solution"
                + " (optional)")
            .build();
        gradeSheet = Option.builder("gradesheet")
            .argName("path")
            .hasArgs()
            .desc("Excel spreadsheet for computing student's assignment grades."
                + " (optional)")
            .build();
        gradeSheet = Option.builder("student")
            .argName("identifier")
            .hasArgs()
            .desc("Which student's submission to grade " 
            + "(optional, defaults to all)")
            .build();
        test = Option.builder("test")
            .argName("identifier")
            .hasArgs()
            .desc("Which test case to run " 
                + "(optional, defaults to all)")
            .build();
        help = Option.builder("help")
            .desc("Print CLI help")
            .build();
        return result;
    }
}
