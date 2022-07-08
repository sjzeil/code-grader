package edu.odu.cs.zeil.codegrader.oracle;

/**
 * A test case score generated by an oracle.
 */
public class OracleResult {
    public int score;  // a percentage 0..100
    public String message; // info indicating why a score was awarded

    public OracleResult (int scor, String msg) {
        score = scor;
        message = msg;
    }

    public String toString() {
        return "" + score + ": " + message;
    }
}
