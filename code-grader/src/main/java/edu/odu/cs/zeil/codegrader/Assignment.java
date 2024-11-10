package edu.odu.cs.zeil.codegrader;

import java.nio.file.Path;


/**
 * An Assignment indicates the various files and directories that will be
 * employed during grading.
 */
public class Assignment implements Cloneable {

    /**
     * Indicates that instructor is providing code that must be added to the
     * the submitter's code when compiling and/or running the submission.
     * 
     * This will be true if 
     *   1) the program is run with the -isrc parameter and the value is not '-'
     *   2) the program is run with the -gold parameter and the -isrc parameter
     *      is missing or not '-'.
     */
    private boolean hasInstructorCode;

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
     * Directory of CSV files containing manually assigned grades.
     */
    private Path manual;

    /**
     * Directory in which a submission will be compiled and tested.
     */
    private Path stagingDirectory;

    /**
     * Where to place information about the points earned for passing tests.
     */
    private Path recordingDirectory;

    /**
     * True if in-place grading is requested.  Code is already present in the
     * submissions directory, can be built (compiled) there and the test suite
     * directory is also the recording directory.
     * 
     * By default, this is false, meaning that code will be copied into a
     * separate temporary "stage" and the recording directory starts as a copy
     * of the test suite and then is augmented with test reports.
     */
    private boolean inPlace;

    private String theSelectedStudent;

    /**
     * Create an empty assignment.
     */
    public Assignment() {
        inPlace = false;
        hasInstructorCode = true;
        theSelectedStudent = "";
    }

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
        if (instructorCodeDirectory != null) {
            return instructorCodeDirectory;
        } else {
            return goldDirectory;
        }
    }

    /**
     * @return true if there is instructor code to be copied along with the
     * code from the submitter.
     */
    public boolean getHasInstructorCode() {
        return hasInstructorCode;
    }

    /**
     * Indicate whether instructor is supplying source code.
     * @param hasCode true if there is instructor code to be copied along
     *     with the code from the submitter.
     * @return the assignment
     */
    public Assignment setHasInstructorCode(boolean hasCode) {
        hasInstructorCode = hasCode;
        return this;
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
     * Instructors can assign manually assign grades to be included in the
     * report. These appear in ...TBD
     * 
     * @return the path to the manually assigned grades.
     */
    public Path getManual() {
        return manual;
    }

    /**
     * Set the path to the manually assigned grades.
     * 
     * @param path the path to use
     * @return the modified assignment
     */
    public Assignment setManual(final Path path) {
        manual = path;
        return this;
    }



    /**
     * Copy an assignment.
     */
    @Override
    public Assignment clone() {
        Assignment theClone;
        try {
            theClone = (Assignment) super.clone();
        } catch (CloneNotSupportedException e) {
            // Not possible.
            theClone = new Assignment();
        }
        theClone.setStagingDirectory(stagingDirectory)
            .setInstructorCodeDirectory(instructorCodeDirectory)
            .setRecordingDirectory(recordingDirectory)
            .setGoldDirectory(goldDirectory)
            .setTestSuiteDirectory(testSuiteDirectory)
            .setManual(manual)
            .setHasInstructorCode(hasInstructorCode)
            .setInPlace(inPlace);
        return theClone;
    }

    /**
     * Get the staging directory for the submitted version.
     * @param submission Identifies the submitter
     * @return a subdirectory within the staging area.
     */
    public Path getSubmitterStage(Submission submission) {
        if (inPlace) {
            return submissionsDirectory;
        } else {
            return stagingDirectory.resolve(submission.getSubmittedBy());
        }
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

   /**
    * Mark this assignment as being graded "in place", rather than using a
    * separate stage and recording area.
    *
    * Student code will be compiled and run in the submission directory
    * and grade info will be recorded in the test suite directory.
    * 
    * @param gradeInPlace true if this should be graded in place. False 
    *    (default) if the original submission directory and test suite 
    *    directory should left unchanged.
    */
    public void setInPlace(boolean gradeInPlace) {
        inPlace = gradeInPlace;
    }


    /**
    * Should this assignment be graded "in place", rather than using a
    * separate stage and recording area?
    *
    * @return true iff in-place grading is desired
    */
    public boolean getInPlace() {
        return inPlace;
    }

    public String getSelectedStudent() {
        return theSelectedStudent;
    }

    public void setSelectedStudent(String selected) {
        theSelectedStudent = selected;
    }


}
