package edu.odu.cs.zeil.codegrader;

public class Pair<First, Second> {

    private First x;
    private Second y;

    /**
     * Create a pair.
     * @param first first item in pair
     * @param second item in pair
     */
    public Pair(First first, Second second) {
        x = first;
        y = second;
    }

    /**
     * @return the first item
     */
    public First getFirst() {
        return x;
    }

    /**
     * @return the second item
     */
    public Second getSecond() {
        return y;
    }
}
