package app.services.lab;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.billing.bills.Bill;
import app.models.lab.LabTest;

import java.util.List;

@ServiceDescriptor
public class LabService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public LabService(Configuration configuration) {
        super(configuration);
    }

    public List<LabTest> getLabTestsByExaminer(long examiner) {
        return executeSelect(connection -> connection
                .createQuery("select * from lab_tests_v where examiner = :examiner")
                .addParameter("examiner", examiner)
                .executeAndFetch(LabTest.class));
    }

    public List<LabTest> getPatientLabTests(long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from lab_tests_v where patient_id = :id")
                .addParameter("id", patientId)
                .executeAndFetch(LabTest.class));
    }

    public void addLabTest(LabTest labTest) {
        labTest.setId(executeUpdate(connection -> connection
                .createQuery("select * from addLabTest(:n, :e, :pid, :proc, :att, :date, :type, :status)")
                .addParameter("n", labTest.getNotes())
                .addParameter("e", labTest.getExaminer())
                .addParameter("pid", labTest.getPatientId())
                .addParameter("proc", labTest.getProcedureId())
                .addParameter("att", labTest.getAttachment())
                .addParameter("date", labTest.getCreatedAt())
                .addParameter("type", Bill.BillType.Lab)
                .addParameter("status", Bill.BillStatus.UnPaid)
                .executeAndFetchFirst(Long.class)));
    }

    public LabTest getLabTestById(long id, long examiner) {
        return executeSelect(connection -> connection
                .createQuery("select * from lab_tests_v where id = :id and examiner = :examiner")
                .addParameter("id", id)
                .addParameter("examiner", examiner)
                .executeAndFetchFirst(LabTest.class));
    }

    public LabTest getPatientLabTest(long testId, long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from lab_tests_v where id = :testId and patient_id = :patientId")
                .addParameter("testId", testId)
                .addParameter("patientId", patientId)
                .executeAndFetchFirst(LabTest.class));
    }
}
