package app.models.patient;


import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(value = "name()")
public enum PatientType {
    Inpatient("Patient is admitted and under facility care"),
    Outpatient("Patient left after receiving services");

    PatientType(String description) {
        this.description = description;
    }

    public final String description;

    public static final PatientType[] TYPES = values();
}
