import java.util.Random;

public class computeSqrt {

    public static void main(String[] args) {
        Random rand = new Random();
        Double x = Double.parseDouble(args[0]);
        double y = 0.0;
        while (y * y != x) {
            y = (double) (rand.nextInt() % 10000);
            y = y / 1000.0;
        }
        System.out.printf("The square root of %.2f is %.4f%n", x, y);
    }

}
