package main;

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.function.Consumer;

public class ArgParser {
    private final HashMap<Key, Object> arguments = new HashMap<>();
    private Consumer<String> onError;
    private Consumer<String> onWarning;

    public enum BivalentKey implements Key {
        LIST_DUPLICATES,
        LIST_EMPTY_DIRS,
        LIST_EMPTY_FILES,
        SKIP_EMPTY_FILES,

        INTERACTIVE_CONSOLE,
    }

    public enum ValueKey implements Key {
        HASH_ALGORITHM,
        MAX_FILE_SIZE,
        MIN_FILE_SIZE,

        DIR_FILE,
    }

    private interface Key {}

    public ArgParser() {
        this.onError = System.err::println;
        this.onWarning = System.err::println;
    }

    public Object getValue(@NotNull Key key) {
        return arguments.get(key);
    }

    public boolean isSet(@NotNull BivalentKey key) {
        Boolean b = (Boolean) arguments.get(key);
        return (b != null) && b;
    }

    public ArgParser onError(Consumer<String> onError) {
        this.onError = onError;
        return this;
    }

    public ArgParser onWarning(Consumer<String> onWarning) {
        this.onWarning = onWarning;
        return this;
    }

    public ArgParser parseArgs(String[] args) {
        try {
            parseInternally(args);
        } catch (ArrayIndexOutOfBoundsException e) {
            onError.accept("Missing argument!");
        }
        return this;
    }

    public ArgParser setDefaultIfAbsent(Key key, Object object) {
        arguments.putIfAbsent(key, object);
        return this;
    }

    private void parseInternally(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                    onError.accept("The source code is the only help you will ever get out of this program :)");
                    break;
                case "--directory-file":
                case "-f":
                    registerArgument(ValueKey.DIR_FILE, new File(args[++i]));
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
                case "--skip-empty-files":
                case "-se":
                    registerArgument(BivalentKey.SKIP_EMPTY_FILES, true);
                    break;
                default:
                    onWarning.accept("Unknown token: \"" + args[i] + "\"");
                    break;
            }
        }
    }

    private Long parseLongSuffix(String input) {
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
                onError.accept("Unknown binary suffix \"" + suffix + "\"");
                return null;
        }
    }

    private void registerArgument(Key key, Object value) {
        if (arguments.put(key, value) != null) {
            onWarning.accept("This key (" + key
                    + ") is already associated with a value. The old value will be overwritten.");
        }
    }
}
