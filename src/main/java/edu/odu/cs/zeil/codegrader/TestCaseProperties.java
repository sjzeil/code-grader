package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class TestCaseProperties implements Comparable<TestCaseProperties> {

    @JsonIgnore
    private Path testDirectory;
    @JsonIgnore
    private Assignment assignment;



//CHECKSTYLE:OFF

    /**
     * Identifies this test case.
     */
    public String name;

    /**
     * A tag name used to determine when (if ever) a test case should
     * be performed.  Default is "test".
     * 
     * A test suite begins by activating all cases of kind "build".
     */
    private Deferred<String> kind;


    /**
     * The command line parameters to be supplies when executing the
     * program under evaluation.
    */
    private Deferred<String> params;

    /**
     * How many points this test case is worth? 
     */
    private Deferred<Integer> weight;

    /**
     * The command string used to launch the program under evaluation.
     */
    private Deferred<String> launch;

    /**
     * A string containing input to be supplied to std in when the program
     * under evaluation is run;
     */
    private Deferred<String> in;

    /**
     * A string containing expected output when the program under
     * evaluation is run.
     */
    private Deferred<String> expected;

    /**
     * How many seconds to allow a test execution to run before concluding
     * that the program is hanging, caught in an infinite loop, or simply
     * unacceptably slow.
     */
    private Deferred<Integer> timelimit;

    /**
     * Should the standard error stream be captures and included in the
     * program output being evaluated?
     */
    private Deferred<Boolean> stderr;

    /**
     * Should the status code be checked for evidence that a program
     * has crashed? Defaults to false because students aren't likely to
     * be all that careful about returning status codes.
     */
    private Deferred<Boolean> status;


    /**
     * The grading criteria for this test case.
     */
    public List<OracleProperties> grading;

    /**
     * Description of this test case (for display in student's grade report)
     */
    private Deferred<String> description;


    /**
     * What tags should be activated if this case fails (score < 100)
     */
    public List<String> onFail;

    /**
     * What tags should be activated if this case succeeds (score == 100)
     */
    public List<String> onSuccess;

    /**
     * If this tag is active, then this case should fail, unrun, with
     *  score of zero.
     */
    private Deferred<String> failIf;

//CHECKSTYLE:ON



    /**
     * Create a test case directory based upon information in testDirectory and
     * in the assignment directory above it.
     * 
     * @param asst     an assignment
     * @param testName name of this test case. Must match a subdirectory
     *                 of the test suite.
     * @throws FileNotFoundException if the assignment's test suite directory
     *                               does not exist or
     *                               does not contain a subdirectory matching
     *                               testName
     */
    public TestCaseProperties(Assignment asst, String testName)
            throws TestConfigurationError {
        this.name = testName;
        this.assignment = asst;
        this.testDirectory = asst.getTestSuiteDirectory().resolve(testName);
        TestSuiteProperties suiteProperties;
        try {
            File defaultsFile = FileUtils.findFile(
                    asst.getTestSuiteDirectory(),
                    ".yaml").get();
            suiteProperties = TestSuiteProperties.loadYAML(defaultsFile);
        } catch (NoSuchElementException ex) {
            suiteProperties = new TestSuiteProperties();
        }
        TestCasePropertiesBase suiteDefaults = suiteProperties.test;
        TestCasePropertiesBase caseProperties = new TestCasePropertiesBase();
        try {
            File yamlFile = FileUtils.findFile(testDirectory,
                    ".yaml").get();
            caseProperties = TestCasePropertiesBase.loadYAML(yamlFile);
        } catch (NoSuchElementException ex) {
            caseProperties = new TestCasePropertiesBase();
        }
        TestCasePropertiesBase defaults = TestCasePropertiesBase.defaults();
        resolveDefaults(caseProperties, suiteDefaults, defaults);
    }


    private void resolveDefaults(
            TestCasePropertiesBase tCase,
            TestCasePropertiesBase suite,
            TestCasePropertiesBase defaults) {
        kind = selectValue(tCase.kind,
                suite.kind, defaults.kind, "kind", 
                new StringParser(true));
        params = selectValue(tCase.params,
                suite.params, defaults.params, "params",
                new StringParser(true));
        weight = selectValue(tCase.weight,
                suite.weight, defaults.weight, "weight", 
                new IntegerParser());
        launch = selectValue(tCase.launch,
                suite.launch, defaults.launch, "launch",
                new StringParser(true));
        expected = selectValue(tCase.expected,
            suite.expected, defaults.expected, "expected",
            new StringParser());
        in = selectValue(tCase.expected,
            suite.expected, defaults.expected, "in",
            new StringParser());
        timelimit = selectValue(tCase.timelimit,
            suite.timelimit, defaults.timelimit, "timelimit",
            new IntegerParser());
        stderr = selectValue(tCase.stderr,
            suite.stderr, defaults.stderr, "stderr", new BooleanParser());
        status = selectValue(tCase.status,
            suite.status, defaults.status, "status", new BooleanParser());
        description = selectValue(tCase.description,
            suite.description, defaults.description, "description",
            new StringParser(true));
        grading = selectValue(tCase.grading,
            suite.grading, defaults.grading, new ArrayList<>());
        onFail = selectValue(tCase.onFail,
            suite.onFail, defaults.onFail, new ArrayList<>());
        onSuccess = selectValue(tCase.onSuccess,
            suite.onSuccess, defaults.onSuccess, new ArrayList<>());
        failIf = selectValue(tCase.failIf,
            suite.failIf, defaults.failIf, "failIf", 
            new StringParser(true));
    }

    /**
     * Create an empty properties set.
     */
    public TestCaseProperties() {
        testDirectory = null;
        name = null;
        assignment = null;
        kind = new Deferred<String>("kind", "test", 
            new StringParser(true));
        params = new Deferred<String>("params", "test", 
                new StringParser(true));
        weight = new Deferred<Integer>("weight", 1, 
            new IntegerParser());
        launch = new Deferred<String>("launch", "test", 
            new StringParser(true));
        expected = new Deferred<String>("expected", "test", 
            new StringParser(false));
        setTimelimit(1);
        stderr = new Deferred<Boolean>("stderr", false, 
            new BooleanParser());
        status = new Deferred<Boolean>("status", false, 
            new BooleanParser());
        grading = null;
        description = new Deferred<String>("description", "test", 
            new StringParser(true));
        in = new Deferred<String>("in", "test", 
            new StringParser(false));
    }



    private static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, 
            false);
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
            return "**Could not display value.**\n" + e.getMessage();
        }
    }

    
    private <T> Deferred<T> selectValue(
        Optional<T> caseValue,
        Optional<T> suiteValue,
        Optional<T> defaultValue,
        String explicitExtension,
        Parser<T> parser) {
    T value = null;
    if (caseValue.isPresent()) {
        value = caseValue.get();
    } else if (suiteValue.isPresent()) {
        value = suiteValue.get();
    } else if (defaultValue.isPresent()) {
        value = defaultValue.get();
    }
    return new Deferred<T>(explicitExtension, value, parser);
}

