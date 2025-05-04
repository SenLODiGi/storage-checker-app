# Storage Checker App

## Overview
The Storage Checker App is a Java-based application designed to monitor and manage storage on your computer. It focuses on user-created, downloaded, or pasted files (excluding `.exe` files). The app provides a user-friendly graphical interface to view disk usage, analyze file types, and manage files by deleting, listing, or moving them to common directories.

## Features

- Displays total, used, and free storage for all drives.
- Analyzes storage usage for various file types, including:
   - `.mp4`, `.jpg`, `.pdf`, `.docx`, `.mp3`, `.png`, `.txt`, `.zip`, `.xlsx`.
- Generates a detailed text report of storage usage.
- Allows file management operations such as:
   - Deleting files by extension.
   - Listing files by extension.
   - Moving files to common directories.

## Prerequisites

- **Java Development Kit (JDK)**: Version 17 or later.
- **Maven**: For building the project.

## Installation

1. Clone the repository or download the source code.
    ```bash
    git clone <repository-url>
    cd storage-checker-app
    ```
2. Build the project using Maven:
    ```bash
    mvn package
    ```
    The JAR file will be located in the `target` directory as `storage-checker-app-1.0-SNAPSHOT.jar`.

## Running the Application

- To execute the application, run the following command:
   ```bash
   java -jar target/storage-checker-app-1.0-SNAPSHOT.jar
   ```
- Alternatively, use the generated `.exe` file (see the section below for details).

## Creating a Windows .exe

1. Ensure JDK 14 or later is installed with the `jpackage` tool.
2. Run the following command to create a Windows executable:
    ```bash
    jpackage --input target --name StorageChecker --main-jar storage-checker-app-1.0-SNAPSHOT.jar --main-class com.storagechecker.Main --win-shortcut --win-menu
    ```

## Usage

- Launch the application to access the following features:
   - **Disk Overview**: View total, used, and free storage for all drives.
   - **File Type Usage**: Analyze storage usage by file type.
   - **File Management**: Scan directories, manage files, and generate reports.
- Use the intuitive GUI to perform file operations and monitor storage effectively.

## Contributing

We welcome contributions! Feel free to submit issues or pull requests to improve the project.

## License

This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for more details.