package edu.odu.cs.zeil.codegrader.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ParamLister {

    public static void main(String[] args) {
        for (String a : args) {
            System.out.println(a);
        }
        System.err.println(args.length);
        BufferedReader in = new BufferedReader((new InputStreamReader(System.in)));
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
        System.err.flush();
        System.exit(statusCode);
    }
}
