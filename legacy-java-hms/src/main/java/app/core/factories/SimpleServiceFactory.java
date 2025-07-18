package app.core.factories;

import app.core.Context;
import app.core.Service;
import app.core.annotations.ServiceDescriptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleServiceFactory implements ServiceFactory {
    private final boolean useCache;
    private final Map<Class, Object> cache;

    public SimpleServiceFactory(boolean useCache) {
        this.useCache = useCache;
        if (useCache) {
            cache = Collections.synchronizedMap(new LinkedHashMap<>());
        } else {
            cache = null;
        }
    }

    @Override
    public <S extends Service> S getInstance(Class<S> type, ServiceDescriptor meta, Context context) throws Exception {
        S instance;
        if (!Service.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type + " must implement " + Service.class);
        }
        if (useCache) {
            synchronized (cache) {
                if ((instance = type.cast(cache.get(type))) == null) {
                    Class<? extends Context> clazz = context.getClass();
                    cache.put(type, instance = type.getConstructor(clazz).newInstance(context));
                }
            }
        } else {
            instance = type.getConstructor().newInstance();
        }
        return instance;
    }
}
