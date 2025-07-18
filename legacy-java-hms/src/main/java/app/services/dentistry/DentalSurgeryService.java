package app.services.dentistry;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.billing.bills.Bill;
import app.models.dentistry.DentalSurgery;

import java.util.List;

@ServiceDescriptor
public class DentalSurgeryService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public DentalSurgeryService(Configuration configuration) {
        super(configuration);
    }

    public List<DentalSurgery> getSurgeryByPerformer(long performer) {
        return executeSelect(connection -> connection
                .createQuery("select * from dental_surgeries_v where performed_by = :performer")
                .addParameter("performer", performer)
                .executeAndFetch(DentalSurgery.class));
    }

    public List<DentalSurgery> getPatientDentalSurgeries(long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from dental_surgeries_v where patient_id = :id order by created_at desc")
                .addParameter("id", patientId)
                .executeAndFetch(DentalSurgery.class));
    }

    public void addDentalSurgery(DentalSurgery surgery) {
        surgery.setId(executeUpdate(connection -> connection
                .createQuery("select * from addDentalSurgery(:n, :e, :pid, :proc, :att, :date, :type, :status)")
                .addParameter("n", surgery.getNotes())
                .addParameter("e", surgery.getPerformedBy())
                .addParameter("pid", surgery.getPatientId())
                .addParameter("proc", surgery.getProcedureId())
                .addParameter("att", surgery.getAttachment())
                .addParameter("date", surgery.getCreatedAt())
                .addParameter("type", Bill.BillType.Dental)
                .addParameter("status", Bill.BillStatus.UnPaid)
                .executeAndFetchFirst(Long.class)));
    }

    public DentalSurgery getSurgeryById(long id, long performer) {
        return executeSelect(connection -> connection
                .createQuery("select * from dental_surgeries_v where id = :id and performed_by = :performer")
                .addParameter("id", id)
                .addParameter("performer", performer)
                .executeAndFetchFirst(DentalSurgery.class));
    }
}
