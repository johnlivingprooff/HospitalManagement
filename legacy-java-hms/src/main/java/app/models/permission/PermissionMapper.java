package app.models.permission;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public class PermissionMapper extends ReflectionBeanMapper<Permission> {

    public PermissionMapper() {
        super(Permission.class);
    }
}
