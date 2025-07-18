package app.models.billing.bills;

import java.math.BigDecimal;

public class PatientBill {
    private long patientId;
    private String mrn;
    private String phone;
    private String patient;
    private long billCount;
    private BigDecimal paid;
    private BigDecimal balance;

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public long getBillCount() {
        return billCount;
    }

    public void setBillCount(long billCount) {
        this.billCount = billCount;
    }

    @Override
    public String toString() {
        return patient + " - " + paid.toString() + "/" + balance.toString();
    }
}
