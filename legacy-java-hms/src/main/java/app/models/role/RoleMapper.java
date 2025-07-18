package app.models.role;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public class RoleMapper extends ReflectionBeanMapper<Role> {
    public RoleMapper() {
        super(Role.class);
    }
}
