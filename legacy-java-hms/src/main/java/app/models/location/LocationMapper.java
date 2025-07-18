package app.models.location;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public abstract class LocationMapper<T extends Location> extends ReflectionBeanMapper<T> {
    public LocationMapper(Class<T> type) {
        super(type);
    }
}
