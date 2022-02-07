package main;

import filesystem.FileManager;

import java.io.File;
import java.util.Scanner;

public class Main {
    public static ArgParser argParser;

    public static void die(String message) {
        System.err.println("Fatal: " + message);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        argParser = new ArgParser(args);
        argParser.setDefaultIfAbsent(ArgParser.ArgOption.HASH_ALGORITHM, "SHA-256");
        argParser.makeImmutable();

        Scanner stdinScanner = new Scanner(System.in);
        FileManager manager = new FileManager();

        File dirFile = (File) argParser.getArgument(ArgParser.ArgOption.DIR_FILE);
        if (dirFile != null) {
            Scanner dirFileScanner = new Scanner(dirFile);

            while (dirFileScanner.hasNextLine()) {
                manager.crawlFilesystem(dirFileScanner.nextLine());
            }
            dirFileScanner.close();
        }

        while (stdinScanner.hasNextLine()) {
            String line = stdinScanner.nextLine();
            if (line.equals("$ kill")) {
                break;
            }
            manager.crawlFilesystem(line);
        }
        stdinScanner.close();
    }
}
