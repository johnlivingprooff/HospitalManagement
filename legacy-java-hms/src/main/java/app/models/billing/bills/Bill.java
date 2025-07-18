package app.models.billing.bills;

import java.time.LocalDateTime;

public abstract class Bill {
    public enum BillType {
        Lab("Lab Fees"),
        Dental("Dental Bills"),
        Prescription("Prescription Bills"),
        Admission("Admission Fees"),
        Theater("Operation/Surgery"),
        Vitals("Vitals"),
        Consultation("Consultation Fees");

        BillType(String description) {
            this.description = description;
        }

        public final String description;
    }

    public enum BillStatus {
        Paid("Fully Paid", "Fully Paid Bills"),
        UnPaid("Outstanding", "Outstanding Bills");

        BillStatus(String description, String title) {
            this.title = title;
            this.description = description;
        }

        public final String title;
        public final String description;
    }

    private long id;
    private double paid;
    private double balance;
    private long patientId;
    private BillType billType;
    private BillStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Bill(BillType billType) {
        this.billType = billType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getPaid() {
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }
}
