package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.ExternalProcess;

public final class OracleFactory {

    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /**
     * Create an instance of an oracle.
     * 
     * @param option   describes a grading option, including the desired oracle
     *                 and the test point cap.
     * @param testCase the test case for which this oracle will be used
     * @return an oracle instance, or null if not oracle can be created.
     */
    public static Oracle getOracle(
            OracleProperties option,
            ExternalProcess testCase) {

        String oracleName = option.oracle;
        if (oracleName.toLowerCase().equals("smart")) {
            return new SmartOracle(option, testCase);
        } else if (oracleName.toLowerCase().equals("external")) {
            return new ExternalOracle(option, testCase);
        } else {
            try {
                Class<?> oracleClass = Class.forName(oracleName);
                Class<?>[] argTypes = new Class<?>[2];
                Object[] args = new Object[2];
                argTypes[0] = option.getClass();
                args[0] = option;
                argTypes[1] = testCase.getClass();
                args[1] = testCase;
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
            return new SmartOracle(option, testCase);
        }
    }

    private OracleFactory() {
    }
}
