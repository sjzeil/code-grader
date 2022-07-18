package edu.odu.cs.zeil.codegrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public final class FileUtils {

    /**
     * error logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /**
     * Directory copy utility.
     * 
     * (from
     * https://stackoverflow.com/questions/29076439/java-8-copy-directory-recursively/60621544#60621544)
     * 
     * @param source  directory to copy
     * @param target  destination for copy
     * @param options copy options, e.g., StandardCopyOption.REPLACE_EXISTING
     * @throws IOException if files cannot be accessed/copied
     */
    public static void copyDirectory(Path source, Path target,
            CopyOption... options)
            throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs)
                    throws IOException {
                Path newDir = target.resolve(source.relativize(dir));
                Files.createDirectories(newDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)),
                        options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Directory delete utility.
     * 
     * @param dir directory to delete
     * @throws IOException if files cannot be accessed/deleted
     */
    public static void deleteDirectory(Path dir)
            throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Read an entire test file into a string.
     * 
     * @param file file to be read
     * @return contents of that file (empty if any IO error occurs)
     */
    public static String readTextFile(File file) {
        StringBuffer result = new StringBuffer();
        try (BufferedReader in = new BufferedReader(
                new FileReader(file, Charset.forName("UTF-8")))) {
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                result.append(line);
                result.append("\n");
            }
        } catch (IOException ex) {
            LOG.warn("Error in readContentsOf when reading from "
                    + file.getAbsolutePath()
                    + ": treated as EOF.", ex);
        }
        return result.toString();
    }

    /**
     * Load a file of YAML content into a map structure.
     * 
     * @param yamlFile the file to read
     * @return the YAML content as a map from field names onto objects.
     */
    public static Map<String, Object> loadYaml(File yamlFile) {
        Yaml yaml = new Yaml();
        try (InputStream yamlIn = new FileInputStream(yamlFile)) {
            Map<String, Object> results = yaml.load(yamlIn);
            return results;
        } catch (IOException ex) {
            LOG.error("unable to process yaml input from "
                    + yamlFile.getAbsolutePath(), ex);
            return new HashMap<>();
        }
    }

    /**
     * Find a file with the given extension in a directory.
     * @param dir a path to a directory 
     * @param extension the desired file extension
     * @return a file with the desired extension, if one exists.
     */
    public static Optional<File> findFile(Path dir, String extension) {
        File[] files = dir.toFile().listFiles();
        if (files != null) {
            for (File file: files) {
                if (file.getName().endsWith(extension)) {
                    return Optional.of(file);
                }
            }
        }
        return Optional.empty();
    }

    private FileUtils() {
    }

    public static void writeTextFile(Path resolve, String output) {
    }

}
