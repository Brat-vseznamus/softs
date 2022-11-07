package lab3.html;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class HTMLGenerator {
    private HTMLGenerator() {}

    public static String cover(String element, String inner) {
        return "<" + element + ">" + inner + "</" + element + ">";
    }

    public static String br(String line) {
        return line + "<br>";
    }

    public static String lines(String... lines) {
        return String.join("\n", lines);
    }

    public static String host(String inner) {
        return cover("host", inner);
    }

    public static String bosy(String inner) {
        return cover("body", inner);
    }
}
