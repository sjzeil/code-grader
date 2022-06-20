package edu.odu.cs.zeil.codegrader;

import org.apache.commons.io.FileUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.BuildPhase;
import edu.odu.cs.zeil.codegrader.Grader;
import edu.odu.cs.zeil.codegrader.ReflectiveProperties;
import edu.odu.cs.zeil.codegrader.SetupPhase;

public class TestBuildPhase {
	
	public Path asst1DirPath = Paths.get("build", "test-data", "asst1");
	
	public Grader autograder;
	public SetupPhase setup;
	public BuildPhase build;
	public Path stagePath;
	private ReflectiveProperties properties;
	
	@BeforeEach
	public void setup() throws IOException {
		asst1DirPath.toFile().getParentFile().mkdirs();
		Path asst1SrcPath = Paths.get("src", "test", "data", "asst1");
		FileUtils.copyDirectory(asst1SrcPath.toFile(), asst1DirPath.toFile());
		
		String[] params = {asst1DirPath.toString()};
		autograder = new Grader(params);
		
		setup = autograder.getSetupPhase();
		
		build = autograder.getBuildPhase();
		properties = autograder.getProperties();
		
		stagePath = setup.getStageDir();
	}
	
	@AfterEach
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(asst1DirPath.toFile());
	}
	
	
	
	@Test
	void testCommandSelectionMake() throws IOException {
		File makefile = createFile(stagePath.resolve(Paths.get("makefile")));
		assertThat (build.getBuildCommand(), is("make -k"));
		makefile.delete();
		
		makefile = createFile(stagePath.resolve(Paths.get("makefile")));
		assertThat (build.getBuildCommand(), is("make -k"));
		makefile.delete();
		
	}

	@Test
	void testCommandSelectionAnt() throws IOException {
		File buildfile = createFile(stagePath.resolve(Paths.get("build.xml")));
		assertThat (build.getBuildCommand(), is("ant"));
		buildfile.delete();
	}
	
	@Test
	void testCommandSelectionMaven() throws IOException {
		File buildfile = createFile(stagePath.resolve(Paths.get("pom.xml")));
		assertThat (build.getBuildCommand(), is("mvn compile"));
		buildfile.delete();
	}
	
	@Test
	void testCommandSelectionGradle() throws IOException {
		File buildfile = createFile(stagePath.resolve(Paths.get("gradlew")));
		String os = System.getProperty("os.name");
		if (!os.contains("Windows"))
			assertThat (build.getBuildCommand(), is("./gradlew"));
		buildfile.delete();

		buildfile = createFile(stagePath.resolve(Paths.get("gradlew.bat")));
		if (os.contains("Windows"))
			assertThat (build.getBuildCommand(), is("gradlew.bat"));
		buildfile.delete();

		buildfile = createFile(stagePath.resolve(Paths.get("build.gradle")));
		assertThat (build.getBuildCommand(), is("gradle"));
		buildfile.delete();
	}
	
	@Test
	void testCommandSelectionCpp() throws IOException {
		File buildfile = createFile(stagePath.resolve(Paths.get("foo.cpp")));
		assertThat (build.getBuildCommand(), is(build.getCppCommand() + " " + build.getCppOptions()));
		buildfile.delete();
	}

	@Test
	void testCommandSelectionJava() throws IOException {
		File buildfile = createFile(stagePath.resolve(Paths.get("foo.java")));
		assertThat (build.getBuildCommand(), is(build.getJavaCommand() + " " + build.getJavaOptions()));
		buildfile.delete();
		
		File jfile = createFile(stagePath.resolve(Paths.get("package/foo.java")));
		assertThat (build.getBuildCommand(), is(build.getJavaCommand() + " " + build.getJavaOptions()));
		jfile.delete();
	}
	
	@Test
	void testCommandSelectionExplicit() throws IOException {
		File buildfile = createFile(stagePath.resolve(Paths.get("makefile")));
		properties.setByReflection("build.buildCommand", "foo");
		assertThat (build.getBuildCommand(), is("foo"));
		buildfile.delete();
		
		File jfile = createFile(stagePath.resolve(Paths.get("package/foo.java")));
		assertThat (build.getBuildCommand(), is(build.getJavaCommand() + " " + build.getJavaOptions()));
		jfile.delete();
	}
	

	private File createFile(Path resolve) throws IOException {
		Path container = resolve.getParent();
		Files.createDirectories(container);
		BufferedWriter out = new BufferedWriter(new FileWriter(resolve.toFile()));
		out.write("foo\n");
		out.close();
		return resolve.toFile();
	}

	
	
}
