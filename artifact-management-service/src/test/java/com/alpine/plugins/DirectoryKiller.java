package com.alpine.plugins;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * I keep writing this!
 */
class DirectoryKiller extends SimpleFileVisitor<Path> {

    void kill(Path path) throws IOException {
        Files.walkFileTree(path, this);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return super.postVisitDirectory(dir, exc);
    }
}
