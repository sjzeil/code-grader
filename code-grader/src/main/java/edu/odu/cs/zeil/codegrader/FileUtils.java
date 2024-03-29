package edu.odu.cs.zeil.codegrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
     * 
     * @param source     directory to copy
     * @param target     destination for copy
     * @param inclusions list of patterns to include.
     *                   If null or empty, copy all files.
     * @param exclusions list of patterns to include.
     *                   If null or empty, exclude no files.
     *                   Exclusions are applied after inclusions.
     * @param options    copy options, e.g., StandardCopyOption.REPLACE_EXISTING
     * @throws IOException if files cannot be accessed/copied
     */
    public static void copyDirectory(Path source, Path target,
            List<String> inclusions,
            List<String> exclusions,
            CopyOption... options)
            throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs)
                    throws IOException {
                // Path newDir = target.resolve(source.relativize(dir));
                // Files.createDirectories(newDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs)
                    throws IOException {
                Path relativeSrc = source.relativize(file);
                if (matchesAny(relativeSrc, inclusions)
                        && matchesNone(relativeSrc, exclusions)) {
                    Path destinationPath = target.resolve(relativeSrc);
                    Path destPathParent = destinationPath.getParent();
                    if (destPathParent != null 
                        && !destPathParent.toFile().exists()) {
                        Files.createDirectories(destPathParent);
                    }
                    Files.copy(file, destinationPath, options);
                }
                return FileVisitResult.CONTINUE;
            }

            private boolean matchesAny(Path relativeSrc,
                    List<String> patterns) {
                if (patterns == null || patterns.size() == 0) {
                    return true;
                }
                for (String patternStr : patterns) {
                    PathMatcher matcher = FileSystems.getDefault()
                            .getPathMatcher("glob:" + patternStr);
                    if (matcher.matches(relativeSrc)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean matchesNone(Path relativeSrc,
                    List<String> patterns) {
                if (patterns == null || patterns.size() == 0) {
                    return true;
                }
                for (String patternStr : patterns) {
                    PathMatcher matcher = FileSystems.getDefault()
                            .getPathMatcher("glob:" + patternStr);
                    if (matcher.matches(relativeSrc)) {
                        return false;
                    }
                }
                return true;
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
     * 
     * @param dir       a path to a directory
     * @param extension the desired file extension
     * @return a file with the desired extension, if one exists.
     */
    public static Optional<File> findFile(Path dir, String extension) {
        File[] files = dir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(extension)) {
                    return Optional.of(file);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Find all files with the given extension in a directory.
     * 
     * @param dir       a path to a directory
     * @param extension the desired file extension
     * @return a list of files with the desired extension.
     */
    public static List<File> findAllFiles(Path dir, String extension) {
        List<File> results = new ArrayList<>();
        File[] files = dir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(extension)) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    /**
     * Find all files with the given extension in a directory tree.
     * 
     * @param dir       a path to a directory
     * @param extension the desired file extension
     * @return a list of files with the desired extension.
     */
    public static List<File> findAllDeepFiles(Path dir, String extension) {
        List<File> results = new ArrayList<>();
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                        IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs)
                        throws IOException {
                    if (file.toFile().getName().endsWith(extension)) {
                        results.add(file.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.warn("Problem walking the directory tree " + dir.toString()
                    + "\n" + e.getMessage());
        }
        return results;
    }

    /**
     * Find all directories containing files with the given extension in
     * a directory tree.
     * 
     * @param dir       a path to a directory
     * @param extension the desired file extension
     * @return a list of directories
     */
    public static List<File> findDirectoriesContaining(
            Path dir,
            String extension) {
        Set<File> results = new HashSet<>();
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                        IOException exc)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs)
                        throws IOException {
                    Path parent = file.getParent();
                    if (parent != null 
                        && file.toFile().getName().endsWith(extension)) {
                            results.add(parent.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.warn("Problem walking the directory tree " + dir.toString()
                    + "\n" + e.getMessage());
        }

        return new ArrayList<File>(results);
    }

    private FileUtils() {
    }

    /**
     * WWrite a string into a file.
     * 
     * @param destination  path at which to write
     * @param outputString string to write
     * @throws TestConfigurationError if file cannot be written.
     */
    public static void writeTextFile(Path destination, String outputString)
            throws TestConfigurationError {
        try (FileWriter output = new FileWriter(destination.toFile())) {
            output.write(outputString);
        } catch (IOException ex) {
            String message = "Could not write to "
                    + destination.toString();
            LOG.error(message, ex);
            throw new TestConfigurationError(message + "\n" + ex.getMessage());
        }
    }

}
