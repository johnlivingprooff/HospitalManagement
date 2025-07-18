package app.daos.system;

import app.models.system.SystemSettings;
import app.models.system.SystemSettingsMapper;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SystemSettingsMapper.class)
public interface SystemDao {
    @SqlQuery("SELECT * FROM system_settings LIMIT 1")
    SystemSettings getSystemSettings();

    @SqlUpdate("update system_settings set logo = :logo, banner = :banner, modified = current_date, modifiedBy = :modifiedBy where id = :id")
    void updateSystemSettings(@BindBean SystemSettings settings);
}
