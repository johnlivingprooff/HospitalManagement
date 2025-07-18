package app.models.billing.bills;

import app.util.LocaleUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillPayment {

    public enum Payer {
        Self,
        Insurance,
        Other;
    }

    private long id;
    private String name;
    private long billId;
    private Payer payer;
    private String phone;
    private double amount;
    private String address;
    private String details;
    private long createdBy;
    private LocalDateTime createdAt;

    public long getId() {
        return id;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBillId() {
        return billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }

    @Override
    public String toString() {
        return LocaleUtil.dec2str(BigDecimal.valueOf(amount)) + " " + payer;
    }
}
