package app.services.medical;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.medical.Procedure;

import java.util.List;

@ServiceDescriptor
public class ProcedureService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public ProcedureService(Configuration configuration) {
        super(configuration);
    }

    public void addProcedure(Procedure procedure) {
        String sql = "insert into procedures (name, cost, deleted, created_at, updated_at, procedure_type) " +
                "values (:name, :cost, :del, :cat, :uat, :type)";
        procedure.setId(executeUpdate(connection -> connection.createQuery(sql)
                .addParameter("name", procedure.getName())
                .addParameter("cost", procedure.getCost())
                .addParameter("del", procedure.isDeleted())
                .addParameter("cat", procedure.getCreatedAt())
                .addParameter("uat", procedure.getUpdatedAt())
                .addParameter("type", procedure.getProcedureType())
                .executeUpdate().getKey(Long.class)));
    }

    public void updateProcedure(Procedure procedure) {
        String sql = "update procedures set name = :name, cost = :cost, deleted = :deleted, updated_at = :updated_at" +
                " where procedure_type = :type and id = :id";
        executeUpdate(connection -> connection.createQuery(sql)
                .addParameter("id", procedure.getId())
                .addParameter("name", procedure.getName())
                .addParameter("cost", procedure.getCost())
                .addParameter("deleted", procedure.isDeleted())
                .addParameter("updated_at", procedure.getUpdatedAt())
                .addParameter("type", procedure.getProcedureType())
                .executeUpdate()
        );
    }

    public List<Procedure> getProceduresByType(Procedure.ProcedureType type) {
        return executeSelect(connection -> connection
                .createQuery("select * from procedures_v where procedure_type = :type")
                .addParameter("type", type)
                .executeAndFetch(Procedure.class));
    }

    public Procedure getProcedureById(long id, Procedure.ProcedureType type) {
        return executeSelect(connection -> connection
                .createQuery("select * from procedures_v where id = :id and procedure_type = :type")
                .addParameter("id", id)
                .addParameter("type", type)
                .executeAndFetchFirst(Procedure.class));
    }
}
