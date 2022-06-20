package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;

import edu.odu.cs.zeil.codegrader.ReflectiveProperties;

public class TestReflectiveProperties {
	
	public static class NoStringConversion {
		public NoStringConversion (int xx) {x = xx;}
		
		int x;
	}
	
	public static class InnerPropertiesSet extends ReflectiveProperties {
		public String address = "nowhere";
	}

	public static class PropertiesSet extends ReflectiveProperties {
		public String name;
		public File file;
		public NoStringConversion unsettable = new NoStringConversion(42);
		public InnerPropertiesSet inner = new InnerPropertiesSet();
	}
	
	
	@Test
	void testSetStringParam() {
		
		PropertiesSet p = new PropertiesSet();
		
		p.setByReflection("name", "Steve");
		assertThat (p.name, equalTo("Steve"));
		assertThat (p.getByReflection("name"), equalTo("Steve"));
	}

	@Test
	void testSetFileParam() {
		
		PropertiesSet p = new PropertiesSet();
		
		p.setByReflection("file", ".");
		assertThat(p.file, equalTo(new File(".")));
		assertThat (p.getByReflection("file"), equalTo(new File(".")));
	}

	@Test
	void testSetParamDoesNotExist() {
		
		PropertiesSet p = new PropertiesSet();
		assertThrows (IllegalArgumentException.class,
				() -> {
					p.setByReflection("bogus", "0");
				}
				);
	}

	@Test
	void testSetParamCannotBeConverted() {
		
		PropertiesSet p = new PropertiesSet();
		assertThrows (IllegalArgumentException.class,
				() -> {
					p.setByReflection("nsc", "0");
				}
				);
	}

	@Test
	void testSetInnerParam() {
		
		PropertiesSet p = new PropertiesSet();
		
		p.setByReflection("inner.address", "somewhere");
		assertThat (p.inner.address, equalTo("somewhere"));
		assertThat (p.getByReflection("inner.address"), equalTo("somewhere"));
	}

	
	
	
	
}
