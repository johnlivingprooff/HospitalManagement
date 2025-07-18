package app.models.billing.bills;

import lib.gintec_rdl.jbeava.validation.annotations.Filter;

public class PatientPayment extends GenericPayment {
    @Filter(
            label = "Details",
            filters = {"trim", "required", "length(1,200)"}
    )
    private String details;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
