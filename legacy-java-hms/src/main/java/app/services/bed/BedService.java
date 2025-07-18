package app.services.bed;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.ListOption;
import app.models.bed.Bed;

import java.util.List;

@ServiceDescriptor
public final class BedService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public BedService(Configuration configuration) {
        super(configuration);
    }

    public List<Bed> getVacantBeds() {
        return getBeds(true);
    }

    public List<Bed> getOccupiedBeds() {
        return getBeds(false);
    }

    public List<Bed> getAllBeds() {
        return executeSelect(connection -> connection
                .createQuery("select * from beds_v")
                .executeAndFetch(Bed.class));
    }

    private List<Bed> getBeds(boolean vacant) {
        return executeSelect(connection -> {
            String sql;
            sql = "select * from beds_v where vacant = :vacant";
            return connection.createQuery(sql)
                    .addParameter("vacant", vacant)
                    .executeAndFetch(Bed.class);
        });
    }

    public Bed getBedById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from beds_v where id = :id")
                .addParameter("id", id)
                .executeAndFetchFirst(Bed.class));
    }

    public void addBed(Bed bed) {
        bed.setVacant(true);
        bed.setId(
                executeUpdate(connection -> {
                    String sql;
                    sql = "insert into beds (code, ward_id, vacant) values (:code, :ward_id, :vacant)";
                    return connection.createQuery(sql)
                            .addParameter("code", bed.getCode())
                            .addParameter("ward_id", bed.getWardId())
                            .addParameter("vacant", bed.isVacant())
                            .executeUpdate()
                            .getKey(Long.class);
                })
        );
    }

    public boolean isBedCodeInUse(String code) {
        return executeSelect(connection -> connection
                .createQuery("select exists(select 1 from beds where lower(code) = :code limit 1)")
                .addParameter("code", code.toLowerCase())
                .executeAndFetchFirst(Boolean.class));
    }

    public void updateBed(Bed bed) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("update beds set code = :code, vacant = :vacant, ward_id = :ward_id where id = :id")
                    .addParameter("id", bed.getId())
                    .addParameter("code", bed.getCode())
                    .addParameter("vacant", bed.isVacant())
                    .addParameter("ward_id", bed.getWardId())
                    .executeUpdate();
            return null;
        });
    }

    public void deleteBed(Bed bed) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("select from deleteBed(:id)")
                    .addParameter("id", bed.getId())
                    .executeAndFetchFirst(Long.class);
            return null;
        });
    }

    public List<ListOption> getVacantBedsByWardId(long wardId) {
        return executeSelect(connection -> connection
                .createQuery("select id, code as label from beds_v where vacant and ward_id = :id")
                .addParameter("id", wardId)
                .executeAndFetch(ListOption.class));
    }
}
