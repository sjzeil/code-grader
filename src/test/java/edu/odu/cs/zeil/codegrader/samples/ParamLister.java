package edu.odu.cs.zeil.codegrader.samples;

public class ParamLister {

    public static void main(String[] args) {
        for (String a : args) {
            System.out.println(a);
        }
        System.err.println (args.length);
        System.out.flush();
        System.err.flush();
        System.exit ((args.length > 0) ? 0 : -1);
    }
}
