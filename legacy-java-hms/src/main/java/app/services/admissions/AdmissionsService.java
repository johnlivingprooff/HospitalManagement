package app.services.admissions;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.admissions.Admission;
import app.models.admissions.AdmissionStatus;
import app.models.admissions.AdmissionType;
import app.models.billing.bills.Bill;
import app.models.patient.Patient;
import app.models.patient.PatientStatus;
import app.models.patient.Vitals;
import app.models.patient.VitalsType;

import java.time.LocalDateTime;
import java.util.List;

@ServiceDescriptor
public class AdmissionsService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public AdmissionsService(Configuration configuration) {
        super(configuration);
    }

    private double getAdmissionRate(AdmissionType admissionType) {
        return executeSelect(connection -> connection
                .createQuery("select rate from admission_rates where admission_type = :type")
                .addParameter("type", admissionType)
                .executeAndFetchFirst(Double.class));
    }

    public double getFullAdmissionRate() {
        return getAdmissionRate(AdmissionType.FullAdmission);
    }

    public double getShortStayAdmissionRate() {
        return getAdmissionRate(AdmissionType.ShortStay);
    }

    public void updateFullAdmissionRate(double rate) {
        updateAdmissionRate(rate, AdmissionType.FullAdmission);
    }

    public void updateShortStayAdmissionRate(double rate) {
        updateAdmissionRate(rate, AdmissionType.ShortStay);
    }

    private void updateAdmissionRate(double rate, AdmissionType type) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("update admission_rates set rate = :rate where admission_type = :type")
                    .addParameter("type", type)
                    .addParameter("rate", rate)
                    .executeUpdate();
            return null;
        });
    }

    public List<Admission> getAdmissions() {
        return executeSelect(connection -> connection.createQuery("select * from admissions_v")
                .executeAndFetch(Admission.class));
    }

    public boolean isPatientUnderAdmission(long patientId) {
        String sql = "select exists (select 1 from admissions where patient_id = :patient_id and status = :status)";
        return executeSelect(connection -> connection.createQuery(sql).
                addParameter("patient_id", patientId)
                .addParameter("status", AdmissionStatus.Active)
                .executeAndFetchFirst(Boolean.class));
    }

    public Admission findOverlappingPatientAdmission(long patientId, LocalDateTime admissionDate) {
        String sql = "select * from admissions_v " +
                "where (created_at >= :date and :date <= COALESCE(created_at, discharged_at)) and patient_id = :pid";
        return executeSelect(connection -> connection.createQuery(sql)
                .addParameter("date", admissionDate)
                .addParameter("pid", patientId)
                .executeAndFetchFirst(Admission.class));
    }

    public void addAdmission(Admission admission) {
        admission.setId(executeUpdate(connection -> {
            long id;
            id = connection.createQuery("insert into admissions (patient_id, created_at, updated_at, " +
                    "bed_id, admitted_by, admission_type, status, reason, attachment, termination_reason) " +
                    "values (:pid, :cat, :uat, :bid, :aby, :type, :status, :reason, :attachment, :t_reason)")
                    .addParameter("pid", admission.getPatientId())
                    .addParameter("cat", admission.getCreatedAt())
                    .addParameter("uat", admission.getUpdatedAt())
                    .addParameter("bid", admission.getBedId())
                    .addParameter("aby", admission.getAdmittedBy())
                    .addParameter("type", admission.getAdmissionType())
                    .addParameter("status", admission.getStatus())
                    .addParameter("reason", admission.getReason())
                    .addParameter("attachment", admission.getAttachment())
                    .addParameter("t_reason", "")
                    .executeUpdate().getKey(Long.class);

            connection.createQuery("update patient set status = :status where id = :id")
                    .addParameter("id", admission.getPatientId())
                    .addParameter("status", PatientStatus.UnderTreatment)
                    .executeUpdate();

            connection.createQuery("update beds set vacant = false where id = :id")
                    .addParameter("id", admission.getBedId())
                    .executeUpdate();

            return id;
        }));
    }

    public Admission getAdmissionById(long id) {
        return executeSelect(connection -> connection.createQuery("select * from admissions_v where id = :id")
                .addParameter("id", id)
                .executeAndFetchFirst(Admission.class));
    }

    public List<Admission> getAdmissionsByState(AdmissionStatus status) {
        return executeSelect(connection -> connection
                .createQuery("select * from admissions_v where status = :status")
                .addParameter("status", status)
                .executeAndFetch(Admission.class));
    }

    public void terminateAdmission(Admission admission) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            LocalDateTime now;

            now = LocalDateTime.now();
            connection.createQuery("select from public.terminateAdmission(:id, :reason, :attachment, :date, :status, :user_id)")
                    .addParameter("id", admission.getId())
                    .addParameter("reason", admission.getTerminationReason())
                    .addParameter("attachment", admission.getTerminationAttachment())
                    .addParameter("date", admission.getDischargedAt())
                    .addParameter("status", admission.getStatus())
                    .addParameter("user_id", admission.getTerminatedBy())
                    .executeAndFetchFirst(Long.class);
            connection.createQuery("select public.createAdmissionBill(:id, :pid, :status, :type, :cat, :uat)")
                    .addParameter("id", admission.getId())
                    .addParameter("pid", admission.getPatientId())
                    .addParameter("status", Bill.BillStatus.UnPaid)
                    .addParameter("type", Bill.BillType.Admission)
                    .addParameter("cat", now)
                    .addParameter("uat", now)
                    .executeAndFetchFirst(Long.class);
            return null;
        });
    }

    public List<Vitals> getVitals(Admission admission) {
        String sql = "select * from patient_vitals_v where type = :type and admission_id = :id order by created desc";
        return executeSelect(connection -> connection
                .createQuery(sql)
                .addParameter("id", admission.getId())
                .addParameter("type", VitalsType.Admission)
                .executeAndFetch(Vitals.class));
    }

    public void addVitals(Vitals vitals) {
        String sql = "insert into patient_vitals(patient_id, created, type, pulse, breaths, " +
                "temperature, systolic, diastolic, created_by, admission_id, tempUnits) " +
                "values (:patientId, :created, :type, :pulse, :breaths, :temperature, :systolic, " +
                ":diastolic, :createdBy, :admissionId, :tempUnits)";
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            bindParameters(connection.createQuery(sql), vitals).executeUpdate();
            return null;
        });
    }

    public List<Admission> getPatientAdmissions(Patient patient) {
        return executeSelect(connection -> connection
                .createQuery("select * from admissions_v where patient_id = :patientId order by created_at desc")
                .addParameter("patientId", patient.getId())
                .executeAndFetch(Admission.class));
    }
}
