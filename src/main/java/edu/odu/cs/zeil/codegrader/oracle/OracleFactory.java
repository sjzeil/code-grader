package edu.odu.cs.zeil.codegrader.oracle;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.odu.cs.zeil.codegrader.TestCase;

public class OracleFactory {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Oracle getOracle(String oracleSpecification, TestCase testCase) {
        int colonPos = oracleSpecification.indexOf(":");
        if (colonPos >= 0) {
            int asstPos = oracleSpecification.indexOf("=");
            if (asstPos < 0 || asstPos > colonPos) {
                String oracleName = oracleSpecification.substring(0, colonPos);
                String settings = oracleSpecification.substring(colonPos + 1);
                if (oracleName.toLowerCase().equals("smart")) {
                    return new SmartOracle(settings, testCase);
                } else if (oracleName.toLowerCase().equals("external")) {
                    return new ExternalOracle(settings, testCase);
                } else {
                    try {
                        Class<?> oracleClass = Class.forName(oracleName);
                        Class<?>[] argTypes = new Class<?>[2];
                        Object[] args = new Object[2];
                        argTypes[0] = "".getClass();
                        args[0] = settings;
                        argTypes[1] = testCase.getClass();
                        args[1] = testCase;
                        Constructor<?> constructor = oracleClass.getConstructor(argTypes);
                        return (Oracle) constructor.newInstance(args);
                    } catch (ClassNotFoundException ex) {
                        logger.error("Could not identify oracle class named " + oracleName
                                + " in test case " + testCase.getProperties().getName(), ex);
                    } catch (NoSuchMethodException e) {
                        logger.error("Could not identify oracle class named " + oracleName
                                + " in test case " + testCase.getProperties().getName(), e);
                    } catch (SecurityException e) {
                        logger.error("Could not create oracle class named " + oracleName
                                + " in test case " + testCase.getProperties().getName(), e);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        logger.error("Could not instantiate oracle class named " + oracleName
                                + " in test case " + testCase.getProperties().getName(), e);
                    }
                    return new SmartOracle(settings, testCase);
                }
            } else {
                return new SmartOracle(oracleSpecification, testCase);
            }
        } else {
            return new SmartOracle(oracleSpecification, testCase);
        }
    }
}
