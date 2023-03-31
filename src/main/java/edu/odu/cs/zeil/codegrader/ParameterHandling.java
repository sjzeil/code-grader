package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

public class ParameterHandling {


    private HashMap<Character, String> replacements;

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

        Path stageDir = (stage != null) ? stage.getStageDir() : null;
        replacements = new HashMap<>();
        Path testSuiteDir = asst.getTestSuiteDirectory();
        String tcName = (tc == null) ? "" : tc.getProperties().name;
        String recordingDir = (submission == null) ? "" 
            : submission.getRecordingDir().toAbsolutePath().toString();
        String submissionDir = (submission == null) ? ""
                : submission.getSubmissionDirectory()
                    .toAbsolutePath().toString();
        replacements.put('P', 
            (tc == null) ? "" : tc.getProperties().getParams());
        if (stageDir != null) {
            replacements.put('S', stageDir.toAbsolutePath().toString());
        }
        if (submission != null) {
            replacements.put('s', submission.getSubmittedBy());
        }
        replacements.put('I', submissionDir);
        replacements.put('T', 
            testSuiteDir.resolve(tcName).toAbsolutePath().toString());
        replacements.put('t', tcName);
        replacements.put('R', recordingDir);
        replacements.put('A', 
            (actual == null) ? "" : actual.getAbsolutePath());
        replacements.put('E', 
            (expected == null) ? "" : expected.getAbsolutePath());
    }

    /**
     * Scans a string for shortcuts, replacing by the appropriate string.
     * Shortcuts are
     * <ul>
     * <li>@P the test command line parameters</li>
     * <li>@S the staging directory</li>
     * <li>@s the name/ID of the submitter</li>
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
                            result.append(replacements.get('P'));
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
                    String replacement = replacements.get(c2);
                    if (replacement != null) {
                        i += 2;
                        result.append(replacement);
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
