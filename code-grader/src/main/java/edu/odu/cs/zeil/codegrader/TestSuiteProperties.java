package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The properties available to describe a test suite.
 */
public class TestSuiteProperties {

    /**
     * error logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    //CHECKSTYLE:OFF


    /**
     * Default properties common to all test cases.
     */
    public TestCasePropertiesBase test;

    /**
     * Properties related to setup and build.
     */
    public BuildProperties build;


    /**
     * Path to grade calculation spreadsheet.
     */
    public String reportTemplate;

    /**
     * Assignment name.
     */
    public String assignment;

    /**
     * Date & time when assignment is due.
     */
    public String dueDate;

    /**
     * How to find when submission was submitted.
     */
    public SubmissionDateOptions dateSubmitted;

    /**
     * Should totals be reported (true by default) in grade reports? Can be
     * turned off if the purpose of the report is feedback rather than
     * evaluation.
     */
    public boolean totals;


    /**
     * How to find when a submission was locked (subsequent submissions will
     * be ignored or graded as zero).
     */
    public SubmissionDateOptions submissionLock;


    /**
     * List of late penalties, as a percentage per day. E.g.,
     * [10, 20, 100] means that a 10% penalty is assessed on the
     * first day late, 20% on the second day, and 100% on all subsequent days.
     * 
     * [0] would ignore lateness, assessing no penalties.
     * 
     * [100] would mean that late submissions are not accepted - they are
     * graded but will get a score of zero. This is the default.
     */
    public int[] latePenalties;


    //CHECKSTYLE:ON


    /**
     * Create a property set with an empty list of grading options.
     */
    public TestSuiteProperties() {
        test = new TestCasePropertiesBase();
        build = new BuildProperties();
        reportTemplate = "";
        assignment = "";
        dueDate = "";
        totals = true;
        latePenalties = new int[1];
        final int lateNotAccepted = 100;
        latePenalties[0] = lateNotAccepted;
        dateSubmitted = new SubmissionDateOptions();
    }

    /**
     * Load properties from a string.
     * 
     * @param input properties in YAML
     * @return the properties
     * @throws TestConfigurationError if input cannot be parsed
     */
    public static TestSuiteProperties loadYAML(String input)
        throws TestConfigurationError {
        if (!input.equals("")) {
            ObjectMapper mapper = getMapper();
            try {
                TestSuiteProperties result = mapper.readValue(input,
                        TestSuiteProperties.class);
                return result;
            } catch (JsonProcessingException e) {
                String message = "Cannot load YAML from string input\n"
                        + e.getMessage();
                LOG.error(message, e);
                throw new TestConfigurationError(message);
            }
        } else {
            return new TestSuiteProperties();
        }
    }

    /**
     * Load properties from a string.
     * 
     * @param input file containing properties in YAML
     * @return the properties
     * @throws TestConfigurationError if input cannot be parsed
     */
    public static TestSuiteProperties loadYAML(File input)
        throws TestConfigurationError {
        ObjectMapper mapper = getMapper();
        try {
            TestSuiteProperties result = mapper.readValue(input, 
                TestSuiteProperties.class);
            return result;
        } catch (IOException e) {
            String message = "Cannot load YAML from " + input.toString() + "\n"
                + e.getMessage();
            LOG.error(message, e);
            throw new TestConfigurationError(message);
        }
    }

    private static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.findAndRegisterModules();
        /*  Not sure if this is a good idea.
        objectMapper.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, 
            false);
        */
        return objectMapper;
    }

    /**
     * Render as a string (YAML).
     */
    @Override
    public String toString() {
        ObjectMapper mapper = getMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "**Could not display value.**";
        }
    }

    /**
     * Set the path pattern that will be used to recognize that an
     * assignment has been locked for a student (e.g., after viewing an
     * on-line solution) so that subsequent submissions will not be accepted.
     * 
     * @param pathPattern  a pattern for a file path containing a date & time
     *          after which submissions cannot be accepted for a specific
     *          individual.
     */
    public void setSubmissionLockIn(String pathPattern) {
        submissionLock = new SubmissionDateOptions();
        submissionLock.in = pathPattern;
    }

}
