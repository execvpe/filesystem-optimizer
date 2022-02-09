package main;

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArgParser {
    private final HashMap<Key, Object> arguments = new HashMap<>();
    private final AtomicBoolean immutable = new AtomicBoolean(false);

    public enum BivalentKey implements Key {
        LIST_DUPLICATES,
        LIST_EMPTY_DIRS,
        LIST_EMPTY_FILES,

        INTERACTIVE_CONSOLE,
    }

    public enum ValueKey implements Key {
        HASH_ALGORITHM,
        MAX_FILE_SIZE,
        MIN_FILE_SIZE,

        DIR_FILE,
    }

    private interface Key {}

    public ArgParser(String[] args) {
        try {
            parse(args);
        } catch (ArrayIndexOutOfBoundsException e) {
            Main.die("Missing argument!");
        }
    }

    public Object getValue(@NotNull Key key) {
        return arguments.get(key);
    }

    public boolean isSet(@NotNull BivalentKey key) {
        Boolean b = (Boolean) arguments.get(key);
        return (b != null) && b;
    }

    public ArgParser makeImmutable() {
        immutable.set(true);
        return this;
    }

    public ArgParser setDefaultIfAbsent(Key key, Object object) {
        if (immutable.get()) {
            throw new UnsupportedOperationException("Immutable flag is set!");
        }
        arguments.putIfAbsent(key, object);
        return this;
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                    Main.die("The source code is the only help you will ever get out of this program :)");
                    break;
                case "--hash-algorithm":
                case "-al":
                    registerArgument(ValueKey.HASH_ALGORITHM, args[++i]);
                    break;
                case "--list-duplicates":
                case "-dup":
                    registerArgument(BivalentKey.LIST_DUPLICATES, true);
                    break;
                case "--list-empty-dirs":
                case "-ed":
                    registerArgument(BivalentKey.LIST_EMPTY_DIRS, true);
                    break;
                case "--list-empty-files":
                case "-ef":
                    registerArgument(BivalentKey.LIST_EMPTY_FILES, true);
                    break;
                case "--max-file-size":
                case "-max":
                    registerArgument(ValueKey.MAX_FILE_SIZE, parseLongSuffix(args[++i]));
                    break;
                case "--min-file-size":
                case "-min":
                    registerArgument(ValueKey.MIN_FILE_SIZE, parseLongSuffix(args[++i]));
                    break;
                case "--no-interactive":
                case "-N":
                    registerArgument(BivalentKey.INTERACTIVE_CONSOLE, false);
                    break;

                default:
                    if (args[i].startsWith("-")) {
                        Main.die("Unknown option: \"" + args[i] + "\"");
                    }
                    registerArgument(ValueKey.DIR_FILE, new File(args[i]));
            }
        }
    }

    private void registerArgument(Key key, Object value) {
        if (arguments.put(key, value) != null) {
            Main.die("Key \"" + key + "\" is already associated with a value!");
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
