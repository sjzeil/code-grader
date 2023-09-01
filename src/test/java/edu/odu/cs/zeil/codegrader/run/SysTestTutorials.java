package edu.odu.cs.zeil.codegrader.run;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import edu.odu.cs.zeil.codegrader.FileUtils;

public class SysTestTutorials {

	private Path testDataPath = Paths.get("build", "test-data");
	private Path tutorialsPath = Paths.get("src", "systest", "data",
			"tutorials");

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
	void testTutorialSimpleCppStdIn() throws IOException, CsvException {

		Path stage = testDataPath.resolve("stage");
		Path recording = testDataPath.resolve("recording");
		Path recordingGrades = recording.resolve("grades");
		Path tutorial = tutorialsPath.resolve("simpleCppStdIn");

		String[] args = {
				"-suite", tutorial.resolve("Tests").toString(),
				"-gold", tutorial.resolve("Gold").toString(),
				"-isrc", "-",
				"-stage", stage.toString(),
				"-submissions", tutorial.resolve("submissions")
					.toString(),
				"-recording", recording.toString() //,
		};

		CLI cli = new CLI(args);
		cli.go();
        
		assertFalse(stage.toFile().exists()); // stage should be cleaned up
		assertTrue(recordingGrades.toFile().exists());

		Path scoresCSV = recordingGrades.resolve("classSummary.csv");

		assertTrue(scoresCSV.toFile().exists());

		List<String[]> csvLines = new ArrayList<>();
		try (Reader reader = Files.newBufferedReader(scoresCSV)) {
			try (CSVReader csvReader = new CSVReader(reader)) {
				csvLines = csvReader.readAll();
			}
		}
		HashMap<String, String> scores = new HashMap<>();
		for (String[] line: csvLines) {
			scores.put(line[0], line[1]);
		}
		assertThat(scores.get("broken"), is("0"));
		assertThat(scores.get("buggy"), is("25"));
		assertThat(scores.get("imperfect"), is("25"));
		assertThat(scores.get("looped"), is("25"));
		assertThat(scores.get("perfect"), is("100"));
	}


}
