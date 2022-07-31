package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


import org.junit.jupiter.api.Test;


import java.util.Optional;
import java.util.OptionalInt;


public class TestTCPropertiesBase {
	

	@Test
	void testReadWrite() 
        throws TestConfigurationError {
        TestCasePropertiesBase tcp = new TestCasePropertiesBase();
        tcp.params = Optional.of("a b c");
        tcp.launch = Optional.of("./myProgram");
        tcp.points = OptionalInt.of(42);

        OracleProperties option1 = new OracleProperties();
        option1.caseSig = true;
        OracleProperties option2 = new OracleProperties();
        option2.oracle = "smart";
        option2.cap = 80;
        option2.caseSig = false;
        tcp.grading.add(option1);
        tcp.grading.add(option2);

        String tcpAsString = tcp.toString();
        assertThat(tcpAsString, containsString("a b c"));
        assertThat(tcpAsString, containsString("./myProgram"));
        assertThat(tcpAsString, containsString("42"));
        //System.err.println(tcpAsString);

        TestCasePropertiesBase tcp2 
                = TestCasePropertiesBase.loadYAML(tcpAsString);
        assertThat(tcp2.params.get(), equalTo(tcp.params.get()));
        assertThat(tcp2.launch.get(), equalTo(tcp.launch.get()));
        assertThat(tcp2.points.getAsInt(), equalTo(tcp.points.getAsInt()));
        assertThat(tcp2.grading.size(), equalTo(2));
	}

    @Test
	void testRead() throws TestConfigurationError {
        
        String input = "params: \"a b\"\npoints: 42\n";

        TestCasePropertiesBase tcp = TestCasePropertiesBase.loadYAML(input);
        assertThat(tcp.params.get(), equalTo("a b"));
        assertThat(tcp.points.getAsInt(), equalTo(42));
        assertThat(tcp.grading.size(), equalTo(0));
	}


}
