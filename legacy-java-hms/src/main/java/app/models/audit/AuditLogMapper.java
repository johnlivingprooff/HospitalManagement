package app.models.audit;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public final class AuditLogMapper extends ReflectionBeanMapper<AuditLog> {
    public AuditLogMapper() {
        super(AuditLog.class);
    }
}
