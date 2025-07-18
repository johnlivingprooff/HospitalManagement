package app.models.lab;

import app.models.billing.bills.Bill;

public class LabBill extends Bill {
    private long testId;

    public LabBill() {
        super(BillType.Lab);
    }

    public long getTestId() {
        return testId;
    }

    public void setTestId(long testId) {
        this.testId = testId;
    }
}
