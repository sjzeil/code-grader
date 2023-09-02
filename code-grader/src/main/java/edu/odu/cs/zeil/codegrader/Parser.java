package edu.odu.cs.zeil.codegrader;
public interface Parser<T> {

    /**
     * Convert a string into an object of the desired type.
     * @param input input string
     * @return value of the desired type
     */
    T parse(String input);
}
