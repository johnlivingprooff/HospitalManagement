package app.daos.audit;

import app.models.audit.AuditLog;
import app.models.audit.AuditLogMapper;
import app.services.audit.AuditService;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Date;
import java.util.List;

@RegisterMapper(AuditLogMapper.class)
public interface AuditDao {

    @SqlUpdate("INSERT INTO audit_log(LogType, Action, Created, Location, Address) VALUES (:type, :action, :created, :location, :address)")
    void createLog(
            @Bind("type") AuditService.LogType type,
            @Bind("action") String action,
            @Bind("created") Date created,
            @Bind("location") String location,
            @Bind("address") String address
    );

    @SqlQuery("SELECT * FROM audit_log WHERE archived = :archived order by id desc")
    List<AuditLog> get(@Bind("archived") boolean archived);
}
