public class computeSqrt {

    public static void main(String[] args) {
        Double x = Double.parseDouble(args[0]);
        double y = Math.sqrt(x);
        System.out.printf("the square root of %.2f is %.4f%n", x, y);
    }

}
