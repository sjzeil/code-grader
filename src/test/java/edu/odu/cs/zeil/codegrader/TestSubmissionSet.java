package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class TestSubmissionSet {



    @Test
    void testNoVersions() {
        Assignment asst = new Assignment();
        Path submissionsPath = Paths.get("src", "test", "data",
            "java-sqrt-assignment", "submissions");
        asst.setSubmissionsDirectory(submissionsPath);
        SubmissionSet submissionSet = new SubmissionSet(asst);
        String[] expected = {"bad", "doesNotCompile", "flattened", "imprecise",
            "misspelling", "packaged", "perfect", "unformatted", "whitespace"};

        int i = 0;
        for (Submission sub: submissionSet) {
            assertThat(sub.getSubmittedBy(), is(expected[i]));
            ++i;
        }
        assertThat(i, is(expected.length));
    }

    @Test
    void testVersions() {
        Assignment asst = new Assignment();
        Path submissionsPath = Paths.get("src", "test", "data",
            "versioned", "submissions");
        asst.setSubmissionsDirectory(submissionsPath);
        SubmissionSet submissionSet = new SubmissionSet(asst);
        String[] expectedNames = {"jones", "smith"};
        String[] expectedDirNames = {"jones.1", "smith.12"};

        int i = 0;
        for (Submission sub: submissionSet) {
            assertThat(sub.getSubmittedBy(), is(expectedNames[i]));
            assertThat(sub.getSubmissionDirectory().getFileName(), 
                is(expectedDirNames[i]));
            ++i;
        }
        assertThat(i, is(expectedNames.length));
    }

}
