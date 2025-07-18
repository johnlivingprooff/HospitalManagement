package app.services.theater;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.billing.bills.Bill;
import app.models.theater.MedicalSurgery;

import java.util.List;

@ServiceDescriptor
public class TheaterService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public TheaterService(Configuration configuration) {
        super(configuration);
    }

    public List<MedicalSurgery> getSurgeryByPerformer(long performer) {
        return executeSelect(connection -> connection
                .createQuery("select * from surgeries_v where performed_by = :performer")
                .addParameter("performer", performer)
                .executeAndFetch(MedicalSurgery.class));
    }

    public List<MedicalSurgery> getPatientSurgeries(long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from surgeries_v where patient_id = :id")
                .addParameter("id", patientId)
                .executeAndFetch(MedicalSurgery.class));
    }

    public void addMedicalSurgery(MedicalSurgery surgery) {
        surgery.setId(executeUpdate(connection -> connection
                .createQuery("select * from addMedicalSurgery(:n, :e, :pid, :proc, :att, :date, :type, :status)")
                .addParameter("n", surgery.getNotes())
                .addParameter("e", surgery.getPerformedBy())
                .addParameter("pid", surgery.getPatientId())
                .addParameter("proc", surgery.getProcedureId())
                .addParameter("att", surgery.getAttachment())
                .addParameter("date", surgery.getCreatedAt())
                .addParameter("type", Bill.BillType.Theater)
                .addParameter("status", Bill.BillStatus.UnPaid)
                .executeAndFetchFirst(Long.class)));
    }

    public MedicalSurgery getSurgeryById(long id, long performer) {
        return executeSelect(connection -> connection
                .createQuery("select * from surgeries_v where id = :id and performed_by = :performer")
                .addParameter("id", id)
                .addParameter("performer", performer)
                .executeAndFetchFirst(MedicalSurgery.class));
    }
}
