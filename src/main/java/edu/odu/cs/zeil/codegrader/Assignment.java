package edu.odu.cs.zeil.codegrader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * An Assignment indicates the various files and directories that will be
 * employed during grading.
 */
public class Assignment implements Cloneable {

    /**
     * The directory containing code supplied by the instructor.
     * (Often the same as the gold directory).
     */
    private Path instructorCodeDirectory;

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
     * The instructor code directory is the location of an instructor's code
     * intended to be included along with the student-supplied code as part of
     * the total build. When a gold version (instructor's solution) is
     * available, the instructor code directory will usually be the instructor
     * code directory as well.
     *
     * @return the path to the directory containing instructor-supplied
     *         code or null if no instructor's code is supplied.
     */
    public Path getInstructorCodeDirectory() {
        return instructorCodeDirectory;
    }

    /**
     * Set the path to the instructor code directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    public Assignment setInstructorCodeDirectory(final Path path) {
        instructorCodeDirectory = path;
        return this;
    }

    /**
     * The gold directory is the location of an instructor's solution to
     * the assignment. If available (non-null), the instructor's solution
     * is run prior to each test of the student's submission in order to collect
     * expected output and timing data.
     *
     * @return the path to the gold directory or null if no instructor's
     *         solution is being used.
     */
    public Path getGoldDirectory() {
        return goldDirectory;
    }

    /**
     * Set the path to the gold directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    public Assignment setGoldDirectory(final Path path) {
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
    public Path getSubmissionsDirectory() {
        return submissionsDirectory;
    }

    /**
     * Set the path to the submissions directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    public Assignment setSubmissionsDirectory(final Path path) {
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
    public Path getStagingDirectory() {
        return stagingDirectory;
    }

    /**
     * Set the path to the staging directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    public Assignment setStagingDirectory(final Path path) {
        stagingDirectory = path; 
        return this;
    }

    /**
     * The recording directory is the location where the student code will be
     * compiled and tested. 
     * 
     * @return the path to the recording directory.
     */
    public Path getRecordingDirectory() {
        return recordingDirectory;
    }

    /**
     * Set the path to the recording directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    public Assignment setRecordingDirectory(final Path path) {
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
    public Path getGradingTemplate() {
        return gradingTemplate;
    }

    /**
     * Set the path to the recording directory.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    public Assignment setGradingTemplate(final Path path) {
        gradingTemplate = path;
        return this;
    }

    /**
     * Parses a command line string, substituting paths for selected shortcut 
     * codes.
     * 
     * @see AssertionError#parameterSubstitution
     * @param launchCommandStr a command string
     * @param testName   name of test case, if applicable
     * @return list of tokens suitable for a ProcessBuilder
     */
    public List<String> parseCommandString(
            String launchCommandStr, 
            String testName) {
        launchCommandStr = parameterSubstitution(launchCommandStr, testName);
        List<String> launchCommand = parseCommand(launchCommandStr);
        return launchCommand;
    }


    /**
     * Scans a string for shortcuts, replacing by the appropriate string.
     * Shortcuts are
     * <ul>
     * <li>@S the staging directory</li>
     * <li>@T the test suite directory</li>
     * <li>@t the test case name</li>
     * <li>@R the reporting directory</li>
     * </ul>
     * A shortcut must be followed by a non-alphabetic character.
     * 
     * @param launchCommandStr a string describing a command to be run
     * @param testName name of the current test case, if applicable
     * @return the launchCommandStr with shortcuts replaced by the appropriate
     *         path/value
     */
    public String parameterSubstitution(
            String launchCommandStr, 
            String testName) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < launchCommandStr.length()) {
            char c = launchCommandStr.charAt(i);
            if (c == '@') {
                if (i + 1 < launchCommandStr.length()) {
                    char c2 = launchCommandStr.charAt(i + 1);
                    if (c2 == 'S' || c2 == 'T' || c2 == 't' || c2 == 'R') {
                        boolean ok = (i + 2 >= launchCommandStr.length())
                                || !Character.isAlphabetic(
                                        launchCommandStr.charAt(i + 2));
                        if (ok) {
                            i += 2;
                            try {
                                if (c2 == 'S') {
                                    result.append(
                                            getStagingDirectory()
                                                    .toRealPath().toString());
                                } else if (c2 == 'T') {
                                    result.append(
                                            getTestSuiteDirectory()
                                                    .toRealPath().toString());
                                } else if (c2 == 't') {
                                    result.append(testName);
                                } else if (c2 == 'R') {
                                    result.append(
                                            getRecordingDirectory()
                                                    .toRealPath().toString());
                                }
                            } catch (IOException ex) {
                                // Path has not been set
                                i -= 1;
                                result.append(c);
                            }
                        } else {
                            i += 1;
                            result.append(c);
                        }
                    } else {
                        i += 1;
                        result.append(c);
                    }
                } else {
                    result.append(c);
                    ++i;
                }
            } else {
                result.append(c);
                ++i;
            }
        }
        return result.toString();
    }


    /**
     * Split a string into space-separated tokens, respecting "" and '' quoted
     * substrings.
     * 
     * @param launch a command line
     * @return tokenized versions of the command line
     */
    private List<String> parseCommand(String launch) {
        ArrayList<String> result = new ArrayList<>();
        StringBuffer token = new StringBuffer();
        boolean inQuotes1 = false;
        boolean inQuotes2 = false;
        for (int i = 0; i < launch.length(); ++i) {
            char c = launch.charAt(i);
            if (c != ' ') {
                token.append(c);
                if (c == '"') {
                    if (inQuotes2) {
                        inQuotes2 = false;
                    } else if (!inQuotes1) {
                        inQuotes2 = true;
                    }
                } else if (c == '\'') {
                    if (inQuotes1) {
                        inQuotes1 = false;
                    } else if (!inQuotes2) {
                        inQuotes1 = true;
                    }
                }
            } else {
                if (inQuotes1 || inQuotes2) {
                    token.append(c);
                } else {
                    result.add(token.toString());
                    token = new StringBuffer();
                }
            }
        }
        if (token.length() > 0) {
            result.add(token.toString());
        }
        return result;
    }




    /**
     * Copy an assignment.
     */
    @Override
    public Assignment clone() {
        Assignment theClone = new Assignment();
        theClone.setStagingDirectory(stagingDirectory)
            .setInstructorCodeDirectory(instructorCodeDirectory)
            .setRecordingDirectory(recordingDirectory)
            .setGoldDirectory(goldDirectory)
            .setTestSuiteDirectory(testSuiteDirectory)
            .setGradingTemplate(gradingTemplate);
        return theClone;
    }

    /**
     * Get the staging directory for the submitted version.
     * @return a subdirectory within the staging area.
     */
    public Path getSubmitterStage() {
        return stagingDirectory.resolve("submission");
   }

   /**
     * Get the staging directory for the gold version.
     * @return a subdirectory within the staging area or null if no gold
     *       version is supplied.
     */
    public Path getGoldStage() {
        if (goldDirectory != null) {
            return stagingDirectory.resolve("gold");
        } else {
            return null;
        }
   }
}
