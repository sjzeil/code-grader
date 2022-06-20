package edu.odu.cs.zeil.codegrader;

import edu.odu.cs.zeil.codegrader.oracle.OracleProperties;

public class TestPhase implements Phase {

	/**
	 * Configuration details for this Phase.
	 * 
	 * @author zeil
	 *
	 */
	public static class TestProperties extends ReflectiveProperties {
		public OracleProperties oracle = new OracleProperties();
	}

	@Override
	public boolean runPhase() {
		// TODO Auto-generated method stub
		return false;
	}

	public ReflectiveProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
