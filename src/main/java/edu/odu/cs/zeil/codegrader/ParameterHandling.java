package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

public class ParameterHandling {


    private char[] tags = {'P', 'S', 'I', 'T', 't', 'R', 'A', 'E'};
    private ArrayList<String> replacements;

    /**
     * Set up an object for parameter substitution .
     * @param asst the assignment
     * @param tc the test case. If null, only S & R substitutions are allowed.
     * @param stage the stage where related code has been built
     * @param submission the submission being evaluated. If null, @R is 
     *          not allowed.
     * @param expected a file of expected output. If null, @E is not allowed.
     * @param actual a file of actual output. If null, @A is not allowed.
     */
    public ParameterHandling(Assignment asst, TestCase tc, 
            Stage stage, Submission submission,
            File expected, File actual) {

        Path stageDir = stage.getStageDir();
        replacements = new ArrayList<>();
        Path testSuiteDir = asst.getTestSuiteDirectory();
        String tcName = (tc == null) ? "" : tc.getProperties().getName();
        String recordingDir = (submission == null) ? "" 
            : submission.getRecordingDir().toAbsolutePath().toString();

        replacements.add((tc == null) 
            ? "" 
            : tc.getProperties().getParams()); // P
        replacements.add(stageDir.toAbsolutePath().toString()); // S
        replacements.add(submission.getSubmissionDirectory()
            .toAbsolutePath().toString()); // I
        replacements.add(testSuiteDir.resolve(tcName)
            .toAbsolutePath().toString()); // T
        replacements.add(tcName); // t
        replacements.add(recordingDir); // R
        replacements.add((actual == null)
                ? ""
                : actual.getAbsolutePath()); // A
        replacements.add((expected == null)
                ? ""
                : expected.getAbsolutePath()); // E
    }

    /**
     * Scans a string for shortcuts, replacing by the appropriate string.
     * Shortcuts are
     * <ul>
     * <li>@P the test command line parameters</li>
     * <li>@S the staging directory</li>
     * <li>@T the test suite directory</li>
     * <li>@t the test case name</li>
     * <li>@R the reporting directory</li>
     * <li>@A a file containing the actual/observed output</li>
     * <li>@E a file containing the expected output</li>
     * </ul>
     * A shortcut must be followed by a non-alphabetic character.
     * 
     * @param launchCommandStr a string describing a command to be run
     * @return the launchCommandStr with shortcuts replaced by the appropriate
     *         path/value
     */
    public String parameterSubstitution(String launchCommandStr) {

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < launchCommandStr.length()) {
            char c = launchCommandStr.charAt(i);
            if (c == '@') {
                if (i + 1 < launchCommandStr.length()) {
                    char c2 = launchCommandStr.charAt(i + 1);
                    if (c2 == 'P') {
                        boolean ok = (i + 2 >= launchCommandStr.length())
                                || !Character.isAlphabetic(
                                        launchCommandStr.charAt(i + 2));
                        if (ok) {
                            i += 2;
                            result.append(replacements.get(0));
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
        launchCommandStr = result.toString();
        result = new StringBuilder();
        i = 0;
        while (i < launchCommandStr.length()) {
            char c = launchCommandStr.charAt(i);
            if (c == '@') {
                if (i + 1 < launchCommandStr.length()) {
                    char c2 = launchCommandStr.charAt(i + 1);
                    int selection = 0;
                    while (selection < tags.length && c2 != tags[selection]) {
                        ++selection;
                    }
                    if (selection < tags.length) {
                        boolean ok = (i + 2 >= launchCommandStr.length())
                                || !Character.isAlphabetic(
                                        launchCommandStr.charAt(i + 2));
                        if (ok) {
                            i += 2;
                            result.append(replacements.get(selection));
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
}
