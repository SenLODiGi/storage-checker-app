package com.storagechecker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.io.IOException;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class StorageGUI {
    private StorageAnalyzer analyzer;
    private FileManager fileManager;
    private ReportGenerator reportGenerator;
    // Removed the frame field as it will be declared locally in initializeGUI
    private JTable diskTable;
    private JTable fileTypeTable;
    private JTextArea fileListArea;
    private JTextField directoryField;
    // Removed extensionComboBox field as it will be declared locally in relevant methods
    // Removed moveToComboBox field as it will be declared locally in initializeGUI
    private JLabel statusLabel;

    public StorageGUI() {
        analyzer = new StorageAnalyzer();
        fileManager = new FileManager();
        reportGenerator = new ReportGenerator();
        initializeGUI();
    }

    private void initializeGUI() {
        JFrame frame = new JFrame("Storage Checker");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Disk Overview
        JPanel diskPanel = new JPanel(new BorderLayout());
        diskTable = new JTable(new DefaultTableModel(new Object[]{"Drive", "Total", "Used", "Free"}, 0));
        diskPanel.add(new JScrollPane(diskTable), BorderLayout.CENTER);
        tabbedPane.addTab("Disk Overview", diskPanel);

        // Tab 2: File Type Usage
        JPanel fileTypePanel = new JPanel(new BorderLayout());
        fileTypeTable = new JTable(new DefaultTableModel(new Object[]{"Extension", "Size", "Files"}, 0));
        fileTypePanel.add(new JScrollPane(fileTypeTable), BorderLayout.CENTER);
        tabbedPane.addTab("File Type Usage", fileTypePanel);

        // Tab 3: File Management
        JPanel fileManagementPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout());
        directoryField = new JTextField("C:\\", 20);
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                directoryField.setText(chooser.getSelectedFile().toString());
            }
        });
        inputPanel.add(new JLabel("Scan Directory:"));
        inputPanel.add(directoryField);
        inputPanel.add(browseButton);

        JComboBox<String> extensionComboBox = new JComboBox<>(new String[]{".mp4", ".jpg", ".pdf", ".docx", ".mp3", ".png", ".txt", ".zip", ".xlsx"});
        inputPanel.add(new JLabel("Select Extension:"));
        inputPanel.add(extensionComboBox);

        JButton deleteButton = new JButton("Delete All");
        deleteButton.addActionListener(e -> {
            String ext = (String) extensionComboBox.getSelectedItem();
            Path root = Paths.get(directoryField.getText());
            statusLabel.setText(fileManager.deleteFilesByExtension(ext, root));
            refreshData();
        });

        JButton listButton = new JButton("List Files");
        listButton.addActionListener(e -> {
            String ext = (String) extensionComboBox.getSelectedItem();
            Path root = Paths.get(directoryField.getText());
            List<String[]> files = fileManager.listFilesByExtension(ext, root);
            fileListArea.setText("");
            for (String[] file : files) {
                fileListArea.append(String.format("%s (%s, %s)%n", file[0], file[1], file[2]));
            }
        });

        JComboBox<String> moveToComboBox = new JComboBox<>(new String[]{"Documents", "Videos", "Pictures", "Music"});
        JButton moveButton = new JButton("Move");
        moveButton.addActionListener(e -> {
            String moveTo = (String) moveToComboBox.getSelectedItem();
            Path targetDir = Paths.get(System.getProperty("user.home"), moveTo);
            List<String[]> files = fileManager.listFilesByExtension((String) extensionComboBox.getSelectedItem(), Paths.get(directoryField.getText()));
            statusLabel.setText(fileManager.moveFilesToDirectory(files, targetDir));
            refreshData();
        });

        inputPanel.add(deleteButton);
        inputPanel.add(listButton);
        inputPanel.add(new JLabel("Move to:"));
        inputPanel.add(moveToComboBox);
        inputPanel.add(moveButton);

        fileListArea = new JTextArea(10, 40);
        fileListArea.setEditable(false);
        fileManagementPanel.add(inputPanel, BorderLayout.NORTH);
        fileManagementPanel.add(new JScrollPane(fileListArea), BorderLayout.CENTER);
        tabbedPane.addTab("File Management", fileManagementPanel);

        // Bottom Panel: Refresh and Report
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        JButton reportButton = new JButton("Generate Report");
        reportButton.addActionListener(e -> {
            Path reportPath = Paths.get(System.getProperty("user.home"), "Documents", "StorageReport_" + LocalDate.now() + ".txt");
            try {
                statusLabel.setText(reportGenerator.generateReport(analyzer.getDiskInfo(), analyzer.getFileTypeSizes(), analyzer.getFileTypeCounts(), reportPath));
            } catch (IOException ex) {
                statusLabel.setText("Error generating report: " + ex.getMessage());
            }
        });
        statusLabel = new JLabel("Status: Ready");
        bottomPanel.add(refreshButton);
        bottomPanel.add(reportButton);
        bottomPanel.add(statusLabel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        refreshData();
    }

    private static final String TOTAL_SUFFIX = "_total";

    private void refreshData() {
        Path root = Paths.get(directoryField.getText());
        analyzer.analyzeFileTypes(root);

        // Update Disk Table
        DefaultTableModel diskModel = (DefaultTableModel) diskTable.getModel();
        diskModel.setRowCount(0);
        try {
            Map<String, Object> diskInfo = analyzer.getDiskInfo();
            for (String key : diskInfo.keySet()) {
                if (key.endsWith(TOTAL_SUFFIX)) {
                    String drive = key.replace(TOTAL_SUFFIX, "");
                    diskModel.addRow(new Object[]{
                            drive,
                            StorageAnalyzer.formatSize((Long) diskInfo.get(drive + TOTAL_SUFFIX)),
                            StorageAnalyzer.formatSize((Long) diskInfo.get(drive + "_used")),
                            StorageAnalyzer.formatSize((Long) diskInfo.get(drive + "_free"))
                    });
                }
            }
        } catch (IOException ex) {
            statusLabel.setText("Error retrieving disk info: " + ex.getMessage());
        }

        // Update File Type Table
        DefaultTableModel fileTypeModel = (DefaultTableModel) fileTypeTable.getModel();
        fileTypeModel.setRowCount(0);
        Map<String, Long> fileTypeSizes = analyzer.getFileTypeSizes();
        Map<String, Integer> fileTypeCounts = analyzer.getFileTypeCounts();
        for (Map.Entry<String, Long> entry : fileTypeSizes.entrySet()) {
            String ext = entry.getKey();
            Long size = entry.getValue();
            fileTypeModel.addRow(new Object[]{ext, StorageAnalyzer.formatSize(size), fileTypeCounts.get(ext)});
        }

        statusLabel.setText("Status: Last updated: " + LocalDate.now());
    }
}