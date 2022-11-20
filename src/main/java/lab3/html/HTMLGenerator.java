package lab3.html;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class HTMLGenerator {
    private HTMLGenerator() {}

    public static String cover(String element, String inner) {
        return "<" + element + ">" + inner + "</" + element + ">";
    }

    public static String br(String line) {
        return line + "</br>";
    }

    public static String lines(String... lines) {
        return String.join("", lines);
    }

    public static String html(String inner) {
        return cover("html", inner);
    }

    public static String body(String inner) {
        return cover("body", inner);
    }

    public static void writeHTML(HttpServletResponse httpServletResponse, String htmlString) throws IOException {
        httpServletResponse.getWriter().println(htmlString);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.setContentType("text/html");
    }
}
