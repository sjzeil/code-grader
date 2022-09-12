package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A collection of submissions.
 */
public class SubmissionSet implements Iterable<Submission> {

    private Assignment assignment;
    private List<Submission> submissions;
    private Set<String> submissionsFilter;
    private List<Submission> submissionsToRun;

    /**
     * Create a submission set.
     * 
     * @param theAssignment assignment that these are submissions to.
     */
    public SubmissionSet(Assignment theAssignment) {
        this.assignment = theAssignment;
        submissions = new ArrayList<>();
        submissionsFilter = new HashSet<>();
        findSubmissions();
        submissionsToRun = submissions;
    }

    private void findSubmissions() {
        Path submissionsDir = assignment.getSubmissionsDirectory();
        if (assignment.getInPlace()) {
            submissions.add(new Submission(assignment,
                    submissionsDir.toFile().getName(),
                    submissionsDir));
        } else {
            File[] submissionFiles = submissionsDir.toFile().listFiles();
            if (submissionFiles == null || submissionFiles.length == 0) {
                throw new TestConfigurationError(
                        "No submission directories in "
                                + submissionsDir.toString());
            }
            Arrays.sort(submissionFiles);
            for (File submissionFile : submissionFiles) {
                if (isAValidSubmission(submissionFile)) {
                    String submittedBy = submissionFile.getName();
                    submissions.add(new Submission(assignment, submittedBy,
                        submissionFile.toPath()));
                }
            }
        }
    }

    /**
     * Provide access to submissions within this set.
     */
    @Override
    public Iterator<Submission> iterator() {
        return submissionsToRun.iterator();
    }

    /**
     * Limits the set of submissions that will actually be run.
     * 
     * @param submissionList list of submission names
     */
    public void setSelectedSubmissions(Set<String> submissionList) {
        submissionsFilter.clear();
        submissionsFilter.addAll(submissionList);
        if (submissionsFilter.size() > 0) {
            submissionsToRun = new ArrayList<>();
            for (Submission submission : submissions) {
                if (submissionsFilter.contains(submission.getSubmittedBy())) {
                    submissionsToRun.add(submission);
                }
            }
        } else {
        submissionsToRun = submissions;
    }
    }

    /**
     * 
     * @param submissionFile possible directory containing a submission
     * @return true if this is a directory and it contains a submission that
     *         we want tested.
     */
    private boolean isAValidSubmission(File submissionFile) {
        if (!submissionFile.isDirectory()) {
            return false;
        }
        String submissionName = submissionFile.getName();
        return (submissionsFilter.size() == 0
                || submissionsFilter.contains(submissionName));

    }

}
