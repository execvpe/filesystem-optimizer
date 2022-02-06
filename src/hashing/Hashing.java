package hashing;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashing {
    private MessageDigest digest;

    public Hashing(String algorithm) {
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public byte[] getFileChecksum(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[1024];

        for (int byteCount = 0; byteCount != -1; byteCount = inputStream.read(bytes)) {
            digest.update(bytes, 0, byteCount);
        }

        inputStream.close();

        return digest.digest();

    }

    public String toHexString(byte[] checksum) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : checksum) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
