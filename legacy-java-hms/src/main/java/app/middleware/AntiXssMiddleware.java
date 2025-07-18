package app.middleware;

import app.core.annotations.Middleware;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.util.concurrent.atomic.AtomicBoolean;

@Middleware(path = "*", method = HttpMethod.before)
public class AntiXssMiddleware implements Filter {
    private static final AtomicBoolean csp = new AtomicBoolean();

    @Override
    public void handle(Request request, Response response) throws Exception {
        response.header("X-Frame-Options", "DENY");
        response.header("X-XSS-Protection", "1; mode=block");
        if (csp.get()) {
            // WARNING: This header causes image downloads to break
            response.header("Content-Security-Policy", "default-src 'self'");
        }
    }

    public static void enableContentSecurityPolicy(boolean value) {
        csp.set(value);
    }
}
