package app.services.doctor;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.billing.bills.Bill;
import app.models.dentistry.DentalSurgery;
import app.models.doctor.ConsultationResult;

import java.util.List;

@ServiceDescriptor
public class ConsultationService extends ServiceImpl {


    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public ConsultationService(Configuration configuration) {
        super(configuration);
    }

    public List<ConsultationResult> getResultsByConsultant(long consultant) {
        return executeSelect(connection -> connection
                .createQuery("select * from consultation_results_v where performed_by = :performer")
                .addParameter("performer", consultant)
                .executeAndFetch(ConsultationResult.class));
    }

    public List<ConsultationResult> getPatientConsultationResults(long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from consultation_results_v where patient_id = :id order by created_at desc")
                .addParameter("id", patientId)
                .executeAndFetch(ConsultationResult.class));
    }

    public void addDConsultationResult(ConsultationResult result) {
        result.setId(executeUpdate(connection -> connection
                .createQuery("select * from addConsultationResults(:n, :e, :pid, :proc, :att, :date, :type, :status)")
                .addParameter("n", result.getNotes())
                .addParameter("e", result.getPerformedBy())
                .addParameter("pid", result.getPatientId())
                .addParameter("proc", result.getProcedureId())
                .addParameter("att", result.getAttachment())
                .addParameter("date", result.getCreatedAt())
                .addParameter("type", Bill.BillType.Consultation)
                .addParameter("status", Bill.BillStatus.UnPaid)
                .executeAndFetchFirst(Long.class)));
    }

    public DentalSurgery getConsultationResultById(long id, long performer) {
        return executeSelect(connection -> connection
                .createQuery("select * from consultation_results_v where id = :id and performed_by = :performer")
                .addParameter("id", id)
                .addParameter("performer", performer)
                .executeAndFetchFirst(DentalSurgery.class));
    }

    public ConsultationResult getPatientConsultationById(long id, long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from consultation_results_v where id = :id and patient_id = :patientId")
                .addParameter("id", id)
                .addParameter("patientId", patientId)
                .executeAndFetchFirst(ConsultationResult.class));
    }
}
