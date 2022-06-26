package edu.odu.cs.zeil.codegrader;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

    /**
     * Directory copy utility 
     * 
     * (from https://stackoverflow.com/questions/29076439/java-8-copy-directory-recursively/60621544#60621544)
     * 
     * @param source  directory to copy
     * @param target  destination for copy
     * @param options  copy options, e.g., StandardCopyOption.REPLACE_EXISTING
     * @throws IOException if files cannot be accessed/copied
     */
    public static void copyDirectory(Path source, Path target, CopyOption... options)
            throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }


        /**
     * Directory delete utility 
     * 
     * @param dir  directory to delete
     * @throws IOException if files cannot be accessed/deleted
     */
    public static void deleteDirectory(Path dir)
            throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete (dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
