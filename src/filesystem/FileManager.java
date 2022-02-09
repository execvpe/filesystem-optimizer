package filesystem;

import hashing.FileHash;
import main.ArgParser;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class FileManager {
    private final ArgParser argParser;
    private final FileHash fileHash;
    private final HashSet<FileAttributeWrapper> wrappers;
    private boolean listDuplicates;
    private boolean listEmptyDirs;
    private boolean listEmptyFiles;
    private Long maxFileSize;
    private Long minFileSize;

    public FileManager(ArgParser argParser) {
        this.argParser = argParser;
        this.fileHash = new FileHash((String) argParser.getValue(ArgParser.ValueKey.HASH_ALGORITHM));
        this.wrappers = new HashSet<>(1024);
    }


    public void crawlFilesystem(String directoryPathname) throws IOException {
        if (directoryPathname.length() == 0)
            return;

        reloadArgs();

        File directory = new File(directoryPathname);
        if (!directory.exists()) {
            printDebug("\"" + directory.getCanonicalPath() + "\" does not exist!");
            return;
        }
        if (!directory.isDirectory()) {
            printDebug("\"" + directory.getCanonicalPath() + "\" is not a directory!");
            return;
        }

        traverseDirectory(directory, 0);
    }

    public int elements() {
        return wrappers.size();
    }

    private void readFile(File file, final int depth) throws IOException {
        String fileName = file.getName();
        long fileSize = file.length();

        if (fileSize == 0) {
            if (listEmptyFiles) {
                System.out.println(file.getCanonicalPath());
            }
            //TODO: skip empty files
        }

        if (maxFileSize != null && fileSize > maxFileSize) {
            printDebug("Skipped file \"" + fileName + "\": too big (" + (fileSize / 1_048_576L) + " MiB)", depth);
            return;
        }
        if (minFileSize != null && fileSize < minFileSize) {
            printDebug("Skipped file \"" + fileName + "\": too small (" + (fileSize / 1_024L) + " KiB)", depth);
            return;
        }

        int extensionSeparatorPos = fileName.lastIndexOf(".");

        String filenameExtension = (extensionSeparatorPos == -1)
                ? FileAttributeWrapper.NO_FILENAME_EXTENSION
                : fileName.substring(extensionSeparatorPos + 1);

        FileAttributeWrapper newWrapper
                = new FileAttributeWrapper(filenameExtension, fileHash.getFileChecksum(file), fileSize);

        if (!wrappers.add(newWrapper)) { // File is already known
            if (listDuplicates) {
                System.out.println(file.getCanonicalPath());
            }
        }
    }

    private void reloadArgs() {
        listDuplicates = argParser.isSet(ArgParser.BivalentKey.LIST_DUPLICATES);
        listEmptyDirs = argParser.isSet(ArgParser.BivalentKey.LIST_EMPTY_DIRS);
        listEmptyFiles = argParser.isSet(ArgParser.BivalentKey.LIST_EMPTY_FILES);
        maxFileSize = (Long) argParser.getValue(ArgParser.ValueKey.MAX_FILE_SIZE);
        minFileSize = (Long) argParser.getValue(ArgParser.ValueKey.MIN_FILE_SIZE);
    }

    private void traverseDirectory(File directory, final int depth) throws IOException {
        printDebug("Entering directory \"" + directory.getCanonicalPath() + "\"", depth);
        File[] directoryEntries = directory.listFiles();

        if (directoryEntries == null)
            return;

        if (listEmptyDirs && directoryEntries.length == 0) {
            System.out.println(directory.getCanonicalPath());
            return;
        }

        int analyzedEntries = 0;
        for (File actual : directoryEntries) {
            if (actual.isFile()) {
                analyzedEntries++;
                readFile(actual, depth + 1);
                continue;
            }
            if (actual.isDirectory()) {
                analyzedEntries++;
                traverseDirectory(actual, depth + 1);
            }
        }

        printDebug("Analyzed " + analyzedEntries + "/" + directoryEntries.length
                + " entries in \"" + directory.getCanonicalPath() + "\"", depth);
    }

    private static void printDebug(String message, int depth) {
        if (depth > 0) {
            System.err.printf("%1$" + depth + "s", " ");
        }
        System.err.println(message);
    }

    private static void printDebug(String message) {
        printDebug(message, -1);
    }
}
