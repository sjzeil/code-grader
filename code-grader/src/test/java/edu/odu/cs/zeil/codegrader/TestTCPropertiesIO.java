package edu.odu.cs.zeil.codegrader;

//CHECKSTYLE:OFF

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;

public class TestTCPropertiesIO {

	// @Disabled("Problem with Jackson data mapper?")
	@Test
	void testReadWrite() throws TestConfigurationError {
		TestCasePropertiesBase tcp = TestCasePropertiesBase.defaults();
		tcp.params = Optional.of("a b c");
		tcp.launch = Optional.of("./myProgram");
		tcp.weight = OptionalInt.of(42);

		OracleProperties option1 = new OracleProperties();
		option1.caseSig = true;
		OracleProperties option2 = new OracleProperties();
		option2.oracle = "smart";
		option2.cap = 80;
		option2.caseSig = false;
		tcp.grading = Optional.of(new ArrayList<>());
		tcp.grading.get().add(option1);
		tcp.grading.get().add(option2);

		String tcpAsString = tcp.toString();
		assertThat(tcpAsString, containsString("a b c"));
		assertThat(tcpAsString, containsString("./myProgram"));
		assertThat(tcpAsString, containsString("42"));
		// System.err.println(tcpAsString);

		TestCasePropertiesBase tcp2 
			= TestCasePropertiesBase.loadYAML(tcpAsString);
		assertThat(tcp2.params.get(), equalTo(tcp.params.get()));
		assertThat(tcp2.launch.get(), equalTo(tcp.launch.get()));
		assertThat(tcp2.weight.getAsInt(), equalTo(tcp.weight.getAsInt()));
		assertThat(tcp2.grading.get().size(), equalTo(2));
	}

	@Test
	void testRead() throws TestConfigurationError {

		String input = "params: \"a b\"\nweight: 42\n";

		TestCasePropertiesBase tcp = TestCasePropertiesBase.loadYAML(input);
		assertThat(tcp.params.get(), equalTo("a b"));
		assertThat(tcp.weight.getAsInt(), equalTo(42));
		assertThat(tcp.grading.isPresent(), equalTo(false));
	}

	@Test
	void testReadMultiListItem() throws TestConfigurationError {

		String input = "onFail: [\"failTag1\", \"failTag2\"]\n";

		TestCasePropertiesBase tcp = TestCasePropertiesBase.loadYAML(input);
		assertThat(tcp.onFail.get().size(), equalTo(2));
		assertThat(tcp.onFail.get().get(0), equalTo("failTag1"));
		assertThat(tcp.onFail.get().get(1), equalTo("failTag2"));
	}

}
