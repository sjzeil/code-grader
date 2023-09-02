package edu.odu.cs.zeil.codegrader;

public class SubmissionDateOptions {

    public SubmissionDateOptions() {
        mod = "";
        in = "";
        git = true;
    }
    
    /**
     * Use the last modification date of the file with this path.
     */
    public String mod;
    /**
     * Use the contents of the file with this path.
     */
    public String in;
    /**
     * If true and if the submission directory is a git repository,
     * use the date of the last commit.  (defaults to true)
     */
    public boolean git;
}
