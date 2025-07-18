package app.services.nurse;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.billing.bills.Bill;
import app.models.nurse.VitalsFee;
import app.models.patient.Vitals;

@ServiceDescriptor
public class NurseService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public NurseService(Configuration configuration) {
        super(configuration);
    }

    public VitalsFee getVitalsFee() {
        return executeSelect(connection -> connection
                .createQuery("select * from vitals_fee")
                .executeAndFetchFirst(VitalsFee.class));
    }

    public void updateVitalsFee(VitalsFee fee) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            bindParameters(
                    connection.createQuery("update vitals_fee set fee = :fee, updated_at = :updatedAt"),
                    fee
            ).executeUpdate();
            return null;
        });
    }

    /**
     * Do not use this method under admissions because it will create a bill for the patient
     *
     * @param vitals .
     */
    public void addVitals(Vitals vitals) {
        String sql = "select * from addVitals(:patientId, :created, :type, :pulse, :breaths, :temperature::numeric, " +
                ":systolic, :diastolic, :createdBy, :tempUnits, :billType, :billStatus)";
        executeSelect((SqlSelectTask<Long>) connection -> {
            connection.createQuery(sql)
                    .addParameter("patientId", vitals.getPatientId())
                    .addParameter("created", vitals.getCreated())
                    .addParameter("type", vitals.getType())
                    .addParameter("pulse", vitals.getPulse())
                    .addParameter("breaths", vitals.getBreaths())
                    .addParameter("temperature", vitals.getTemperature())
                    .addParameter("systolic", vitals.getSystolic())
                    .addParameter("diastolic", vitals.getDiastolic())
                    .addParameter("createdBy", vitals.getCreatedBy())
                    .addParameter("tempUnits", vitals.getTempUnits())
                    .addParameter("billType", Bill.BillType.Vitals)
                    .addParameter("billStatus", Bill.BillStatus.UnPaid)
                    .executeAndFetchFirst(Long.class);
            return null;
        });
    }
}
