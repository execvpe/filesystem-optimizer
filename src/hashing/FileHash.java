package hashing;

import main.Main;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FileHash {
	private MessageDigest digest;

	public FileHash(String algorithm) {
		try {
			digest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			Main.die("Algorithm \"" + algorithm + "\" is invalid or not available in this environment!");
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

	public static byte[] fromHexString(String hexString) {
		byte[] bytes = new byte[hexString.length() / 2];

		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hexString.substring(i * 2, (i * 2) + 2), 16);
		}

		return bytes;
	}

	public static String toHexString(byte[] checksum) {
		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : checksum) {
			stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
		}

		return stringBuilder.toString();
	}
}
