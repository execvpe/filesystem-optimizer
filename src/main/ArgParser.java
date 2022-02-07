package main;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArgParser {
    private final HashMap<ArgOption, Object> arguments = new HashMap<>();
    private final AtomicBoolean immutable = new AtomicBoolean(false);

    public enum ArgOption {
        HASH_ALGORITHM,
        LIST_DUPLICATES,
        LIST_EMPTY_DIRS,
        LIST_EMPTY_FILES,
        MAX_FILE_SIZE,
        MIN_FILE_SIZE,

        DIR_FILE,
    }

    public ArgParser(String[] args) {
        try {
            parse(args);
        } catch (ArrayIndexOutOfBoundsException e) {
            Main.die("Missing argument!");
        }
    }

    public Object getArgument(ArgOption argOption) {
        return arguments.get(argOption);
    }

    public void makeImmutable() {
        immutable.set(true);
    }

    public void setDefaultIfAbsent(ArgOption argOption, Object object) {
        if (immutable.get()) {
            throw new UnsupportedOperationException("Immutable flag is set!");
        }
        arguments.putIfAbsent(argOption, object);
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                    Main.die("The source code is the only help you will ever get out of this program :)");
                    break;
                case "--hash-algorithm":
                case "-al":
                    registerArgument(ArgOption.HASH_ALGORITHM, args[++i]);
                    break;
                case "--list-duplicates":
                case "-dup":
                    registerArgument(ArgOption.LIST_DUPLICATES, Boolean.TRUE);
                    break;
                case "--list-empty-dirs":
                case "-ed":
                    registerArgument(ArgOption.LIST_EMPTY_DIRS, Boolean.TRUE);
                    break;
                case "--list-empty-files":
                case "-ef":
                    registerArgument(ArgOption.LIST_EMPTY_FILES, Boolean.TRUE);
                    break;
                case "--max-file-size":
                case "-max":
                    registerArgument(ArgOption.MAX_FILE_SIZE, parseLongSuffix(args[++i]));
                    break;
                case "--min-file-size":
                case "-min":
                    registerArgument(ArgOption.MIN_FILE_SIZE, parseLongSuffix(args[++i]));
                    break;

                default:
                    if (args[i].startsWith("-")) {
                        Main.die("Unknown option: \"" + args[i] + "\"");
                    }
                    registerArgument(ArgOption.DIR_FILE, new File(args[i]));
            }
        }
    }

    private void registerArgument(ArgOption option, Object value) {
        if (arguments.put(option, value) != null) {
            Main.die("Option \"" + option + "\" specified twice!");
        }
    }

    private static long parseLongSuffix(String input) {
        int suffixPos = -1;
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) {
                suffixPos = i;
                break;
            }
        }
        if (suffixPos == -1) {
            return Long.parseLong(input);
        }

        long digits = Long.parseLong(input.substring(0, suffixPos));
        String suffix = input.substring(suffixPos);

        switch (suffix) {
            case "k":
            case "Ki":
                return digits * 1_024L;
            case "M":
            case "Mi":
                return digits * 1_048_576L;
            case "G":
            case "Gi":
                return digits * 1_073_741_824L;
            case "T":
            case "Ti":
                return digits * 1_099_511_627_766L;
            default:
                Main.die("Unknown binary suffix \"" + suffix + "\"");
                return -1;
        }
    }
}
