package edu.odu.cs.zeil.codegrader;


public class TestSuite {

	TestProperties properties;
	
	public enum PhaseNames {Setup, Build, Execute, Oracle, Report};

	private final Phase[] phases = new Phase[PhaseNames.Report.ordinal()+1];
	
    public TestSuite(TestProperties testProperties) {
    	properties = testProperties;
    	/*
    	phases[PhaseNames.Setup.ordinal()] = new SetupPhase(properties);
		phases[PhaseNames.Build.ordinal()] = new BuildPhase(properties);
		phases[PhaseNames.Execute.ordinal()] = new ExecutePhase(properties);
		phases[PhaseNames.Oracle.ordinal()] = new OraclePhase(properties);
		phases[PhaseNames.Report.ordinal()] = new ReportPhase(properties);
		*/
    }

    public void run(PhaseNames start, PhaseNames finish) {
    	for (int ph = start.ordinal(); ph != finish.ordinal() + 1; ++ph) {
    		phases[ph].runPhase();
    	}
    }

    public Object getOutput() {
        return null;
    }

    public Object getErr() {
        return null;
    }

    public Object getTime() {
        return null;
    }

}
