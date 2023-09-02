package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Optional;

public class Deferred<T> {

    private String theExtension;
    private T defaultValue;
    private Parser<T> reader;
    private WeakReference<T> resolved;

    /**
     * Provide a value of type either as an explicitly assigned default
     * or by reading it, on demand, from a file with a given extension.
     * 
     * @param extension  the extension of the file to look for, not including
     *                   the '.'. The first such file found in the directory
     *                   will be used.
     * @param defaultVal the value to supply if no such file exists.
     * @param parser     An object capable of reading the desired value from a
     *                   String.
     */
    public Deferred(String extension,
            T defaultVal, Parser<T> parser) {
        theExtension = extension;
        defaultValue = defaultVal;
        reader = parser;
        resolved = new WeakReference<T>(null);
    }

    /**
     * Provide the desired value.
     * 
     * @param dir directory from which to seek and, if possible, read a value
     * @return the value
     */
    public T get(Path dir) {
        T result = resolved.get();
        if (result == null) {
            if (reader != null) {
                Optional<File> input = FileUtils.findFile(dir, theExtension);
                if (input.isPresent()) {
                    String inputVal = FileUtils.readTextFile(input.get());
                    result = reader.parse(inputVal);
                    resolved = new WeakReference<T>(result);
                } else {
                    result = defaultValue;
                    resolved = new WeakReference<T>(result);
                }
            } else {
                result = defaultValue;
                resolved = new WeakReference<T>(result);
            }
        }
        return result;
    }

    /**
     * Set the value (overrides any values in files).
     * 
     * @param value value to set
     */
    public void set(T value) {
        resolved = new WeakReference<T>(value);
        defaultValue = value;
        reader = null;
    }

}
