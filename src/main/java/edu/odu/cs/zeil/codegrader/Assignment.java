package edu.odu.cs.zeil.codegrader;

import java.nio.file.Path;


/**
 * An Assignment indicates the various files and directories that will be
 * employed during grading.
 */
public class Assignment {

    /**
     * Instructor's code.
     */
    private Path goldDirectory;
    /**
     * Submissions from all students.
     */
    private Path submissionsDirectory;
    /**
     * Collection of all test cases.
     */
    private Path testSuiteDirectory;
    /**
     * Spreadsheet for performing grading calculations.
     */
    private Path gradingTemplate;

    /**
     * Directory in which a submission will be compiled and tested.
     */
    private Path stagingDirectory;

    /**
     * Where to place information about the points earned for passing tests.
     */
    private Path recordingDirectory;


    /**
     * The gold directory is the location of an instructor's solution to
     * the assignment. If available (non-null), the instructor's solution
     * is run prior to each test of the student's submission in order to collect
     * expected output and timing data.
     *
     * @return the path to the gold directory or null if no instructor's
     *         solution is being used.
     */
    Path getGoldDirectory() {
        return goldDirectory;
    }

    /**
     * Set the path to the gold directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    Assignment setGoldDirectory(final Path path) {
        goldDirectory = path;
        return this;
    }

    /**
     * The submissions directory is the location of the collected submissions
     * from an entire class. Each subdirectory of this one represents one 
     * submission and should be named with the identifier of the student who
     * submitted it.
     * 
     * @return the path to the submissions directory.
     */
    Path getSubmissionsDirectory() {
        return submissionsDirectory;
    }

    /**
     * Set the path to the submissions directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    Assignment setSubmissionsDirectory(final Path path) {
        submissionsDirectory = path;
        return this;
    }

    /**
     * The test suite directory is the location of the test suite
     * that will be used to evaluate the submissions.
     * Each subdirectory of this one represents one test case
     * and should be named with the identifier of the test case.
     * 
     * @return the path to the test suite directory.
     */
    public Path getTestSuiteDirectory() {
        return testSuiteDirectory;
    }

    /**
     * Set the path to the test suite directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    public Assignment setTestSuiteDirectory(final Path path) {
        testSuiteDirectory = path; return this;
    }

    /**
     * The staging directory is the location where the student code will be
     * compiled and tested. 
     * 
     * @return the path to the staging directory.
     */
    Path getStagingDirectory() {
        return stagingDirectory;
    }

    /**
     * Set the path to the staging directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    Assignment setStagingDirectory(final Path path) {
        stagingDirectory = path; 
        return this;
    }

    /**
     * The recording directory is the location where the student code will be
     * compiled and tested. 
     * 
     * @return the path to the recording directory.
     */
    Path getRecordingDirectory() {
        return recordingDirectory;
    }

    /**
     * Set the path to the recording directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    Assignment setRecordingDirectory(final Path path) {
        recordingDirectory = path;
        return this;
    }

    /**
     * The grading template is an Excel spreadsheet in which the instructor
     * can specify how an overall grade for the assignment is to be computed.
     * 
     * A report is generated from this spreadsheet for each submission and
     * placed in the recording directory.
     * 
     * @return the path to the grading template.
     */
    Path getGradingTemplate() {
        return gradingTemplate;
    }

    /**
     * Set the path to the recording directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    Assignment setGradingTemplate(final Path path) {
        gradingTemplate = path;
        return this;
    }


}
