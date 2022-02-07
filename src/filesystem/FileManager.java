package filesystem;

import com.sun.istack.internal.NotNull;
import hashing.FileHash;
import main.ArgParser;
import main.Main;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileManager {
    private static final Boolean LIST_DUPLICATES = (Boolean) Main.argParser.getArgument(ArgParser.ArgOption.LIST_DUPLICATES);
    private static final Boolean LIST_EMPTY_DIRS = (Boolean) Main.argParser.getArgument(ArgParser.ArgOption.LIST_EMPTY_DIRS);
    private static final Boolean LIST_EMPTY_FILES = (Boolean) Main.argParser.getArgument(ArgParser.ArgOption.LIST_EMPTY_FILES);
    private static final Long MAX_FILE_SIZE = (Long) Main.argParser.getArgument(ArgParser.ArgOption.MAX_FILE_SIZE);
    private static final Long MIN_FILE_SIZE = (Long) Main.argParser.getArgument(ArgParser.ArgOption.MIN_FILE_SIZE);

    private final FileHash fileHash =
            new FileHash((String) Main.argParser.getArgument(ArgParser.ArgOption.HASH_ALGORITHM));
    private final HashSet<FileAttributeWrapper> wrappers = new HashSet<>(1028);

    public void crawlFilesystem(String directoryPathname) throws IOException {
        if (directoryPathname.length() == 0)
            return;

        File directory = new File(directoryPathname);
        if (!directory.exists()) {
            System.err.println("\"" + directory.getCanonicalPath() + "\" does not exist!");
            return;
        }
        if (!directory.isDirectory()) {
            System.err.println("\"" + directory.getCanonicalPath() + "\" is not a directory!");
            return;
        }

        traverseDirectory(directory, 0);
    }

    private void readFile(File file, final int depth) throws IOException {
        String fileName = file.getName();
        long fileSize = file.length();

        if (LIST_EMPTY_FILES != null && fileSize == 0) {
            System.out.println(file.getCanonicalPath());
            return;
        }

        if (LIST_DUPLICATES == null) {
            return;
        }

        if (MAX_FILE_SIZE != null && fileSize > MAX_FILE_SIZE) {
            System.err.printf("%1$" + depth + "s", " ");
            System.err.println("Skipped file \"" + fileName + "\": too big (" + (fileSize / 1_048_576L) + " MiB)");
            return;
        }
        if (MIN_FILE_SIZE != null && fileSize < MIN_FILE_SIZE) {
            System.err.printf("%1$" + depth + "s", " ");
            System.err.println("Skipped file \"" + fileName + "\": too small (" + (fileSize / 1_024L) + " KiB)");
            return;
        }

        int extensionSeparatorPos = fileName.lastIndexOf(".");

        String filenameExtension = (extensionSeparatorPos == -1)
                ? FileAttributeWrapper.NO_FILENAME_EXTENSION
                : fileName.substring(extensionSeparatorPos + 1);

        FileAttributeWrapper newWrapper
                = new FileAttributeWrapper(filenameExtension, fileHash.getFileChecksum(file), fileSize);

        if (!wrappers.add(newWrapper)) { // File is already known
            System.out.println(file.getCanonicalPath());
        }
    }

    private void traverseDirectory(File directory, final int depth) throws IOException {
        if (depth > 0) {
            System.err.printf("%1$" + depth + "s", " ");
        }
        System.err.println("Traversing... \"" + directory.getCanonicalPath() + "\"");
        File[] directoryEntries = directory.listFiles();

        if (directoryEntries == null)
            return;

        if (LIST_EMPTY_DIRS != null && directoryEntries.length == 0) {
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

        if (depth > 0) {
            System.err.printf("%1$" + depth + "s", " ");
        }
        System.err.println("Analyzed " + analyzedEntries + "/" + directoryEntries.length
                + " entries in \"" + directory.getCanonicalPath() + "\"");
    }

    private static class FileAttributeWrapper {
        public static final String NO_FILENAME_EXTENSION = "\\:*/";

        public final String filenameExtension;
        public final byte[] hash;
        public final long size;

        private FileAttributeWrapper(@NotNull String filenameExtension, @NotNull byte[] hash, @NotNull long size) {
            this.filenameExtension = filenameExtension;
            this.hash = hash;
            this.size = size;
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hash(filenameExtension, size) + Arrays.hashCode(hash);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FileAttributeWrapper)) // implicit null check
                return false;

            FileAttributeWrapper that = (FileAttributeWrapper) o;

            // A file is only considered equal to another if it has the same size, filename extension and hash
            // (paranoid about possible data loss due to conflicting checksum).
            // Almost impossible, but still...
            return (this.size == that.size)
                    && this.filenameExtension.equalsIgnoreCase(that.filenameExtension)
                    && Arrays.equals(this.hash, that.hash);
        }
    }
}
