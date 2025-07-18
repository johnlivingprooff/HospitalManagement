package app.core.factories;

import app.core.Context;
import app.core.annotations.Middleware;
import spark.Filter;

public interface MiddlewareFactory {
    <F extends Filter> F getInstance(Class<F> type, Middleware meta, Context context) throws Exception;
}
