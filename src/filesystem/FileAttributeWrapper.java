package filesystem;

import com.sun.istack.internal.NotNull;

import java.util.Arrays;
import java.util.Objects;

final class FileAttributeWrapper {
    public static final String NO_FILENAME_EXTENSION = "\\:*/";

    public final String filenameExtension;
    public final byte[] hash;
    public final long size;

    FileAttributeWrapper(@NotNull String filenameExtension, @NotNull byte[] hash, @NotNull long size) {
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