package app.models.patient;

import app.core.validation.FilterNode;
import app.core.validation.ValidationChain;
import app.core.validation.validators.MedicalRecordsNumber;
import app.core.validation.validators.Required;

public class PatientVitals extends Vitals {
    @ValidationChain(label = "Patient ID", filters = {@FilterNode(Required.class), @FilterNode(MedicalRecordsNumber.class)})
    private String mrn;

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }
}
