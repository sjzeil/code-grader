package edu.odu.cs.zeil.codegrader;

import org.apache.commons.io.FileUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.Grader;
import edu.odu.cs.zeil.codegrader.PathProperty;
import edu.odu.cs.zeil.codegrader.ReflectiveProperties;
import edu.odu.cs.zeil.codegrader.SetupPhase;

public class TestSetupPhase {
	
	public Path asst1DirPath = Paths.get("build", "test-data", "asst1");
	
	@BeforeEach
	public void setup() throws IOException {
		asst1DirPath.toFile().getParentFile().mkdirs();
		Path asst1SrcPath = Paths.get("src", "test", "data", "asst1");
		FileUtils.copyDirectory(asst1SrcPath.toFile(), asst1DirPath.toFile());
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(asst1DirPath.toFile());
	}
	
	
	
	@Test
	void testDefaultSetup() throws IOException {
		String[] params = new String[0];
		Grader autograder = new Grader(params);
		
		SetupPhase setup = autograder.getSetupPhase();
		ReflectiveProperties setupProperties = autograder.getProperties();
		PathProperty pp = (PathProperty)setupProperties.getByReflection("assignmentRoot");
		assertTrue(Files.isSameFile(Paths.get("."), pp.getPath()));
		
		Path p = setup.getSubmissionDir();  
		assertTrue(Files.isSameFile(Paths.get(".", "Submission"), p));
		
		p = setup.getInstructorDir();  
		assertTrue(Files.isSameFile(Paths.get(".", "Instructor"), p));
		
		p = setup.getStageDir();  
		assertTrue(Files.isSameFile(Paths.get(".", "Stage"), p));
	}

	@Test
	void testLocalCopying() throws IOException {
		String[] params = {asst1DirPath.toString()};
		Grader autograder = new Grader(params);
		
		SetupPhase setup = autograder.getSetupPhase();
		setup.runPhase();
		
		File studentFile1 = setup.getStageDir().resolve(Paths.get("student1.h")).toFile();
		assertThat (studentFile1.exists(), equalTo(true));
	}
	
	
	
}
