package filesystem;

import hashing.FileHash;
import main.ArgParser;
import main.Main;

import java.io.*;
import java.util.*;

public class FileManager {
    private static final String SECTION_SEPARATOR = "* // * // * // *";
    private final ArgParser argParser;
    private final ArrayList<String> crawledPaths;
    private final FileHash fileHash;
    private final HashSet<FileAttributeWrapper> wrappers;
    private boolean listDuplicates;
    private boolean listEmptyDirs;
    private boolean listEmptyFiles;
    private Long maxFileSize;
    private Long minFileSize;
    private boolean skipEmptyFiles;

    public FileManager(ArgParser argParser) {
        this.argParser = argParser;
        this.crawledPaths = new ArrayList<>();
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

        crawledPaths.add(directory.getCanonicalPath());
        traverseDirectory(directory, 0);
    }

    public void dumpWrappers(File dumpFile) throws IOException {
        if (dumpFile.exists()) {
            printDebug("File \"" + dumpFile.getCanonicalPath() + "\" does already exist! Do nothing...");
            return;
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dumpFile), 1_048_576);
        bufferedWriter.write("FILE WRAPPER DUMP (" + elements() + ") - " + new Date());
        bufferedWriter.newLine();
        for (String canonicalPath : crawledPaths) {
            bufferedWriter.write(canonicalPath);
            bufferedWriter.newLine();
        }
        bufferedWriter.write(SECTION_SEPARATOR);
        bufferedWriter.newLine();
        for (FileAttributeWrapper w : wrappers) {
            bufferedWriter.write(w.toString());
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    public int elements() {
        return wrappers.size();
    }

    public void loadWrappers(File dumpedWrappers) throws IOException {
        if (!dumpedWrappers.exists()) {
            printDebug("File \"" + dumpedWrappers.getCanonicalPath() + "\" does already exist! Do nothing...");
            return;
        }

        Scanner scanner = new Scanner(dumpedWrappers);
        if (!scanner.hasNextLine() || !scanner.nextLine().startsWith("FILE WRAPPER DUMP")) {
            scanner.close();
            printDebug("Not a valid wrapper dump!");
            return;
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals(SECTION_SEPARATOR)) {
                break;
            }
            crawledPaths.add("L>" + line);
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!wrappers.add(FileAttributeWrapper.fromString(line))) {
                printDebug("Wrapper represented by \"" + line + "\" is already present!");
            }
        }
        scanner.close();
    }

    private void readFile(File file, final int depth) throws IOException {
        String fileName = file.getName();
        long fileSize = file.length();

        if (fileSize == 0) {
            if (listEmptyFiles) {
                System.out.println(file.getCanonicalPath());
            }
            if (skipEmptyFiles) {
                return;
            }
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
        skipEmptyFiles = argParser.isSet(ArgParser.BivalentKey.SKIP_EMPTY_FILES);
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
        Main.hint(message);
    }
}
