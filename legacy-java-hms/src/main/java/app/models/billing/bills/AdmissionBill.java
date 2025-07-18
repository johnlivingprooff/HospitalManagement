package app.models.billing.bills;

public class AdmissionBill extends Bill {
    private long admissionId;

    // id | type [Short, Full] | rate [per night / per hour ] |

    public AdmissionBill() {
        super(BillType.Admission);
    }
}
