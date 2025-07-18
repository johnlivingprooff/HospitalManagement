package app.models.billing.bills;

public class PrescriptionBill extends Bill {
    private long prescriptionId;

    public PrescriptionBill() {
        super(BillType.Prescription);
    }

    public long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
}