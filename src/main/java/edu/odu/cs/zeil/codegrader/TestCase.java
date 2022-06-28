package edu.odu.cs.zeil.codegrader;


public class TestCase {

	TestProperties properties;
	
		
    public TestCase(TestProperties testProperties) {
    	properties = testProperties;
    }

    public void run() {
    	runTheTest();
    	//new TestOracle(properties).run();
    }

    private void runTheTest() {
		// TODO Auto-generated method stub
		
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
