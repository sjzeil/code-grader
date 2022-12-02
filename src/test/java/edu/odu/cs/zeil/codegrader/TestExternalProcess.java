package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class TestExternalProcess {

    public static boolean isWindows 
        = System.getProperty("os.name").contains("Windows");
    public String ls = (isWindows) ? "dir" : "ls";
    public String cat0 = (isWindows) ? "findstr x*" : "cat";
    public String cat2 = (isWindows) ? "type" : "cat";
    public String sleep5 = (isWindows) ? "timeout /t 5" : "sleep 5000";

    @Test
    void testBasicExt() throws IOException {
        Path testDir = Paths.get("build", "test-data", "ext");
        if (Files.exists(testDir)) {
            FileUtils.deleteDirectory(testDir);
        }
        Files.createDirectories(testDir);
        Path file1 = testDir.resolve("foo.txt");
        FileUtils.writeTextFile(file1, "foo");
        Path file2 = testDir.resolve("bar.txt");
        FileUtils.writeTextFile(file2, "bar");

        ExternalProcess ep = new ExternalProcess(testDir, ls,
            1, null, "testProcess1");
        ep.execute();
        assertThat(ep.getErr(), is(""));
        assertThat(ep.getOutput(), containsString("bar.txt"));
        assertThat(ep.getOutput(), containsString("foo.txt"));
        assertThat(ep.getOnTime(), is(true));
        assertThat(ep.getStatusCode(), is(0));
    }

    @Test
    void testExtStdIn() throws IOException {
        Path testDir = Paths.get("build", "test-data", "ext");
        if (Files.exists(testDir)) {
            FileUtils.deleteDirectory(testDir);
        }
        Files.createDirectories(testDir);
        Path file1 = testDir.resolve("foo.txt");
        FileUtils.writeTextFile(file1, "foo");
        Path file2 = testDir.resolve("bar.txt");
        FileUtils.writeTextFile(file2, "bar");

        ExternalProcess ep = new ExternalProcess(testDir, cat0,
            1, file1.toFile(), "testProcess2");
        ep.execute();
        assertThat(ep.getErr(), is(""));
        assertThat(ep.getOutput(), containsString("foo"));
        assertThat(ep.getOnTime(), is(true));
        assertThat(ep.getStatusCode(), is(0));
    }

    @Test
    void testTimeOut() throws IOException {
        Path testDir = Paths.get("build", "test-data", "ext2");
        if (Files.exists(testDir)) {
            FileUtils.deleteDirectory(testDir);
        }
        Files.createDirectories(testDir);
        Path file1 = testDir.resolve("foo.txt");
        FileUtils.writeTextFile(file1, "foo");
        Path file2 = testDir.resolve("bar.txt");
        FileUtils.writeTextFile(file2, "bar");

        ExternalProcess ep = new ExternalProcess(testDir, sleep5,
            1, file1.toFile(), "testProcess3");
        ep.execute();
        assertThat(ep.getOnTime(), is(false));
    }

    @Test
    void testWildCardsExt() throws IOException {
        Path testDir = Paths.get("build", "test-data", "ext");
        if (Files.exists(testDir)) {
            FileUtils.deleteDirectory(testDir);
        }
        Files.createDirectories(testDir);
        Path file1 = testDir.resolve("foo.txt");
        FileUtils.writeTextFile(file1, "foo");
        Path file2 = testDir.resolve("bar.txt");
        FileUtils.writeTextFile(file2, "bar");

        ExternalProcess ep = new ExternalProcess(testDir, cat2 + " *.txt",
            1, null, "testProcess1");
        ep.execute();
        assertThat(ep.getErr(), is(""));
        assertThat(ep.getOutput(), containsString("bar"));
        assertThat(ep.getOutput(), containsString("foo"));
        assertThat(ep.getOnTime(), is(true));
        assertThat(ep.getStatusCode(), is(0));
    }

    @Test
    void testMultiLineExt() throws IOException {
        Path testDir = Paths.get("build", "test-data", "ext");
        if (Files.exists(testDir)) {
            FileUtils.deleteDirectory(testDir);
        }
        Files.createDirectories(testDir);
        Path file1 = testDir.resolve("foo.txt");
        FileUtils.writeTextFile(file1, "baz");
        Path file2 = testDir.resolve("bar.txt");
        FileUtils.writeTextFile(file2, "qux");

        String command = cat0 + " foo.txt\n"  + cat0 + " bar.txt";
        ExternalProcess ep = new ExternalProcess(testDir, command,
            1, null, "testProcess1");
        ep.execute();
        assertThat(ep.getErr(), is(""));
        assertThat(ep.getOutput(), containsString("baz"));
        assertThat(ep.getOutput(), containsString("qux"));
        assertThat(ep.getOnTime(), is(true));
        assertThat(ep.getStatusCode(), is(0));
    }

}