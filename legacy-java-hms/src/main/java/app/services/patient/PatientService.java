package app.services.patient;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.daos.patient.PatientDao;
import app.models.account.Sex;
import app.models.billing.bills.GenericBill;
import app.models.patient.*;
import app.util.LocaleUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@ServiceDescriptor
public final class PatientService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public PatientService(Configuration configuration) {
        super(configuration);
    }

    public List<Patient> search(String firstName, String lastName, Sex sex, Date dateOfBirth) {
        return withDao(PatientDao.class).search(firstName, lastName, dateOfBirth, sex);
    }

    public Patient findByIdNumber(String idNumber) {
        return withDao(PatientDao.class).findByIdNumber(idNumber);
    }

    public void add(Patient patient) {
        PatientIds ids = withDao(PatientDao.class).add(patient);
        patient.setId(ids.getId());
        patient.setMrn(ids.getMrn());
    }

    public Patient findById(long id) {
        return withDao(PatientDao.class).findById(id);
    }

    public List<Identification> getPatientIds(long patientId) {
        return withDao(PatientDao.class).getPatientIds(patientId);
    }

    public Patient getPatientByPatientId(String mrn) {
        return executeSelect(connection -> connection
                .createQuery("select * from patient where lower(mrn) = lower(:mrn)")
                .addParameter("mrn", mrn)
                .executeAndFetchFirst(Patient.class));
    }

    public Patient getPatientByEmail(String email) {
        return executeSelect(connection -> connection
                .createQuery("select * from patient where lower(email) = lower(:email)")
                .addParameter("email", email)
                .executeAndFetchFirst(Patient.class));
    }

    public List<Patient> getPatientsByType(PatientType inpatient) {
        return withDao(PatientDao.class).getPatientsByType(inpatient);
    }

    public PatientInfo findPatientInfoByEmail(String email) {
        return executeSelect(connection -> {
            String sql = "select id, email, active, status, (firstName || ' ' || lastName) as fullName " +
                    "from patient " +
                    "where active = true and lower(email) = lower(:email) limit 1";
            return connection.createQuery(sql)
                    .addParameter("email", email)
                    .executeAndFetchFirst(PatientInfo.class);
        });
    }

    public PatientInfo findPatientInfoById(long patientId) {
        return executeUpdate(connection -> {
            String sql = "select id, email, active, status, (firstName || ' ' || lastName) as fullName " +
                    "from patient " +
                    "where id = :patientId limit 1";
            return connection.createQuery(sql)
                    .addParameter("patientId", patientId)
                    .executeAndFetchFirst(PatientInfo.class);
        });
    }

    public PatientInfo findPatientInfoByMrn(String mrn) {
        return executeSelect(connection -> {
            String sql = "select id, email, active, status, (firstName || ' ' || lastName) as fullName " +
                    "from patient " +
                    "where active = true and lower(mrn) = lower(:mrn) limit 1";
            return connection.createQuery(sql)
                    .addParameter("mrn", mrn)
                    .executeAndFetchFirst(PatientInfo.class);
        });
    }

    public String getPatientPassword(long id) {
        return executeSelect(connection -> connection
                .createQuery("select password from patient where id = :id")
                .addParameter("id", id)
                .executeAndFetchFirst(String.class));
    }

    public void updatePatientPassword(long id, String password) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("update patient set password = :password  where id = :id")
                    .addParameter("id", id)
                    .addParameter("password", password)
                    .executeUpdate();
            return null;
        });
    }

    public File getUserProfileImage(long patientId) {
        String fileName = executeSelect(connection -> connection
                .createQuery("select picture from patient where id = :id")
                .addParameter("id", patientId)
                .executeAndFetchFirst(String.class));
        if (LocaleUtil.isNullOrEmpty(fileName)) {
            return null;
        }
        return new File(getSystemConfiguration().ImageDirectory, fileName);
    }

    public Patient getPatientByAccountId(long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from patient where id = :id")
                .addParameter("id", patientId)
                .executeAndFetchFirst(Patient.class));
    }

    public boolean isEmailAddressInUse(String email) {
        return executeSelect(connection -> connection
                .createQuery("select exists(select 1 from patient where lower(email) = lower(:email))")
                .addParameter("email", email)
                .executeAndFetchFirst(Boolean.class));
    }

    public void update(Patient patient) {
        withDao(PatientDao.class).updatePatient(patient);
    }

    public boolean isIdNumberInUse(String idNumber) {
        return executeSelect(connection -> connection
                .createQuery("select exists(select 1 from patient where lower(idNumber) = lower(:idNumber))")
                .addParameter("idNumber", idNumber)
                .executeAndFetchFirst(Boolean.class));
    }

    public PatientNextOfKin getNextOfKin(Patient patient) {
        return executeSelect(connection -> connection
                .createQuery("select * from patient_nok where patientId = :patientId limit 1")
                .addParameter("patientId", patient.getId())
                .executeAndFetchFirst(PatientNextOfKin.class));
    }

    public void deleteNextOfKin(PatientNextOfKin nok) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("delete from patient_nok where id = :id and patientId = :patientId")
                    .addParameter("id", nok.getId())
                    .addParameter("patientId", nok.getPatientId())
                    .executeUpdate();
            return null;
        });
    }

    public void updateNextOfKin(PatientNextOfKin nok) {
        long row = executeUpdate(connection -> {
            String sql = "insert into patient_nok (firstName, lastName, address, phone1, phone2, relationShipType, " +
                    "patientId, created, modified) values " +
                    "(:firstName, :lastName, :address, :phone1, :phone2, :relationship, :patientId, " +
                    ":created, :modified) on conflict (patientId) " +
                    "do update set firstName = :firstName, lastName = :lastName, address = :address, " +
                    "phone1 = :phone1, phone2 = :phone2, relationShipType = :relationship, modified = :modified";
            return connection.createQuery(sql)
                    .addParameter("firstName", nok.getFirstName())
                    .addParameter("lastName", nok.getLastName())
                    .addParameter("address", nok.getAddress())
                    .addParameter("phone1", nok.getPhone1())
                    .addParameter("phone2", nok.getPhone2())
                    .addParameter("relationship", nok.getRelationship())
                    .addParameter("patientId", nok.getPatientId())
                    .addParameter("created", nok.getCreated())
                    .addParameter("modified", nok.getModified())
                    .executeUpdate()
                    .getKey(Long.class);
        });
        if (nok.getId() == 0) {
            nok.setId(row);
        }
    }

    public Insurance getInsurance(Patient patient) {
        return getInsurance(patient.getId());
    }

    public Insurance getInsurance(long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from patient_insurance where patientId = :patientId")
                .addParameter("patientId", patientId)
                .executeAndFetchFirst(Insurance.class));
    }

    public void updateInsurance(Insurance insurance) {
        long row = executeUpdate(connection -> {
            String sql = "insert into patient_insurance (insurer, address, phone, patientId, membershipId, created, " +
                    "modified) values (:insurer, :address, :phone, :patientId, :membershipId, :created, :modified) " +
                    "on conflict (patientId) do update set insurer = :insurer, address = :address, phone = :phone, " +
                    "membershipId = :membershipId, modified = :modified";
            return connection.createQuery(sql)
                    .addParameter("insurer", insurance.getInsurer())
                    .addParameter("address", insurance.getAddress())
                    .addParameter("phone", insurance.getPhone())
                    .addParameter("patientId", insurance.getPatientId())
                    .addParameter("membershipId", insurance.getMembershipId())
                    .addParameter("created", insurance.getCreated())
                    .addParameter("modified", insurance.getModified())
                    .executeUpdate()
                    .getKey(Long.class);
        });
        if (insurance.getId() == 0) {
            insurance.setId(row);
        }
    }

    private List<PatientDocument> getPatientDocuments(Patient patient, boolean filter, boolean hidden) {
        return executeSelect(connection -> {
            if (filter) {
                return connection.createQuery("select * patient_documents where patient_id = :id and hidden = :h order by id desc")
                        .addParameter("id", patient.getId())
                        .addParameter("h", hidden)
                        .executeAndFetch(PatientDocument.class);
            } else {
                return connection.createQuery("select * from patient_documents where patient_id = :patientId order by id desc")
                        .addParameter("patientId", patient.getId())
                        .executeAndFetch(PatientDocument.class);
            }
        });
    }

    public List<PatientDocument> getPatientDocuments(Patient patient) {
        return getPatientDocuments(patient, false, false);
    }

    /**
     * Get list of patient documents for patient's own use, excluding hidden documents.
     *
     * @param patient .
     * @return .
     */
    public List<PatientDocument> getPatientDocumentsForPatient(Patient patient) {
        return getPatientDocuments(patient, true, false);
    }

    public PatientDocument getPatientDocument(long documentId, long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from patient_documents where patient_id = :pid and id = :did")
                .addParameter("pid", patientId)
                .addParameter("did", documentId)
                .executeAndFetchFirst(PatientDocument.class));
    }

    public void addPatientDocument(PatientDocument document) {
        String sql = "insert into patient_documents (patient_id, name, attachment, hidden) " +
                "values (:pid, :name, :attachment, :hidden)";
        document.setId(executeUpdate(connection -> connection
                .createQuery(sql)
                .addParameter("pid", document.getPatientId())
                .addParameter("name", document.getName())
                .addParameter("attachment", document.getAttachment())
                .addParameter("hidden", document.isHidden())
                .executeUpdate()
                .getKey(Long.class)));
    }

    public void updatePatientDocument(PatientDocument document) {
        String sql = "update patient_documents " +
                "set name = :name, attachment = :attachment, hidden = :hidden " +
                "where id = :id and patient_id = :pid";

        document.setId(executeUpdate(connection -> connection
                .createQuery(sql)
                .addParameter("id", document.getId())
                .addParameter("pid", document.getPatientId())
                .addParameter("name", document.getName())
                .addParameter("attachment", document.getAttachment())
                .addParameter("hidden", document.isHidden())
                .executeUpdate()
                .getKey(Long.class)));
    }

    public void deletePatientDocument(PatientDocument document) {
        File file = new File(getSystemConfiguration().AttachmentDirectory, document.getAttachment());
        if (!file.delete()) {
            getLogger().error("Error deleting patient file {}", file.getAbsolutePath());
        }
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("delete from patient_documents where patient_id = :pid and id = :did")
                    .addParameter("did", document.getId())
                    .addParameter("pid", document.getPatientId())
                    .executeUpdate();
            return null;
        });
    }

    public boolean hasInsuranceDetails(long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select exists(select 1 from patient_insurance where patientId = :id limit 1)")
                .addParameter("id", patientId).executeAndFetchFirst(Boolean.class));
    }

    public List<Vitals> getPatientVitals(Patient patient) {
        return executeSelect(connection -> connection
                .createQuery("select * from patient_vitals_v where patient_id = :patientId order by created desc")
                .addParameter("patientId", patient.getId())
                .executeAndFetch(Vitals.class));
    }

    public List<GenericBill> getPatientBills(Patient patient) {
        return executeSelect(connection -> connection
                .createQuery("select * from bills where patient_id = :patientId order by created_at desc")
                .addParameter("patientId", patient.getId())
                .executeAndFetch(GenericBill.class));
    }

    public void deleteBirth(Birth birth) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("delete from births where id = :id and patient_id = :patientId")
                    .addParameter("id", birth.getId())
                    .addParameter("patientId", birth.getPatientId())
                    .executeUpdate();
            return null;
        });
    }

    public List<Birth> getPatientBirths(Patient patient) {
        return executeSelect(connection -> connection
                .createQuery("select * from births_v where patient_id = :patientId order by dob desc")
                .addParameter("patientId", patient.getId())
                .executeAndFetch(Birth.class));
    }

    public Birth getPatientBirth(long birthId, long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from births_v where id = :id and patient_id = :patientId")
                .addParameter("id", birthId)
                .addParameter("patientId", patientId)
                .executeAndFetchFirst(Birth.class));
    }

    public void addPatientBirth(Birth birth) {
        String sql = "insert into births" +
                "(patient_id, name, sex, dob, created, pulse, breaths, height, weight, temperature, systolic, diastolic, " +
                "created_by, tempUnits) " +
                "values (:patientId, :name, :sex, :dob, :created, :pulse, :breaths, :height, :weight, :temperature, :systolic, :diastolic, :createdBy, :tempUnits)";
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            bindParameters(
                    connection.createQuery(sql),
                    birth
            ).executeUpdate();
            return null;
        });
    }

    public DeathReport getPatientDeathReport(Patient patient) {
        return executeSelect(connection -> connection
                .createQuery("select * from death_reports_v where patient_id = :patientId")
                .addParameter("patientId", patient.getId())
                .executeAndFetchFirst(DeathReport.class));
    }

    public void addPatientDeathReport(DeathReport report) {
        String sql = "insert into death_reports (patient_id, dod, created_at, created_by, attachment) " +
                "values (:patientId, :dod, :createdAt, :createdBy, :attachment)";
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            bindParameters(connection.createQuery(sql), report).executeUpdate();
            connection.createQuery("update patient set status = :status, active = :active, modified = :modified where id = :id")
                    .addParameter("status", PatientStatus.Expired)
                    .addParameter("modified", LocalDateTime.now())
                    .addParameter("active", false)
                    .addParameter("id", report.getPatientId())
                    .executeUpdate();
            return null;
        });
    }

    public List<DeathReport> getDeathReports() {
        return executeSelect(connection -> connection
                .createQuery("select * from death_reports_v")
                .executeAndFetch(DeathReport.class));
    }

    public DeathReport getDeathReportById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from death_reports_v where id = :id")
                .addParameter("id", id)
                .executeAndFetchFirst(DeathReport.class));
    }
}
