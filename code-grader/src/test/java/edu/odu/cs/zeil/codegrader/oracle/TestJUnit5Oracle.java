package edu.odu.cs.zeil.codegrader.oracle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.Assignment;
import edu.odu.cs.zeil.codegrader.FileUtils;
import edu.odu.cs.zeil.codegrader.OracleProperties;
import edu.odu.cs.zeil.codegrader.Stage;
import edu.odu.cs.zeil.codegrader.Submission;
import edu.odu.cs.zeil.codegrader.TestCase;
import edu.odu.cs.zeil.codegrader.TestCaseProperties;
import edu.odu.cs.zeil.codegrader.TestConfigurationError;
import edu.odu.cs.zeil.codegrader.TestSuiteProperties;

//CHECKSTYLE:OFF

public class TestJUnit5Oracle {

	private  Path asstSrcPath = Paths.get("src", "test", "data", "assignment2");
	private Path testSuitePath = Paths.get("build", "test-data", "assignment2");
	private Submission sub;
	
	private Assignment asst;
	private TestCase testCase;

	private Stage stage;
	
	@BeforeEach
	private void setup() throws IOException, TestConfigurationError {
        Path testData = Paths.get("build", "test-data");
        if (testData.toFile().exists()) {
            FileUtils.deleteDirectory(testData);
        }

		testSuitePath.toFile().getParentFile().mkdirs();
		FileUtils.copyDirectory(asstSrcPath, testSuitePath, null, null,
			StandardCopyOption.REPLACE_EXISTING);

		asst = new Assignment();
		asst.setTestSuiteDirectory(testSuitePath.resolve("tests"));
		asst.setStagingDirectory(testSuitePath.resolve("stage"));

		testCase = new TestCase(new TestCaseProperties(asst, "params"));
		sub = new Submission(asst, "student1", 
            testSuitePath.resolve("submissions"));
		stage = new Stage(asst, sub, new TestSuiteProperties());
	}
	
    @Test
    void testJU5Oracle() throws FileNotFoundException {
        Oracle oracle = new JUnit5Oracle(new OracleProperties(), testCase,
                sub, stage);

        String observed = "Test run finished after 64 ms\n" +
                "[         3 containers found      ]\n" +
                "[         0 containers skipped    ]\n" +
                "[         3 containers started    ]\n" +
                "[         0 containers aborted    ]\n" +
                "[         3 containers successful ]\n" +
                "[         0 containers failed     ]\n" +
                "[        10 tests found           ]\n" +
                "[         0 tests skipped         ]\n" +
                "[        10 tests started         ]\n" +
                "[         0 tests aborted         ]\n" +
                "[        6 tests successful      ]\n" +
                "[         4 tests failed          ]\n";

        OracleResult result = oracle.compare("", observed);
        assertThat(result.score, equalTo(60));
        assertThat(result.message, equalTo(observed));
    }

    @Test
    void testJU5OracleDirtier() throws FileNotFoundException {
        Oracle oracle = new JUnit5Oracle(new OracleProperties(), testCase,
                sub, stage);

        String observed = "make[1]: Entering directory '/home/zeil/courses/cs350/AutoGrading/s23-zeil/WorkArea/'                                                                                                                      javac -cp 'src/main/java:lib/*' src/main/java/edu/odu/cs/cs350/tdd/Book.java                                            javac -cp 'src/main/java:lib/*' src/main/java/edu/odu/cs/cs350/tdd/SJZTestBook.java                                     java -jar lib/junit-platform-console-standalone-1.7.2.jar  -cp 'src/main/java:lib/hamcrest-all-1.3.jar:lib/junit-jupiter-api-5.7.2.jar:lib/junit-jupiter-engine-5.7.2.jar:lib/junit-jupiter-params-5.6.2.jar:lib/junit-platform-console-standalone-1.7.2.jar:lib/unitTestTracker.jar' -c edu.odu.cs.cs350.tdd.SJZTestBook\n"
+ "\n"
+ "        Thanks for using JUnit! Support its development at https://junit.org/sponsoring\n"
+ "        \n"
+ "        .\n"
+ "        +-- JUnit Jupiter [OK]\n"
+ "        | '-- SJZTestBook [OK]\n"
+ "        |   +-- testAddChapter() [OK]\n"
+ "        |   '-- testConstructor() [OK]\n"
+ "        '-- JUnit Vintage [OK]\n"
+ "        \n"
+ "        Test run finished after 135 ms\n"
+ "        [         3 containers found      ]\n"
+ "        [         0 containers skipped    ]\n"
+ "[         3 containers started    ]\n"
+ "[         0 containers aborted    ]\n"
+ "[         3 containers successful ]\n"
+ "[         0 containers failed     ]\n"
+ "[         2 tests found           ]\n"
+ "[         0 tests skipped         ]\n"
+ "[         2 tests started         ]\n"
+ "[         0 tests aborted         ]\n"
+ "[         2 tests successful      ]\n"
+ "[         0 tests failed          ]\n"
+ "\n"
+ "make[1]: Leaving directory '/home/zeil/courses/cs350/AutoGrading/s23-zeil/WorkArea/\n"
        ;

        OracleResult result = oracle.compare("", observed);
        assertThat(result.score, equalTo(100));
        assertThat(result.message, equalTo(observed));
    }



    @Test
    void testJU5Gradle() throws IOException {
        Oracle oracle = new JUnit5Oracle(new OracleProperties(), testCase,
                sub, stage);

        Path reportDir = stage.getStageDir().resolve("build/reports/tests/test/");
        reportDir.toFile().mkdirs();
        FileUtils.copyDirectory(Paths.get("src/test/data/ju5"), reportDir, null, null, StandardCopyOption.REPLACE_EXISTING);        

        String observed = "\n";

        OracleResult result = oracle.compare("", observed);
        assertThat(result.score, equalTo(80));
        
    }

    @Test
    void testJU5Maven() throws IOException {
        Oracle oracle = new JUnit5Oracle(new OracleProperties(), testCase,
                sub, stage);

        Path reportDir = stage.getStageDir().resolve("target/surefire-reports");
        reportDir.toFile().mkdirs();
        FileUtils.copyDirectory(Paths.get("src/test/data/ju5"), reportDir, null, null, StandardCopyOption.REPLACE_EXISTING);

        String observed = "\n";

        OracleResult result = oracle.compare("", observed);
        assertThat(result.score, equalTo(50));
        
    }


}

