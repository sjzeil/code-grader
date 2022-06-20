package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.PathProperty;

public class TestPathProperty {
	
	
	
	@Test
	void testSimplePath() {
		
		PathProperty prop = new PathProperty("foo");
		Path path = Paths.get("foo");
		assertThat (prop.getPath(), equalTo(path));
	}

	@Test
	void testRelativePath() {
		PathProperty prop = new PathProperty("foo/bar");
		Path path = Paths.get("foo", "bar");
		assertThat (prop.getPath(), equalTo(path));
	}

	@Test
	void testRelativePathWin() {
		PathProperty prop = new PathProperty("foo\\bar");
		Path path = Paths.get("foo", "bar");
		assertThat (prop.getPath(), equalTo(path));
	}

}
