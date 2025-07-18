package app.core.factories;

import app.core.Context;
import app.core.Controller;
import app.core.annotations.RouteController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleControllerFactory implements ControllerFactory {
    private final boolean useCache;
    private final Map<Class, Object> cache;

    public SimpleControllerFactory(boolean useCache) {
        this.useCache = useCache;
        if (useCache) {
            cache = Collections.synchronizedMap(new LinkedHashMap<>());
        } else {
            cache = null;
        }
    }

    @Override
    public <C extends Controller> C getInstance(Class<C> type, RouteController meta, Context context) throws Exception {
        C instance;

        if (!Controller.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type + " must extend " + Controller.class);
        }

        if (useCache) {
            if ((instance = getInstance(type)) == null) {
                cacheInstance(type, instance = type.getConstructor().newInstance());
            }
        } else {
            instance = type.getConstructor().newInstance();
        }
        return instance;
    }

    protected final <C extends Controller> void cacheInstance(Class<C> type, C instance) {
        synchronized (cache) {
            cache.put(type, instance);
        }
    }

    protected final <C extends Controller> C getInstance(Class<C> type) {
        synchronized (cache) {
            return type.cast(cache.get(type));
        }
    }
}
