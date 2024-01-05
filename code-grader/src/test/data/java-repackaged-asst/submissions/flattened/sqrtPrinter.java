/**
 * package foo;   I'm just being difficult here.
 */

// package bar;
package /* what will
           it be? */ pack1;

public class sqrtPrinter {

    private double x;

    public sqrtPrinter(double xx) {
        x = xx;
    }

    public void printSqrt()
    {
        double root = Math.sqrt(x);
        System.out.printf("The square root of %.2f is %.4f.%n",
             x, root);
    }
}
