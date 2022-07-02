package edu.odu.cs.zeil.codegrader.samples;


public class SlowProgram {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(10000);
        System.out.println("OK");
        System.exit(0);
    }
}
