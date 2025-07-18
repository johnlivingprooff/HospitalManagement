package app.services.ward;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.ward.Ward;

import java.util.List;

@ServiceDescriptor
public final class WardService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public WardService(Configuration configuration) {
        super(configuration);
    }

    public List<Ward> getWards() {
        return getWards(false);
    }

    public List<Ward> getActiveWards() {
        return getWards(true);
    }

    private List<Ward> getWards(boolean activeOnly) {
        if (activeOnly) {
            return executeSelect(connection -> connection.createQuery("select * from wards where active = true")
                    .executeAndFetch(Ward.class));
        } else {
            return executeSelect(connection -> connection.createQuery("select * from wards")
                    .executeAndFetch(Ward.class));
        }
    }

    public void addWard(Ward ward) {
        ward.setId(this.<Long>executeUpdate(connection -> {
            final String sql;

            sql = "insert into wards (code, name, active) values (:code, :name, :active)";
            return connection.createQuery(sql)
                    .addParameter("code", ward.getCode())
                    .addParameter("name", ward.getName())
                    .addParameter("active", ward.isActive())
                    .executeUpdate()
                    .getKey(Long.class);
        }));
    }

    public void updateWard(Ward ward) {
        this.<Void>executeUpdate(connection -> {
            final String sql;
            sql = "update wards set code = :code, name = :name, active = :active where id = :id";
            connection.createQuery(sql)
                    .addParameter("id", ward.getId())
                    .addParameter("code", ward.getCode())
                    .addParameter("name", ward.getName())
                    .addParameter("active", ward.isActive())
                    .executeUpdate()
                    .getKey(Long.class);
            return null;
        });
    }

    public boolean isWardCodeInUse(String code) {
        return executeSelect(connection -> {
            String sql;
            sql = "select exists(select 1 from wards where lower(code) = :code limit 1)";
            return connection.createQuery(sql)
                    .addParameter("code", code.toLowerCase())
                    .executeAndFetchFirst(Boolean.class);
        });
    }

    public Ward findWardById(long id) {
        return executeSelect(connection -> {
            String sql;
            sql = "select * from wards where id = :id";
            return connection.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Ward.class);
        });
    }

    public boolean activeWardExists(long wardId) {
        return executeSelect(connection -> connection
                .createQuery("select exists(select 1 from wards where id = :id and active = :active)")
                .addParameter("id", wardId)
                .addParameter("active", true)
                .executeAndFetchFirst(Boolean.class));
    }
}
