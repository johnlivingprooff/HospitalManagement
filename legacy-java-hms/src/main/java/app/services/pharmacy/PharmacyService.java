package app.services.pharmacy;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.patient.Patient;
import app.models.pharmacy.medicine.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ServiceDescriptor
public final class PharmacyService extends ServiceImpl {
    private static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0);

    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public PharmacyService(Configuration configuration) {
        super(configuration);
    }

    public List<MedicineCategory> getMedicineCategories() {
        return executeSelect(connection -> connection
                .createQuery("select * from medicine_category where deleted = :deleted order by name")
                .addParameter("deleted", false)
                .executeAndFetch(MedicineCategory.class));
    }

    public void addMedicineCategory(MedicineCategory category) {
        category.setId(executeUpdate(connection -> (Long) connection
                .createQuery("insert into medicine_category (name, deleted) values (:name, :deleted)")
                .addParameter("name", category.getName())
                .addParameter("deleted", category.isDeleted())
                .executeUpdate()
                .getKey()));
    }

    public MedicineCategory getMedicineCategoryById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from medicine_category where deleted = :deleted and id = :id")
                .addParameter("deleted", false)
                .addParameter("id", id)
                .executeAndFetchFirst(MedicineCategory.class));
    }

    public void updateMedicineCategory(MedicineCategory category) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("update medicine_category set name = :name, deleted = :deleted where id = :id")
                    .addParameter("name", category.getName())
                    .addParameter("deleted", category.isDeleted())
                    .addParameter("id", category.getId())
                    .executeUpdate();
            return null;
        });
    }

    ////
    public List<MedicineLocation> getMedicineLocations() {
        return executeSelect(connection -> connection
                .createQuery("select * from medicine_locations")
                .executeAndFetch(MedicineLocation.class));
    }

    public void addMedicineLocation(MedicineLocation location) {
        location.setId(executeUpdate(connection -> (Long) connection
                .createQuery("insert into medicine_location (name, deleted) values (:name, :deleted)")
                .addParameter("name", location.getName())
                .addParameter("deleted", location.isDeleted())
                .executeUpdate()
                .getKey()));
    }

    public MedicineLocation getMedicineLocationById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from medicine_locations where id = :id")
                .addParameter("id", id)
                .executeAndFetchFirst(MedicineLocation.class));
    }

    public void updateMedicineLocation(MedicineLocation location) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("update medicine_location set name = :name, deleted = :deleted where id = :id")
                    .addParameter("name", location.getName())
                    .addParameter("deleted", location.isDeleted())
                    .addParameter("id", location.getId())
                    .executeUpdate();
            return null;
        });
    }

    public List<Medicine> getMedicines() {
        return executeSelect(connection -> connection
                .createQuery("select * from medicines")
                .executeAndFetch(Medicine.class));
    }

    public Medicine getMedicineById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from medicines where id = :id")
                .addParameter("id", id)
                .executeAndFetchFirst(Medicine.class));
    }

    public void addMedicine(Medicine medicine) {
        String sql = "insert into medicine (category, location, purchase_price, selling_price, quantity, generic_name "
                + ", updated, expires, deleted, name, threshold) values (:c,:l,:p,:s,:q,:g,:u,:e,:d,:n,:t)";
        medicine.setId(executeUpdate(connection -> (long) connection
                //.createQuery("select public.addMedicine(:c,:l,:p,:s,:q,:g,:u::,:e,:d,:n)")
                .createQuery(sql)
                .addParameter("c", medicine.getCategory())
                .addParameter("l", medicine.getLocation())
                .addParameter("p", medicine.getPurchasePrice())
                .addParameter("s", medicine.getSellingPrice())
                .addParameter("q", medicine.getQuantity())
                .addParameter("g", medicine.getGenericName())
                .addParameter("u", medicine.getUpdated())
                .addParameter("e", medicine.getExpires())
                .addParameter("d", medicine.isDeleted())
                .addParameter("n", medicine.getName())
                .addParameter("t", medicine.getThreshold())
                .executeUpdate()
                .getKey()));
        invalidateLastAlertTimes();
    }

    public void updateMedicine(Medicine medicine) {
        String sql = "update medicine set category = :c, location = :l, purchase_price = :p, selling_price = :s, " +
                "quantity = :q, generic_name=  :g, updated = :u, expires = :e, deleted = :d, name = :n," +
                "threshold = :t where id = :id";
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection
                    //.createQuery("select public.updateMedicine(:id,:c,:l,:p,:s,:q,:g,:u,:e,:d,:n)")
                    .createQuery(sql)
                    .addParameter("id", medicine.getId())
                    .addParameter("c", medicine.getCategory())
                    .addParameter("l", medicine.getLocation())
                    .addParameter("p", medicine.getPurchasePrice())
                    .addParameter("s", medicine.getSellingPrice())
                    .addParameter("q", medicine.getQuantity())
                    .addParameter("g", medicine.getGenericName())
                    .addParameter("u", medicine.getUpdated())
                    .addParameter("e", medicine.getExpires())
                    .addParameter("d", medicine.isDeleted())
                    .addParameter("n", medicine.getName())
                    .addParameter("t", medicine.getThreshold())
                    .executeUpdate();
            invalidateLastAlertTimes();
            return null;
        });
    }

    /**
     * <p>This will invalidate the alert log time so as to check the new levels again.</p>
     */
    private void invalidateLastAlertTimes() {
        LocalDateTime time = LocalDateTime.of(1970, 1, 1, 0, 0);
        updateLastStockLevelAlertTime(time);
        updateLastExpirationAlertTime(time);
    }

    public List<Medicine> getLowStockMedicines() {
        return executeSelect(connection -> connection
                .createQuery("select * from low_stock_medicines order by quantity asc")
                .executeAndFetch(Medicine.class));
    }

    public List<Medicine> getExpiringMedicines() {
        return executeSelect(connection -> connection
                .createQuery("select * from expiring_medicines order by expires asc")
                .executeAndFetch(Medicine.class));
    }

    public StockAlertConfiguration getStockAlertConfiguration() {
        return executeSelect(connection -> connection
                .createQuery("select * from medicine_expiration")
                .executeAndFetchFirst(StockAlertConfiguration.class));
    }

    public void updateSocketAlertConfiguration(StockAlertConfiguration config) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            String sql = "update medicine_expiration set days = :days, notifyExpiration = :ne, " +
                    "notifyStockLevel = :nsl, notificationEmail = :email where id = 1";
            connection.createQuery(sql)
                    .addParameter("days", config.getDays())
                    .addParameter("ne", config.isNotifyExpiration())
                    .addParameter("nsl", config.isNotifyStockLevel())
                    .addParameter("email", config.getNotificationEmail())
                    .executeUpdate();
            return null;
        });
    }

    private static final Map<String, String> mappings = new LinkedHashMap<>() {{
        put("last_expiration_alert_at", "lastExpirationAlertAt");
        put("last_stock_level_alert_at", "lastStockLevelAlertAt");
    }};


    public LocalDateTime getLastStockLevelAlertTime() {
        return getLastAlertTime("last_stock_level_alert_at");
    }

    public LocalDateTime getLastExpirationAlertTime() {
        return getLastAlertTime("last_expiration_alert_at");
    }

    private LocalDateTime getLastAlertTime(String type) {
        return executeSelect(connection -> connection
                .createQuery(format("select \"%s\" from medicine_alert_log", type))
                .setColumnMappings(mappings)
                .executeAndFetchFirst(LocalDateTime.class));
    }

    public void updateLastExpirationAlertTime(LocalDateTime time) {
        updateLastAlertTime("last_expiration_alert_at", time);
    }

    public void updateLastStockLevelAlertTime(LocalDateTime time) {
        updateLastAlertTime("last_stock_level_alert_at", time);
    }

    private void updateLastAlertTime(String type, LocalDateTime date) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            String sql = format("update medicine_alert_log set \"%s\" = :time", type);
            connection.createQuery(sql)
                    .addParameter("time", date)
                    .executeUpdate();
            return null;
        });
    }

    public void resetLastAlertTimes() {
        updateLastStockLevelAlertTime(DEFAULT_TIME);
        updateLastExpirationAlertTime(DEFAULT_TIME);
    }

    public void addPrescription(Prescription prescription) {
        prescription.setId(executeUpdate(connection -> {
            String sql = "insert into prescriptions(created_at, updated_at, patient_id, filer_id, " +
                    "updater_id, status, deleted) " +
                    "values (:created_at, :updated_at, :patient_id, :filer_id, :updater_id, :status, :deleted)";
            return (Long) connection.createQuery(sql)
                    .addParameter("created_at", prescription.getCreatedAt())
                    .addParameter("updated_at", prescription.getUpdatedAt())
                    .addParameter("patient_id", prescription.getPatientId())
                    .addParameter("filer_id", prescription.getFilerId())
                    .addParameter("updater_id", prescription.getUpdaterId())
                    .addParameter("status", prescription.getStatus())
                    .addParameter("deleted", prescription.isDeleted())
                    .executeUpdate()
                    .getKey();
        }));
    }

    public void updatePrescription(Prescription prescription) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            String sql = "update prescriptions set updated_at = :updated_at, updater_id = :updater_id, " +
                    "status = :status, deleted = :deleted where id = :id";
            connection.createQuery(sql)
                    .addParameter("id", prescription.getId())
                    .addParameter("updated_at", prescription.getUpdatedAt())
                    .addParameter("updater_id", prescription.getUpdaterId())
                    .addParameter("status", prescription.getStatus())
                    .addParameter("deleted", prescription.isDeleted())
                    .executeUpdate()
                    .getKey();
            return null;
        });
    }

    public Prescription getUnDispensedPrescriptionForPatient(long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescriptions_v where patient_id = :patientId and (status = :status1 or status = :status2)")
                .addParameter("patientId", patientId)
                .addParameter("status1", Prescription.Status.Pending)
                .addParameter("status2", Prescription.Status.Filed)
                .executeAndFetchFirst(Prescription.class));
    }


    public List<Prescription> getPrescriptionsByStatus(Prescription.Status status) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescriptions_v where status = :status")
                .addParameter("status", status)
                .executeAndFetch(Prescription.class));
    }

    public Prescription getPrescriptionById(long id, Prescription.Status status) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescriptions_v where id = :id and status = :status")
                .addParameter("status", status)
                .addParameter("id", id)
                .executeAndFetchFirst(Prescription.class));
    }

    public Prescription getPrescriptionById(long id, long filerId) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescriptions_v where id = :id and filer_id = :filer_id")
                .addParameter("filer_id", filerId)
                .addParameter("id", id)
                .executeAndFetchFirst(Prescription.class));
    }

    public Prescription getPrescriptionById(long id, long filerId, Prescription.Status status) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescriptions_v where id = :id and filer_id = :filer_id and status = :status")
                .addParameter("filer_id", filerId)
                .addParameter("id", id)
                .addParameter("status", status)
                .executeAndFetchFirst(Prescription.class));
    }

    public List<Prescription> getPrescriptionsByFiler(long filerId, Prescription.Status status) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescriptions_v where filer_id = :filer_id and status = :status order by created_at asc")
                .addParameter("filer_id", filerId)
                .addParameter("status", status)
                .executeAndFetch(Prescription.class));
    }

    public List<Medicine> getMedicineForPrescription(long prescriptionId) {
        return executeSelect(connection -> connection
                .createQuery("select * from getMedicineForPrescription(:id)")
                .addParameter("id", prescriptionId)
                .executeAndFetch(Medicine.class));
    }

    public Medicine getMedicineForPrescriptionById(long prescriptionId, long medicineId) {
        return executeSelect(connection -> connection
                .createQuery("select * from getMedicineForPrescription(:pid) where id = :mid")
                .addParameter("id", prescriptionId)
                .addParameter("mid", medicineId)
                .executeAndFetchFirst(Medicine.class));
    }

    public boolean isMedicineOnPrescriptionList(Prescription prescription, Medicine medicine) {
        // select exists (select 1 from prescription_drugs_v where prescription_id = :pid and medicine_id = :mid)
        return executeSelect(connection -> {
            String sql = "select exists (select 1 from prescription_drugs_v where prescription_id = :pid and medicine_id = :mid)";
            return connection.createQuery(sql)
                    .addParameter("pid", prescription.getId())
                    .addParameter("mid", medicine.getId())
                    .executeAndFetchFirst(Boolean.class);
        });
    }

    public List<PrescriptionDrug> getMedicinesOnPrescription(Prescription prescription) {
        switch (prescription.getStatus()) {
            case Dispensed:
                return executeSelect(connection -> connection
                        .createQuery("select * from dispensed_medicine where prescription_id = :id")
                        .addParameter("id", prescription.getId())
                        .addColumnMapping("prescribed", "quantity")
                        .addColumnMapping("dispensed", "actualQuantity")
                        .addColumnMapping("price", "sellingPrice")
                        .addColumnMapping("total", "totalCost")
                        .executeAndFetch(PrescriptionDrug.class));
            case Pending:
            case Filed:
            default:
                return executeSelect(connection -> connection
                        .createQuery("select * from prescription_drugs_v where prescription_id = :id")
                        .addParameter("id", prescription.getId())
                        .executeAndFetch(PrescriptionDrug.class));
        }
    }

    public void addMedicineToPrescription(PrescriptionDrug drug) {
        String sql = "insert into prescription_drugs(prescription_id, medicine_id, quantity, notes) " +
                "values (:pid, :mid, :qty, :notes)";
        drug.setId(executeUpdate(connection -> (Long) connection
                .createQuery(sql)
                .addParameter("pid", drug.getPrescriptionId())
                .addParameter("mid", drug.getMedicineId())
                .addParameter("qty", drug.getQuantity())
                .addParameter("notes", drug.getNotes())
                .executeUpdate().getKey()));
    }

    public PrescriptionDrug getPrescribedMedicine(long drugId, long prescriptionId) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescription_drugs_v where prescription_id = :pid and id = :id")
                .addParameter("pid", prescriptionId)
                .addParameter("id", drugId)
                .executeAndFetchFirst(PrescriptionDrug.class));
    }

    public void removeMedicineFromPrescription(PrescriptionDrug drug) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("delete from prescription_drugs where id = :id and prescription_id = :pid")
                    .addParameter("pid", drug.getPrescriptionId())
                    .addParameter("id", drug.getId())
                    .executeUpdate();
            return null;
        });
    }

    /**
     * Creates a bill
     *
     * @param prescription .
     */
    public void dispensePrescription(Prescription prescription) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            String sql = "update prescriptions SET status = :status, updated_at = :uat, updater_id = :uid  " +
                    "where id = :id";
            // 1. Update prescription
            connection.createQuery(sql)
                    .addParameter("status", prescription.getStatus())
                    .addParameter("uat", prescription.getUpdatedAt())
                    .addParameter("uid", prescription.getUpdaterId())
                    .addParameter("id", prescription.getId())
                    .executeUpdate();

            // 2. copy all prescriptions into static references
            connection.createQuery("select public.\"createPrescriptionMedicineHistory\" (:prescription_id)")
                    .addParameter("prescription_id", prescription.getId())
                    .executeAndFetchFirst(Long.class);

            // 3. deduct from quantity
            connection.createQuery("select updateMedicineStockQuantity(:prescription_id)")
                    .addParameter("prescription_id", prescription.getId())
                    .executeAndFetchFirst(Long.class);
            return null;
        });
        // Immediately trigger low level stock email
        updateLastStockLevelAlertTime(DEFAULT_TIME);
    }

    public long getTotalPrescriptionMedicineQuantity(Prescription prescription) {
        final String sql = "SELECT sum(actualQuantity) from prescription_drugs_v where prescription_id = :id";
        return executeSelect(connection -> connection.createQuery(sql)
                .addParameter("id", prescription.getId())
                .executeAndFetchFirst(Long.class));
    }

    public double getPrescriptionTotalCost(Prescription prescription) {
        return executeSelect(connection -> connection
                .createQuery("SELECT sum(totalCost) from prescription_drugs_v where prescription_id = :id")
                .addParameter("id", prescription.getId())
                .executeAndFetchFirst(Double.class));
    }

    public List<Prescription> getDispensedPatientPrescriptions(Patient patient) {
        String sql = "select * from prescriptions_v where patient_id = :pid and status = :status order by created_at desc";
        return executeSelect(connection -> connection
                .createQuery(sql)
                .addParameter("pid", patient.getId())
                .addParameter("status", Prescription.Status.Dispensed)
                .executeAndFetch(Prescription.class));
    }

    public Prescription getPatientsDispensedPrescriptionById(long id, long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescriptions_v where id = :id and patient_id = :patientId")
                .addParameter("id", id)
                .addParameter("patientId", patientId)
                .executeAndFetchFirst(Prescription.class));
    }

    public List<PrescriptionDrug> getPrescriptionDrugs(Prescription prescription) {
        return executeSelect(connection -> connection
                .createQuery("select * from prescription_drugs_v where prescription_id = :id")
                .addParameter("id", prescription.getId())
                .executeAndFetch(PrescriptionDrug.class));
    }
}
