package edu.odu.cs.zeil.codegrader;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Submission represents a student's submitted solution to an assignment.
 */
public class Submission {

    private Assignment assignment;
    private String submittedBy;
    
    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Create a submission.
     * 
     * @param testDirectory
     */
    public Submission (Assignment assignment, String submittedBy) {
        this.assignment = assignment;
        this.submittedBy = submittedBy;
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
    Assignment getAssignment()
    {
        return assignment;
    }


}
