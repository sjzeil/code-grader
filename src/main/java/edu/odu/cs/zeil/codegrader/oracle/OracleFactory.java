package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
        if (oracleName.toLowerCase().equals("smart")) {
            return new SmartOracle(option, testCase,
                    submission, submitterStage);
        } else if (oracleName.toLowerCase().equals("external")) {
            return new ExternalOracle(option, testCase,
                    submission, submitterStage);
        } else if (oracleName.toLowerCase().equals("junit5")) {
            return new JUnit5Oracle(option, testCase,
                    submission, submitterStage);
        } else if (oracleName.toLowerCase().equals("tap")) {
            return new TAPOracle(option, testCase,
                        submission, submitterStage);
        } else if (oracleName.toLowerCase().equals("self")) {
            return new SelfScoredOracle(option, testCase,
                    submission, submitterStage);
        } else {
            try {
                Class<?> oracleClass = Class.forName(oracleName);
                Class<?>[] argTypes = {option.getClass(), testCase.getClass(),
                         submission.getClass(), submitterStage.getClass()};
                Object[] args = {option, testCase, submission, submitterStage};
                
                Constructor<?> constructor = oracleClass.getConstructor(
                        argTypes);
                return (Oracle) constructor.newInstance(args);
            } catch (ClassNotFoundException ex) {
                logger.error("Could not identify oracle class named "
                        + oracleName
                        + " in test case "
                        + testCase.getProperties().getName(), ex);
            } catch (NoSuchMethodException e) {
                logger.error("Could not identify oracle class named "
                        + oracleName
                        + " in test case "
                        + testCase.getProperties().getName(), e);
            } catch (SecurityException e) {
                logger.error("Could not create oracle class named "
                        + oracleName
                        + " in test case "
                        + testCase.getProperties().getName(), e);
            } catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                logger.error("Could not instantiate oracle class named "
                        + oracleName
                        + " in test case "
                        + testCase.getProperties().getName(), e);
            }
            return new SmartOracle(option, testCase,
                submission, submitterStage);
        }
    }

    private OracleFactory() {
    }
}
