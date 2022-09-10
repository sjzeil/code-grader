package edu.odu.cs.zeil.codegrader.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

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
        Path testData = Paths.get("build", "test-data");
        if (testData.toFile().exists()) {
            FileUtils.deleteDirectory(testData);
        }

		testDataPath.toFile().mkdirs();
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
							.resolve("TestCases")
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
	void testCLICpp() {

		testDataPath = Paths.get("build", "test-data");
		assignmentPath = Paths.get("src", "test", "data",
			"cpp-assignment");

		Path stage = testDataPath.resolve("stage");
		Path recording = testDataPath.resolve("recording");
		Path recordingGrades = recording.resolve("grades");

		String[] args = {
				"-suite", assignmentPath.resolve("Grading").toString(),
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

			File[] testCases = assignmentPath.resolve("Grading").toFile()
					.listFiles();
			for (File testCase : testCases) {
				if (testCase.isDirectory()) {
					String testCaseName = testCase.getName();
					Path studentRecordedTest = studentRecording
							.resolve("TestCases")
							.resolve(testCaseName);
					assertTrue(studentRecordedTest.toFile().isDirectory());
					assertTrue(studentRecordedTest
							.resolve(testCaseName + ".score")
							.toFile().exists());
				}
			}

		}

		Path summaryFile = recordingGrades.resolve("classSummary.csv");
		assertTrue(summaryFile.toFile().exists());
		String summary = FileUtils.readTextFile(summaryFile.toFile());
		assertTrue(summary.contains("janeSmith,100"));
		assertTrue(summary.contains("jjones,0"));
		assertFalse(summary.contains("johnDoe,100"));
		assertFalse(summary.contains("johnDoe,0"));
	}


	@Test
	void testCLICppSelective() {
		String selectedStudent = "johnDoe";
		String selectedStudentScore = "46";

		testDataPath = Paths.get("build", "test-data");
		assignmentPath = Paths.get("src", "test", "data",
			"cpp-assignment");

		Path stage = testDataPath.resolve("stage");
		Path recording = testDataPath.resolve("recording");
		Path recordingGrades = recording.resolve("grades");

		String[] args = {
				"-suite", assignmentPath.resolve("Grading").toString(),
				"-gold", assignmentPath.resolve("Gold").toString(),
				"-stage", stage.toString(),
				"-submissions", assignmentPath.resolve("submissions")
					.toString(),
				"-recording", recording.toString(),
				"-student", selectedStudent
		};

		CLI cli = new CLI(args);
		cli.go();

		assertTrue(stage.toFile().exists()); // stage should be cleaned up
		assertTrue(recordingGrades.toFile().exists());

		File[] students = assignmentPath.resolve("submissions")
				.toFile().listFiles();
		for (File studentDir : students) {
			String studentID = studentDir.getName();
			if (studentID.equals(selectedStudent)) {
				Path studentRecording = recordingGrades.resolve(studentID);
				assertTrue(studentRecording.toFile().isDirectory());

				File[] testCases = assignmentPath.resolve("Grading").toFile()
						.listFiles();
				for (File testCase : testCases) {
					if (testCase.isDirectory()) {
						String testCaseName = testCase.getName();
						Path studentRecordedTest = studentRecording
								.resolve("TestCases")
								.resolve(testCaseName);
						assertTrue(studentRecordedTest.toFile().isDirectory());
						assertTrue(studentRecordedTest
								.resolve(testCaseName + ".score")
								.toFile().exists());						
					}
				}
				String total = FileUtils.readTextFile(
							studentRecording
							.resolve(selectedStudent + ".total").toFile());
				assertEquals(selectedStudentScore, total.trim());
			}
		}
	}

	@Test
	void testCLICpp2() {

		testDataPath = Paths.get("build", "test-data");
		assignmentPath = Paths.get("src", "test", "data",
			"cpp-assignment2");

		Path stage = testDataPath.resolve("stage");
		Path recording = testDataPath.resolve("recording");
		Path recordingGrades = recording.resolve("grades");

		String[] args = {
				"-suite", assignmentPath.resolve("Grading").toString(),
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

			File[] testCases = assignmentPath.resolve("Grading").toFile()
					.listFiles();
			for (File testCase : testCases) {
				if (testCase.isDirectory()) {
					String testCaseName = testCase.getName();
					Path studentRecordedTest = studentRecording
							.resolve("TestCases")
							.resolve(testCaseName);
					assertTrue(studentRecordedTest.toFile().isDirectory());
					assertTrue(studentRecordedTest
							.resolve(testCaseName + ".score")
							.toFile().exists());
				}
			}

		}

		Path summaryFile = recordingGrades.resolve("classSummary.csv");
		assertTrue(summaryFile.toFile().exists());
		String summary = FileUtils.readTextFile(summaryFile.toFile());
		assertTrue(summary.contains("janeSmith,100"));
		assertTrue(summary.contains("jjones,0"));
		assertTrue(summary.contains("johnDoe,100"));
		assertFalse(summary.contains("johnDoe,0"));
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
								.resolve("TestCases")
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
							.resolve("TestCases")
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
