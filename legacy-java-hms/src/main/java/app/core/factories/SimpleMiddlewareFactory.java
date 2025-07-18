package app.core.factories;

import app.core.Context;
import app.core.annotations.Middleware;
import spark.Filter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleMiddlewareFactory implements MiddlewareFactory {
    private final boolean useCache;
    private final Map<Class, Object> cache;

    public SimpleMiddlewareFactory(boolean useCache) {
        this.useCache = useCache;
        if (useCache) {
            cache = Collections.synchronizedMap(new LinkedHashMap<>());
        } else {
            cache = null;
        }
    }

    @Override
    public <F extends Filter> F getInstance(Class<F> type, Middleware meta, Context context) throws Exception {
        F instance;
        if (!Filter.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type + " must implement " + Filter.class);
        }
        if (useCache) {
            synchronized (cache) {
                if ((instance = type.cast(cache.get(type))) == null) {
                    cache.put(type, instance = type.getConstructor().newInstance());
                }
            }
        } else {
            instance = type.getConstructor().newInstance();
        }
        return instance;
    }
}
