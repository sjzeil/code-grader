package edu.odu.cs.zeil.codegrader.samples;


public class CWDLister {

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        System.out.flush();
        System.err.flush();
        System.exit(0);
    }
}
