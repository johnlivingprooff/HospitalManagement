package app.models.billing.bills;

import lib.gintec_rdl.jbeava.validation.annotations.Filter;

public class GenericPayment {

    @Filter(
            label = "Amount",
            filters = {"required", "double", "range(0.01,999999999.99)"}
    )
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
