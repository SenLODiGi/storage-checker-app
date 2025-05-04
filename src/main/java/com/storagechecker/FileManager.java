package com.storagechecker;

import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

class FileManager {
    private List<String[]> fileList = new ArrayList<>();

    public String deleteFilesByExtension(String extension, Path root) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().toLowerCase().endsWith(extension.toLowerCase())) {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return "Deleted all " + extension + " files.";
        } catch (IOException e) {
            return "Error deleting files: " + e.getMessage();
        }
    }

    public List<String[]> listFilesByExtension(String extension, Path root) {
        fileList.clear();
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().toLowerCase().endsWith(extension.toLowerCase())) {
                        try {
                            String size = StorageAnalyzer.formatSize(Files.size(file));
                            String date = Files.getLastModifiedTime(file).toString().split("T")[0];
                            fileList.add(new String[]{file.toString(), size, date});
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public String moveFilesToDirectory(List<String[]> files, Path targetDir) {
        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            for (String[] fileInfo : files) {
                Path source = Paths.get(fileInfo[0]);
                Path target = targetDir.resolve(source.getFileName());
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "Moved " + files.size() + " files to " + targetDir;
        } catch (IOException e) {
            return "Error moving files: " + e.getMessage();
        }
    }
}