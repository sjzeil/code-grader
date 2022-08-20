package edu.odu.cs.zeil.codegrader;

import java.nio.file.Path;


/**
 * A Submission represents a student's submitted solution to an assignment.
 */
public class Submission {

    private Assignment assignment;
    private String submittedBy;
    

    /**
     * Create a submission.
     * 
     * @param assignment0 assignment that this is a submission to.
     * @param submittedBy0 who submitted this
     */
    public Submission(Assignment assignment0, String submittedBy0) {
        this.assignment = assignment0;
        this.submittedBy = submittedBy0;
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
    Path getSubmissionDirectory() {
        return assignment.getSubmissionsDirectory().resolve(submittedBy);
    }

    /**
     * 
     * @return the identifier of the submitter
     */
    String getSubmittedBy() {
        return submittedBy;
    }

    /**
     * 
     * @return the assignment to which this was submitted
     */
    Assignment getAssignment() {
        return assignment;
    }


    /**
     * @return The directory containing this submitter's own copy of
     *         the test suite (within the assignment recording directory).
     */
    Path getTestSuiteDir() {
        return getRecordingDir().resolve("TestCases");
    }

    /**
     * 
     * @param testCaseName a test case name
     * @return The directory containing this submitter's own copy of
     *      the test case.
     */
    Path getTestCaseDir(String testCaseName) {
        return getTestSuiteDir().resolve(testCaseName);
    }

    /**
     * @return the directory where the student's grade info will be written.
     */
    public Path getRecordingDir() {
		Path studentRecordingArea = assignment.getRecordingDirectory()
				.resolve(getSubmittedBy());
		return studentRecordingArea;
	}

    public String getSubmissionDate() {
        //TODO
        return "";
    }

    /**
	 * Returns the score for a test.
	 * @param testCaseName
	 * @return the score or -1 if the test has not been run.
	 */
	public int getScore(String testCaseName) {
		Path scoreFile = getTestCaseDir(testCaseName)
            .resolve(testCaseName + ".score");
        if (scoreFile.toFile().exists()) {
            String contents = FileUtils.readTextFile(scoreFile.toFile()).trim();
            try {
                int score = Integer.parseInt(contents);
                return score;
            } catch (NumberFormatException ex) {
                return -1;
            }
        } else {
            return -1;
        }
	}

    /**
	 * Returns the message, if any, for a test.
	 * @param testCaseName
	 * @return the message or "" if none exists or if the test has not been run.
	 */
	public String getMessage(String testCaseName) {
		Path msgFile = getTestCaseDir(testCaseName)
            .resolve(testCaseName + ".message");
        if (msgFile.toFile().exists()) {
            String contents = FileUtils.readTextFile(msgFile.toFile()).trim();
            return contents;
        } else {
            return "";
        }
	}



}
