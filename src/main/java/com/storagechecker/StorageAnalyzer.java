package com.storagechecker;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.io.IOException;

// Removed unused import

class StorageAnalyzer {
    private static final String[] FILE_EXTENSIONS = {".mp4", ".jpg", ".pdf", ".docx", ".mp3", ".png", ".txt", ".zip", ".xlsx"};
    private Map<String, Long> fileTypeSizes = new HashMap<>();
    private Map<String, Integer> fileTypeCounts = new HashMap<>();

    public Map<String, Object> getDiskInfo() throws IOException {
        Map<String, Object> diskInfo = new HashMap<>();
        try {
            for (FileStore store : FileSystems.getDefault().getFileStores()) {
                String drive = store.toString().split(" ")[0];
                long total = store.getTotalSpace();
                long free = store.getUnallocatedSpace();
                long used = total - free;
                diskInfo.put(drive + "_total", total);
                diskInfo.put(drive + "_used", used);
                diskInfo.put(drive + "_free", free);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diskInfo;
    }

    public void analyzeFileTypes(Path root) {
        fileTypeSizes.clear();
        fileTypeCounts.clear();
        for (String ext : FILE_EXTENSIONS) {
            fileTypeSizes.put(ext, 0L);
            fileTypeCounts.put(ext, 0);
        }

        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.toString().toLowerCase();
                    for (String ext : FILE_EXTENSIONS) {
                        if (fileName.endsWith(ext)) {
                            try {
                                long size = Files.size(file);
                                fileTypeSizes.put(ext, fileTypeSizes.get(ext) + size);
                                fileTypeCounts.put(ext, fileTypeCounts.get(ext) + 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Long> getFileTypeSizes() {
        return fileTypeSizes;
    }

    public Map<String, Integer> getFileTypeCounts() {
        return fileTypeCounts;
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}