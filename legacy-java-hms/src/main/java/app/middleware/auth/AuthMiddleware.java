package app.middleware.auth;

import app.core.annotations.Middleware;
import app.util.SessionUtil;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

/**
 * Authentication middleware
 */
@Middleware(path = "/Hms/*", method = HttpMethod.before)
public class AuthMiddleware implements Filter {
    @Override
    public void handle(Request request, Response response) {
        if (!SessionUtil.isUserAuthenticated(request)) {
            response.redirect("/Auth");
        }
    }
}
