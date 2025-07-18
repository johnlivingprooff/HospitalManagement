package app.models.billing.bills;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentPayment {
    private long id;
    private String mrn;
    private String patient;
    private String processor;
    private BigDecimal amount;
    private Bill.BillType billType;
    private LocalDateTime createdAt;
    private BillPayment.Payer payer;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Bill.BillType getBillType() {
        return billType;
    }

    public void setBillType(Bill.BillType billType) {
        this.billType = billType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BillPayment.Payer getPayer() {
        return payer;
    }

    public void setPayer(BillPayment.Payer payer) {
        this.payer = payer;
    }
}
