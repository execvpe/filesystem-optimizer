package hashing;

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileHashManager {

    private static final Hashing HASHING = new Hashing("SHA-256");
    private static final long MAX_FILE_SIZE = 10 * 1_048_576L; // n MiB
    private final HashSet<FileWrapper> wrapperIndex = new HashSet<>(1028);

    public void registerAll(File folder) throws IOException {
        if (!folder.exists()) {
            System.err.println("\"" + folder.getCanonicalPath() + "\" does not exist!");
            return;
        }
        if (!folder.isDirectory()) {
            System.err.println("\"" + folder.getCanonicalPath() + "\" is not a directory!");
            return;
        }

        traverseFolder(folder);
    }

    private void readFile(File file) throws IOException {
        String fileName = file.getName();

        long fileSize = file.length();
        if (fileSize > MAX_FILE_SIZE) {
            System.err.println("Skipped file \"" + fileName + "\": too big (" + (fileSize / 1_048_576L) + " MiB)");
            return;
        }

        int lastDot = fileName.lastIndexOf(".");
        String fileExtension = (lastDot == -1) ? "\\*/" : fileName.substring(lastDot + 1);

        FileWrapper newWrapper = new FileWrapper(fileExtension, HASHING.getFileChecksum(file), fileSize);
        if (!wrapperIndex.add(newWrapper)) {
            System.out.println(file.getCanonicalPath());
        }
    }

    private void traverseFolder(File folder) throws IOException {
        System.err.println("Traversing... \"" + folder.getCanonicalPath() + "\"");
        File[] filesInFolder = folder.listFiles();

        if (filesInFolder == null)
            return;

        int analyzedEntries = 0;
        for (File actual : filesInFolder) {
            if (actual.isFile()) {
                analyzedEntries++;
                readFile(actual);
                continue;
            }
            if (actual.isDirectory()) {
                analyzedEntries++;
                traverseFolder(actual);
            }
        }

        System.err.println("Analyzed " + analyzedEntries + "/" + filesInFolder.length
                + " entries in \"" + folder.getCanonicalPath() + "\"");
    }

    private static class FileWrapper {
        public final String extension;
        public final byte[] hash;
        public final long size;

        private FileWrapper(@NotNull String extension, @NotNull byte[] hash, @NotNull long size) {
            this.extension = extension.toLowerCase(Locale.ROOT);
            this.hash = hash;
            this.size = size;
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(extension, size);
            result = 31 * result + Arrays.hashCode(hash);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            FileWrapper that = (FileWrapper) o;

            return (size == that.size)
                    && extension.equalsIgnoreCase(that.extension)
                    && Arrays.equals(hash, that.hash);
        }
    }
}
