package edu.odu.cs.zeil.codegrader.samples;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Writes its first CLI parameter into the file named in the 2nd CLI parameter.
 */
public final class ParamWriter{

    // Assumes that "java" is in the PATH
    public static String launcher = "java -cp " 
        + System.getProperty("java.class.path") 
        + " edu.odu.cs.zeil.codegrader.samples.ParamWriter";


    /**
     * Run the program.
     * @param args CLI parameters
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(args[1]));
        out.write(args[0]);
        out.newLine();
        out.close();
    }

}
