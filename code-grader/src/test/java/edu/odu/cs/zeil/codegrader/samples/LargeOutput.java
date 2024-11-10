package edu.odu.cs.zeil.codegrader.samples;


public class LargeOutput {

    public final static int OutputSize = 250000;
    public static void main(String[] args) throws InterruptedException {
        
        for (int i = 0; i < OutputSize / 10; ++i)
            System.out.println("abcdefghi"); 
        System.exit(0);
    }
}
