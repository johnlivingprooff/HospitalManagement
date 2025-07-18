package app.models.admissions;

import lib.gintec_rdl.jbeava.validation.annotations.Filter;

public class AdmissionRates {
    @Filter(
            label = "Short stay admission rate",
            filters = {"required", "double", "range(1,9999999)"}
    )
    private double shortStay;
    @Filter(
            label = "Full admission rate",
            filters = {"required", "double", "range(1,9999999)"}
    )
    private double fullAdmission;

    public double getShortStay() {
        return shortStay;
    }

    public void setShortStay(double shortStay) {
        this.shortStay = shortStay;
    }

    public double getFullAdmission() {
        return fullAdmission;
    }

    public void setFullAdmission(double fullAdmission) {
        this.fullAdmission = fullAdmission;
    }
}
