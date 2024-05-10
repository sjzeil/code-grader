package edu.odu.cs.zeil.codegrader.samples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Compares two files, line by line, to see if they match. Counts the number
 * of non-matching lines. This number becomes the exit status code of the rpogram.
 */
public final class DiffByLine {

    /**
     * Run the program.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        BufferedReader in1 = new BufferedReader(new FileReader(args[0]));
        BufferedReader in2 = new BufferedReader(new FileReader(args[1]));
        int counter = 0;
        String line1 = null;
        String line2 = null;
        while (true) {
            line1 = in1.readLine();
            if (line1 == null) break;
            line2 = in2.readLine();
            if (line2 == null) break;
            if (!line1.equals(line2)) {
                ++counter;
            }
        }
        while (line1 != null) {
            ++counter;
            line1 = in1.readLine();
        }
        while (line2 != null) {
            ++counter;
            line2 = in2.readLine();
        }
        in1.close();
        in2.close();
        if (counter > 0) {
            System.exit(counter);
        }
    }
}
