package app.models.billing.bills;

import lib.gintec_rdl.jbeava.validation.annotations.Filter;

public class OtherPayment extends PatientPayment {
    @Filter(
            label = "Name of other individual or organization",
            filters = {"trim", "required", "length(1,200)"}
    )
    private String name;

    @Filter(
            label = "Address",
            filters = {"trim", "required", "length(1,200)"}
    )
    private String address;

    @Filter(
            label = "Phone",
            filters = {"trim", "required", "length(10,30)"}
    )
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
