public class sqrtProg {

    private double x;

    public sqrtProg(double xx) {
        x = xx;
    }

    public void printSqrt()
    {
        throw new ArithmeticException("This is broken.");
    }

    static public void main(String[] args) {
        if (args.length != 1) {
            System.err.println ("Usage: java sqrtProg x");
            System.err.println ("  where x is a non-negative number.");
            System.exit(1);
        }
        double x = Double.parseDouble(args[0]);
        if (x < 0.0) {
            System.err.println ("Number must be non-negative");
            System.exit(2);
        }
        new sqrtProg(x).printSqrt();
    }
}
