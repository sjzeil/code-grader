package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDeferred {

    private Path asstSrcPath = Paths.get("src", "test", "data",
            "assignment2");
    private Path testsSrcPath = asstSrcPath.resolve("tests");
    private Path paramsTestPath = testsSrcPath.resolve("params");


    /**
     * Set up assignment2 params test.
     * 
     * @throws IOException
     * @throws TestConfigurationError
     */
    @BeforeEach
    public void setup() throws IOException, TestConfigurationError {
    }


    @Test
    void testDeferredString() {
        Deferred<String> defStr 
            = new Deferred<String>("params", "default",
                new StringParser());
        assertThat(defStr.get(paramsTestPath), is("a b c\n"));

        Deferred<String> defStr2 
            = new Deferred<String>("params0", "default",
                    new StringParser());
        assertThat(defStr2.get(paramsTestPath), is("default"));

    }

    @Test
    void testDeferredInteger() {
        final int defaultVal = 10;
        Deferred<Integer> deferred 
            = new Deferred<Integer>("timelimit", defaultVal,
                new IntegerParser());
        assertThat(deferred.get(paramsTestPath), is(2));

        Deferred<Integer> deferred2 
            = new Deferred<Integer>("bogus", defaultVal,
                    new IntegerParser());
        assertThat(deferred2.get(paramsTestPath), is(defaultVal));

        Deferred<Integer> deferred3 
            = new Deferred<Integer>("params", defaultVal,
                    new IntegerParser());
        assertNull(deferred3.get(paramsTestPath));
    }

    @Test
    void testDeferredBoolean() {
        Deferred<Boolean> deferred 
            = new Deferred<Boolean>("timelimit", false,
                new BooleanParser());
        assertThat(deferred.get(paramsTestPath), is(true));

        Deferred<Boolean> deferred2 
            = new Deferred<Boolean>("bogus", false,
                    new BooleanParser());
        assertThat(deferred2.get(paramsTestPath), is(false));

        Deferred<Boolean> deferred3 
            = new Deferred<Boolean>("params", false,
                    new BooleanParser());
        assertNull(deferred3.get(paramsTestPath));
    }

}
