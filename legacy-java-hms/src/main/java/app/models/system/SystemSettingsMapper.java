package app.models.system;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public final class SystemSettingsMapper extends ReflectionBeanMapper<SystemSettings> {

    public SystemSettingsMapper() {
        super(SystemSettings.class);
    }
}
