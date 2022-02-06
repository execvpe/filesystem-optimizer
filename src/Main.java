import hashing.FileHashManager;

import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        FileHashManager manager = new FileHashManager();

        for (String dir : args) {
            manager.registerAll(new File(dir));
        }

        while (scanner.hasNextLine()) {
            manager.registerAll(new File(scanner.nextLine()));
        }

        System.err.println("Received EOF!");
    }

}
