package util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {
    /**
     * The system line separator returned by {@link System#lineSeparator()}.
     */
    public static final String LINE_SEPARATOR = System.lineSeparator();
    /**
     * This regular expression matches a system independent Unicode linebreak sequence. e.g. "\n" (Unix), "\r\n" (Windows), "\r"
     * (Macintosh)
     */
    public static final String LINE_SEPARATOR_REGEX = "\\R";

    /**
     * Dummy constructor: This is a utility class which should not be instantiated.
     */
    private StringUtil() {
    }

    /**
     * Calculate the number of occurrences of a particular character in the given String
     *
     * @param str a String
     * @param chr a char
     * @return the count
     */
    public static long countChar(String str, char chr) {
        return str.chars().filter(c -> c == chr).count();
    }

    /**
     * Checks if the given String ends with a newline indicator respectively '\r' or '\n' (obviously including "\r\n").
     *
     * @param s the {@link String} which should be checked
     * @return {@code true} if the String ends with '\r' or '\n'; {@code false} if not
     */
    public static boolean endsWithLineSeparator(String s) {
        return s.endsWith("\r") || s.endsWith("\n");
    }

    public static String[] tokenize(final String line) throws IllegalArgumentException {
        if (countChar(line, '\'') % 2 != 0)
            throw new IllegalArgumentException("The string \"" + line + "\" contains an odd number of single-quotes!");
        if (countChar(line, '"') % 2 != 0)
            throw new IllegalArgumentException("The string \"" + line + "\" contains an odd number of double-quotes!");

        final ArrayList<String> list = new ArrayList<>();
        final Matcher matcher = Pattern.compile("\"([^\"]*)\"|'([^']*)'|(\\S+)").matcher(line);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // "Quoted" argument
                list.add(matcher.group(1));
                continue;
            }
            if (matcher.group(2) != null) {
                // 'Quoted' argument
                list.add(matcher.group(2));
                continue;
            }
            // Plain argument without whitespace
            list.add(matcher.group(3));
        }

        return list.toArray(new String[0]);
    }

    public static String[] tokenizeDoubleQuotesOnly(final String line) throws IllegalArgumentException {
        if (countChar(line, '"') % 2 != 0)
            throw new IllegalArgumentException("The string \"" + line + "\" contains an odd number of double-quotes!");

        final ArrayList<String> list = new ArrayList<>();
        final Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(line);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // "Quoted" argument
                list.add(matcher.group(1));
                continue;
            }
            // Plain argument without whitespace
            list.add(matcher.group(2));
        }

        return list.toArray(new String[0]);
    }


}
