package com.storagechecker;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Map;
import java.util.Date;

class ReportGenerator {
    private static final String TOTAL_SUFFIX = "_total";

    public String generateReport(Map<String, Object> diskInfo, Map<String, Long> fileTypeSizes, Map<String, Integer> fileTypeCounts, Path reportPath) {
        StringBuilder report = new StringBuilder();
        report.append("Storage Report - ").append(new Date()).append("\n\n");
        for (String key : diskInfo.keySet()) {
            if (key.endsWith(TOTAL_SUFFIX)) {
                String drive = key.replace(TOTAL_SUFFIX, "");
                report.append(String.format("%s - Total: %s, Used: %s, Free: %s%n",
                        drive,
                        StorageAnalyzer.formatSize((Long) diskInfo.get(drive + TOTAL_SUFFIX)),
                        StorageAnalyzer.formatSize((Long) diskInfo.get(drive + "_used")),
                        StorageAnalyzer.formatSize((Long) diskInfo.get(drive + "_free"))));
            }
        }
        report.append("\nFile Type Usage:\n");
        for (Map.Entry<String, Long> entry : fileTypeSizes.entrySet()) {
            String ext = entry.getKey();
            Long size = entry.getValue();
            report.append(String.format("%s - Size: %s, Files: %d%n",
                    ext, StorageAnalyzer.formatSize(size), fileTypeCounts.get(ext)));
        }

        try {
            Files.writeString(reportPath, report.toString());
            return "Report saved to " + reportPath;
        } catch (IOException e) {
            return "Error saving report: " + e.getMessage();
        }
    }
}