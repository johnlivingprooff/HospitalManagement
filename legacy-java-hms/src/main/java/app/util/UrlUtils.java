package app.util;

public interface UrlUtils {
    static String make(String base, String... paths) {
        String path;
        StringBuilder sb;

        sb = new StringBuilder();

        if (paths == null || paths.length == 0) {
            return base;
        }

        for (String p : paths) {
            sb.append('/').append(p);
        }

        path = sb.toString().replaceAll("//", "/");

        if (base.endsWith("/") && path.startsWith("/")) {
            return base + path.substring(1);
        } else if ((!base.endsWith("/") && path.startsWith("/")) || (base.endsWith("/") && !path.startsWith("/"))) {
            return base + path;
        } else /*if (!base.endsWith("/") && !path.startsWith("/"))*/ {
            return base + "/" + path;
        }
    }


}
