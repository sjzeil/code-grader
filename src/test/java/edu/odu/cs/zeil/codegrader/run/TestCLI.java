package edu.odu.cs.zeil.codegrader.run;

//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.FileUtils;

public class TestCLI {

	private Path testDataPath = Paths.get("build", "test-data");
	private Path assignmentPath = Paths.get("src", "test", "data",
			"java-sqrt-assignment");

	/**
	 * Set up assignment2 params test.
	 * 
	 * @throws IOException
	 * @throws TestConfigurationError
	 */
	@BeforeEach
	public void setup() throws IOException {
		testDataPath.toFile().mkdirs();
		String template = "gradeTemplate.xlsx";
		Path gradingTemplate = Paths.get("src", "main", "resources",
			"edu", "odu", "cs", "zeil", "codegrader", template);
		Path binDir = Paths.get("bin", "main",
			"edu", "odu", "cs", "zeil", "codegrader");
		Path buildDir = Paths.get("build", "classes", "java", "main",
			"edu", "odu", "cs", "zeil", "codegrader");
		if (binDir.toFile().exists()) {
			Files.copy(gradingTemplate, binDir.resolve(template), 
				StandardCopyOption.REPLACE_EXISTING);
		}
		if (buildDir.toFile().exists()) {
			Files.copy(gradingTemplate, buildDir.resolve(template), 
				StandardCopyOption.REPLACE_EXISTING);
		}

	}

	/**
	 * Clean up test data.
	 * 
	 * @throws IOException
	 */
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(Paths.get("build", "test-data"));
	}

	@Test
	void testCLIBasic() {

		Path stage = testDataPath.resolve("stage");
		Path recording = testDataPath.resolve("recording");
		Path recordingGrades = recording.resolve("grades");

		String[] args = {
				"-suite", assignmentPath.resolve("Tests").toString(),
				"-gold", assignmentPath.resolve("Gold").toString(),
				"-stage", stage.toString(),
				"-submissions", assignmentPath.resolve("submissions")
					.toString(),
				"-recording", recording.toString()
		};

		CLI cli = new CLI(args);
		cli.go();

		assertFalse(stage.toFile().exists()); // stage should be cleaned up
		assertTrue(recordingGrades.toFile().exists());

		File[] students = assignmentPath.resolve("submissions")
				.toFile().listFiles();
		for (File studentDir : students) {
			String studentID = studentDir.getName();
			Path studentRecording = recordingGrades.resolve(studentID);
			assertTrue(studentRecording.toFile().isDirectory());

			File[] testCases = assignmentPath.resolve("Tests").toFile()
					.listFiles();
			for (File testCase : testCases) {
				if (testCase.isDirectory()) {
					String testCaseName = testCase.getName();
					Path studentRecordedTest = studentRecording
							.resolve("Grading")
							.resolve(testCaseName);
					assertTrue(studentRecordedTest.toFile().isDirectory());
					assertTrue(studentRecordedTest
							.resolve(testCaseName + ".score")
							.toFile().exists());
				}
			}

		}

	}

	@Test
	void testCLISelectedStudent() {

		Path recording = testDataPath.resolve("recording");
		Path recordingGrades = recording.resolve("grades");
		Path stage = recording.resolve("stage");

		String[] args = {
				"-suite", assignmentPath.resolve("Tests").toString(),
				"-gold", assignmentPath.resolve("Gold").toString(),
				"-stage", stage.toString(),
				"-submissions", assignmentPath.resolve("submissions")
					.toString(),
				"-recording", recording.toString(),
				"-student", "perfect"
		};

		CLI cli = new CLI(args);
		cli.go();

		assertTrue(stage.toFile().isDirectory()); // stage is retained
		assertTrue(stage.resolve("gold").toFile().isDirectory());
		assertTrue(stage.resolve("gold").resolve("makefile").toFile().exists());
		assertTrue(stage.resolve("submission").toFile().isDirectory());
		assertTrue(stage.resolve("submission").resolve("makefile")
			.toFile().exists());
		assertTrue(recordingGrades.toFile().isDirectory());

		File[] students = assignmentPath.resolve("submissions")
				.toFile().listFiles();
		for (File studentDir : students) {
			String studentID = studentDir.getName();
			Path studentRecording = recordingGrades.resolve(studentID);
			if (studentID.equals("perfect")) {
				assertTrue(studentRecording.toFile().isDirectory());

				File[] testCases = assignmentPath.resolve("Tests").toFile()
						.listFiles();
				for (File testCase : testCases) {
					if (testCase.isDirectory()) {
						String testCaseName = testCase.getName();
						Path studentRecordedTest = studentRecording
								.resolve("Grading")
								.resolve(testCaseName);
						assertTrue(studentRecordedTest.toFile().isDirectory());
						assertTrue(studentRecordedTest
								.resolve(testCaseName + ".score")
								.toFile().exists());
					}
				}
			} else {
				assertFalse(studentRecording.toFile().isDirectory());
			}
		}

	}

	@Test
	void testCLIDefaultStage() {

		Path recording = testDataPath.resolve("recording");
		Path recordingGrades = recording.resolve("grades");
		Path stage = recording.resolve("stage");

		String[] args = {
				"-suite", assignmentPath.resolve("Tests").toString(),
				"-gold", assignmentPath.resolve("Gold").toString(),
				"-submissions", assignmentPath.resolve("submissions")
					.toString(),
				"-recording", recording.toString()
		};

		CLI cli = new CLI(args);
		cli.go();

		assertFalse(stage.toFile().exists()); // stage should be cleaned up
		assertTrue(recording.toFile().exists());

		File[] students = assignmentPath.resolve("submissions")
				.toFile().listFiles();
		for (File studentDir : students) {
			String studentID = studentDir.getName();
			Path studentRecording = recordingGrades.resolve(studentID);
			assertTrue(studentRecording.toFile().isDirectory());

			File[] testCases = assignmentPath.resolve("Tests").toFile()
					.listFiles();
			for (File testCase : testCases) {
				if (testCase.isDirectory()) {
					String testCaseName = testCase.getName();
					Path studentRecordedTest = studentRecording
							.resolve("Grading")
							.resolve(testCaseName);
					assertTrue(studentRecordedTest.toFile().isDirectory());
					assertTrue(studentRecordedTest
							.resolve(testCaseName + ".score")
							.toFile().exists());
				}
			}

		}

	}
}
