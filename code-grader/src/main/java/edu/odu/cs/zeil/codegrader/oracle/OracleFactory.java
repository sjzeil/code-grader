package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;

public final class OracleFactory {

    //private static final int NUM_CONSTRUCTOR_PARAMS = 4;

    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /**
     * Create an instance of an oracle.
     * 
     * @param option   describes a grading option, including the desired oracle
     *                 and the test point cap.
     * @param testCase the test case for which this oracle will be used
     * @param submission the submission being evaluated
     * @param submitterStage the stage where submitted code was built.
     * @return an oracle instance, or null if not oracle can be created.
     */
    public static Oracle getOracle(
            OracleProperties option,
            TestCase testCase,
            Submission submission,
            Stage submitterStage) {

        String oracleName = option.oracle;
        Class<? extends Oracle> oracleClass = registered.get(oracleName);
        if (oracleClass != null) {
            Class<?>[] argTypes = {option.getClass(), testCase.getClass(),
                    submission.getClass(), submitterStage.getClass() };
            Object[] args = {option, testCase, submission, submitterStage };

            try {
                Constructor<?> constructor = oracleClass.getConstructor(
                        argTypes);
                return (Oracle) constructor.newInstance(args);
            } catch (NoSuchMethodException | SecurityException
                    | InstantiationException | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                logger.error("Could not instantiate oracle class "
                        + oracleClass.getCanonicalName()
                        + " in test case "
                        + testCase.getProperties().name);
            }
        } else {
            try {
                Class<?> oracleClassName = Class.forName(oracleName);
                Class<?>[] argTypes = {option.getClass(), testCase.getClass(),
                         submission.getClass(), submitterStage.getClass()};
                Object[] args = {option, testCase, submission, submitterStage};
                
                Constructor<?> constructor = oracleClassName.getConstructor(
                        argTypes);
                return (Oracle) constructor.newInstance(args);
            } catch (ClassNotFoundException ex) {
                logger.error("Could not identify oracle class named "
                        + oracleName
                        + " in test case "
                        + testCase.getProperties().name, ex);
            } catch (NoSuchMethodException e) {
                logger.error("Could not identify oracle class named "
                        + oracleName
                        + " in test case "
                        + testCase.getProperties().name, e);
            } catch (SecurityException e) {
                logger.error("Could not create oracle class named "
                        + oracleName
                        + " in test case "
                        + testCase.getProperties().name, e);
            } catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                logger.error("Could not instantiate oracle class named "
                        + oracleName
                        + " in test case "
                        + testCase.getProperties().name, e);
            }
        }
        return new SmartOracle(option, testCase, submission, submitterStage);
}

    private OracleFactory() {
    }

    private static HashMap<String, Class<? extends Oracle>> registered
       = new HashMap<>();

    /**
     * Register an oracle for use in scoring test case executions.
     * @param name short name for an oracle
     * @param oracle a subclass of Oracle
     */
    public static void register(String name, Class<? extends Oracle> oracle) {
        registered.put(name, oracle);
    }

    static {
        register("external", ExternalOracle.class);
        register("smart", SmartOracle.class);
        register("junit5", JUnit5Oracle.class);
        register("self", SelfScoredOracle.class);
        register("tap", TAPOracle.class);
        register("status", StatusOracle.class);
    }

}
