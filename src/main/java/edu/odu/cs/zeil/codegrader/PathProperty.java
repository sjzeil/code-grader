/**
 * 
 */
package edu.odu.cs.zeil.codegrader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A wrapper for java.nio.Path that allows construction from
 * a string.
 * 
 * @author zeil
 *
 */
public class PathProperty {
	
	/**
	 * The actual path.
	 */
	final private Path path;
	
	/**
	 * Create a path property from a string
	 * @param pathStr string representation of a path
	 */
	public PathProperty (final String path0) {
		String pathStr = path0;
		if (File.separatorChar == '\\' && path0.contains("/")) {
			pathStr = path0.replace('/', File.separatorChar);
		} else if (File.separatorChar == '/' && path0.contains("\\")) {
			pathStr = path0.replace('\\', File.separatorChar);
		}
		path = Paths.get(pathStr);
	}
	
	public Path getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		return path.toString();
	}
}
