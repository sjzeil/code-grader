/**
 * 
 */
package edu.odu.cs.zeil.codegrader.oracle;

import edu.odu.cs.zeil.codegrader.ReflectiveProperties;

/**
 * Options for controlling the oracle behavior.
 * 
 * @author zeil
 *
 */
public class OracleProperties extends ReflectiveProperties {
	
	/**
	 * Should strings comparisons reject upper/lowercase differences?
	 */
	public boolean caseSensitive = true;
}
