package main;

import filesystem.FileManager;
import util.StringUtil;

import java.io.File;
import java.util.Scanner;

public class Main {
    public static ArgParser argParser;

    public static void die(String message) {
        System.err.println("Fatal: " + message);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        argParser = new ArgParser(args)
                .setDefaultIfAbsent(ArgParser.ValueKey.HASH_ALGORITHM, "SHA-256")
                .setDefaultIfAbsent(ArgParser.BivalentKey.INTERACTIVE_CONSOLE, true)
                .makeImmutable();

        FileManager manager = new FileManager();

        File dirFile = (File) argParser.getValue(ArgParser.ValueKey.DIR_FILE);
        if (dirFile != null) {
            Scanner dirFileScanner = new Scanner(dirFile);

            while (dirFileScanner.hasNextLine()) {
                manager.crawlFilesystem(dirFileScanner.nextLine());
            }
            dirFileScanner.close();
        }

        if (!argParser.isSet(ArgParser.BivalentKey.INTERACTIVE_CONSOLE)) {
            return; // exit
        }

        Scanner stdinScanner = new Scanner(System.in);
        while (true) {
            System.err.printf("(%d) $> ", manager.elements());

            if (!stdinScanner.hasNextLine()) {
                break;
            }

            String[] tokens = StringUtil.tokenize(stdinScanner.nextLine());
            switch (tokens[0]) {
                case "exit":
                case "e":
                    stdinScanner.close();
                    System.err.println("Bye");
                    return; // exit
                case "crawl":
                case "c":
                    if (tokens.length == 1) {
                        System.err.println("\"crawl\" expects at least one argument!");
                        break;
                    }
                    for (int i = 1; i < tokens.length; i++) {
                        manager.crawlFilesystem(tokens[i]);
                    }
                    break;
            }
        }
        stdinScanner.close();
    }
}