private Deferred<Integer> selectValue(
    OptionalInt caseValue,
    OptionalInt suiteValue,
    OptionalInt defaultValue,
    String explicitExtension,
    Parser<Integer> parser) {
Integer value = null;
if (caseValue.isPresent()) {
    value = caseValue.getAsInt();
} else if (suiteValue.isPresent()) {
    value = suiteValue.getAsInt();
} else if (defaultValue.isPresent()) {
    value = defaultValue.getAsInt();
}
return new Deferred<Integer>(explicitExtension, value, parser);
}

private <T> T selectValue(
    Optional<T> caseValue,
    Optional<T> suiteValue,
    Optional<T> defaultValue,
    T fallback) {
T value = fallback;
if (caseValue.isPresent()) {
    value = caseValue.get();
} else if (suiteValue.isPresent()) {
    value = suiteValue.get();
} else if (defaultValue.isPresent()) {
    value = defaultValue.get();
}
return value;
}



    /**
     * The expected output from the program.
     * 
     * @return the expected output
     */
    public String getExpected() {
        return expected.get(testDirectory);
    }


    /**
     * The standard input for the program.
     * 
     * @return the input path
     */
    public String getIn() {
        return in.get(testDirectory);
    }


    /**
     * @return command line parameters for running the test case.
     */
    public String getParams() {
        return params.get(testDirectory);
    }




    /**
     * 
     * @return the location of the test case information
     */
    public Path getTestCaseDirectory() {
        return testDirectory;
    }


    /**
     * @return Directory in which a submission will be compiled and tested.
     */

    public Path getStagingDirectory() {
        return assignment.getStagingDirectory();
    }

    /**
     * @return Where to place information about the points earned for
     *         passing tests.
     */
    public Path getRecordingDirectory() {
        return assignment.getRecordingDirectory();
    }

    /**
     * @return Collection of all test cases.
     */

    public Path getTestSuiteDirectory() {
        return assignment.getTestSuiteDirectory();
    }


    /**
     * Permits iteration over the grading options specified
     * for this test case.
     * @return iterable over OracleProperties
     */
    public Iterable<OracleProperties> getGradingOptions() {
        return grading;
    }

    /**
     * 
     * @return the assignment settings
     */
    public Assignment getAssignment() {
        return assignment;
    }

    /**
     * Compare two TC properties by name.
     * @param right another TC properties
     * @return ordering of the names
     */
    public int compareTo(TestCaseProperties right) {
        return name.compareTo(right.name);
    }

    /**
     * Compare by name.
     * @param right another test cast property collection
     * @return true if the names match
     */
    public boolean equals(Object right) {
        if (right instanceof TestCaseProperties) {
            return name.equals(((TestCaseProperties) right).name);
        } else {
            return false;
        }
    }

    /**
     * Hash code of name.
     * @return hash code
     */
    public int hashCode() {
        return name.hashCode();
    }


	/**
	 * @return the kind
	 */
	public String getKind() {
		return kind.get(testDirectory);
	}


	/**
	 * @param theKind the kind to set
	 */
	public void setKind(String theKind) {
		kind.set(theKind);
	}


	/**
	 * @param theParams the params to set
	 */
	public void setParams(String theParams) {
		this.params.set(theParams);
	}


	/**
	 * @return the weight
	 */
	public int getWeight() {
		return weight.get(testDirectory);
	}


	/**
	 * @param theWeight the weight to set
	 */
	public void setWeight(int theWeight) {
		this.weight.set(theWeight);
	}


	/**
	 * @return the launch
	 */
	public String getLaunch() {
		return launch.get(testDirectory);
	}


	/**
	 * @param theLaunch the launch to set
	 */
	public void setLaunch(String theLaunch) {
		this.launch.set(theLaunch);
	}


	/**
	 * @param theExpected the expected to set
	 */
	public void setExpected(String theExpected) {
		this.expected.set(theExpected);
	}

    /**
	 * @param theInput the input to set
	 */
	public void setIn(String theInput) {
		this.in.set(theInput);
	}


	/**
	 * @return the timelimit
	 */
	public int getTimelimit() {
		return timelimit.get(testDirectory);
	}


	/**
	 * @param theTimelimit the timelimit to set
	 */
	public void setTimelimit(int theTimelimit) {
		this.timelimit.set(theTimelimit);
	}


	/**
	 * @return the stderr
	 */
	public boolean isStderr() {
		return stderr.get(testDirectory);
	}


	/**
	 * @param theStderr the stderr to set
	 */
	public void setStderr(boolean theStderr) {
		this.stderr.set(theStderr);
	}


	/**
	 * @return the status
	 */
	public boolean isStatus() {
		return status.get(testDirectory);
	}


	/**
	 * @param theStatus the status to set
	 */
	public void setStatus(boolean theStatus) {
		this.status.set(theStatus);
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return description.get(testDirectory);
	}


	/**
	 * @param theDescription the description to set
	 */
	public void setDescription(String theDescription) {
		this.description.set(theDescription);
	}


	/**
	 * @return the failIf
	 */
	public String getFailIf() {
		return failIf.get(testDirectory);
	}


	/**
	 * @param theFailIf the failIf to set
	 */
	public void setFailIf(String theFailIf) {
		this.failIf.set(theFailIf);
	}

}
