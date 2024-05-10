package edu.odu.cs.zeil.codegrader.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Lists all CLI parameters & std input lines, printing the number of
 * CLI parameters to the standard error stream.
 * 
 * If the first CLI parameter is the word "fail", then exits with a non-zero
 * status code.
 */
public final class ParamLister {

    // Assumes that "java" is in the PATH
    public static String launcher = "java -cp " 
        + System.getProperty("java.class.path") 
        + " edu.odu.cs.zeil.codegrader.samples.ParamLister";


    /**
     * Run the program.
     * @param args CLI parameters
     */
    public static void main(String[] args) {
        for (String a : args) {
            System.out.println(a);
        }
        System.err.println(args.length);
        BufferedReader in 
            = new BufferedReader((new InputStreamReader(System.in)));
        try {
            String line = in.readLine();
            while (line != null) {
                System.out.println(line);
                line = in.readLine();
            }
        } catch (IOException ex) {

        }
        System.out.flush();
        int statusCode = (args.length > 0 && args[0].equals("fail")) ? 212 : 0;
        if (statusCode != 0) {
            System.err.println("This is written to std err.");
        }
        System.err.flush();
        System.exit(statusCode);
    }

    private ParamLister() {
    }
}
