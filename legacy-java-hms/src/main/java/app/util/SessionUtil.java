package app.util;

import spark.Request;
import spark.Session;

public interface SessionUtil {
    static boolean isUserAuthenticated(Request request) {
        final Session session = request.session(false);
        return session != null && session.attribute("account") != null;
    }
}
