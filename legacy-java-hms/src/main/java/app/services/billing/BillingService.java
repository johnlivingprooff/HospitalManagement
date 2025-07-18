package app.services.billing;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.billing.bills.*;
import org.sql2o.Connection;

import java.util.List;

@ServiceDescriptor
public class BillingService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public BillingService(Configuration configuration) {
        super(configuration);
    }

    private void addBill(Bill bill, Connection connection) {
        String sql = "insert into bills (paid, balance, patient_id, bill_type, status, created_at, updated_at) " +
                "values(:paid, :balance, :patient_id, :bill_type, :status, :created_at, :updated_at)";
        bill.setId(
                (long) connection.createQuery(sql)
                        .addParameter("paid", bill.getPaid())
                        .addParameter("balance", bill.getBalance())
                        .addParameter("patient_id", bill.getPatientId())
                        .addParameter("bill_type", bill.getBillType())
                        .addParameter("status", bill.getStatus())
                        .addParameter("created_at", bill.getCreatedAt())
                        .addParameter("updated_at", bill.getUpdatedAt())
                        .executeUpdate().getKey()
        );
        if (bill instanceof PrescriptionBill) {
            connection.createQuery("insert into prescription_bills (bill_id, prescription_id) values (:bid, :pid)")
                    .addParameter("bid", bill.getId())
                    .addParameter("pid", ((PrescriptionBill) bill).getPrescriptionId())
                    .executeUpdate();
        } else {
            throw new IllegalArgumentException("Unsupported bill type " + bill.getBillType());
        }
    }

    public void addPrescriptionBill(PrescriptionBill bill) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            addBill(bill, connection);
            return null;
        });
    }

    public List<PatientBill> getOutstandingBills() {
        return executeSelect(connection -> connection
                .createQuery("select * from outstanding_bills_v")
                .executeAndFetch(PatientBill.class));
    }

    public List<PatientBill> getPaidBills() {
        return executeSelect(connection -> connection
                .createQuery("select * from paid_bills_v")
                .executeAndFetch(PatientBill.class));
    }

    private List<GenericBill> getPatientsBills(long id, Bill.BillStatus status) {
        return executeSelect(connection -> connection
                .createQuery("select * from bills where status = :status and patient_id = :id order by created_at desc")
                .addParameter("status", status)
                .addParameter("id", id)
                .executeAndFetch(GenericBill.class));
    }

    public List<GenericBill> getPatientsUnpaidBills(long id) {
        return getPatientsBills(id, Bill.BillStatus.UnPaid);
    }

    public List<GenericBill> getPatientsPaidBills(long id) {
        return getPatientsBills(id, Bill.BillStatus.Paid);
    }

    public GenericBill getBillById(long id, long patientId) {
        return executeSelect(connection -> connection
                .createQuery("select * from bills where id = :id and patient_id = :patientId limit 1")
                .addParameter("id", id)
                .addParameter("patientId", patientId)
                .executeAndFetchFirst(GenericBill.class));
    }

    public GenericBill getBillById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from bills where id = :id limit 1")
                .addParameter("id", id)
                .executeAndFetchFirst(GenericBill.class));
    }

    private void updateBill(Connection connection, Bill bill) {
        String sql = "update bills set balance = :balance, paid = :paid, status = :status, " +
                "updated_at = :updatedAt where id = :id and patient_id = :patientId";
        bindParameters(connection.createQuery(sql), bill)
                .executeUpdate();
    }

    private void addPayment(Connection connection, BillPayment payment) {
        String sql = "insert into bill_payments " +
                "   (bill_id, amount, payer, name, phone, address, details, created_at, created_by) " +
                "values (:billId, :amount, :payer, :name, :phone, :address, :details, :createdAt, :createdBy)";
        bindParameters(connection.createQuery(sql), payment).executeUpdate();
    }

    public void addBillPayment(BillPayment payment, Bill bill) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            updateBill(connection, bill);
            addPayment(connection, payment);
            return null;
        });
    }

    public List<RecentPayment> getRecentPayments(){
        return executeSelect(connection -> connection
                .createQuery("select * from recent_payments order by created_at desc")
                .executeAndFetch(RecentPayment.class));
    }
}
