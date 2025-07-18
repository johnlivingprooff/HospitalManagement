package app.models.pharmacy.medicine;

import app.models.Contexts;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

public class PrescriptionDrug {
    @Filter(
            label = "Prescription medicine id",
            filters = {"required", "long"},
            contexts = {Contexts.FIND}
    )
    private long id;

    @Filter(
            label = "Prescription notes",
            filters = {"trim", "length(1,256)"},
            contexts = Contexts.CREATE
    )
    private String notes;

    @Filter(
            label = "Prescription quantity",
            filters = {"required", "long", "positive", "range(1,1000)"},
            contexts = Contexts.CREATE
    )
    private long quantity;

    @Filter(
            label = "Medicine Id",
            filters = {"required", "long"},
            contexts = Contexts.CREATE
    )
    private long medicineId;
    private boolean expiring;
    private boolean runningLow;
    private long stockQuantity;
    private long actualQuantity;
    private double sellingPrice;
    private double totalCost;

    private long prescriptionId;

    private String medicineName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(long medicineId) {
        this.medicineId = medicineId;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public boolean isExpiring() {
        return expiring;
    }

    public void setExpiring(boolean expiring) {
        this.expiring = expiring;
    }

    public boolean isRunningLow() {
        return runningLow;
    }

    public void setRunningLow(boolean runningLow) {
        this.runningLow = runningLow;
    }

    public long getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(long stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public long getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(long actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {
        return "{id=" + getId() + ", medicineId=" + getMedicineId() + "}";
    }
}
