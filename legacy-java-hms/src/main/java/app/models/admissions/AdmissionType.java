package app.models.admissions;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(label = "description", value = "name()")
public enum AdmissionType {
    ShortStay("Short Stay/Emergency"),
    FullAdmission("Full Admission");

    AdmissionType(String description) {
        this.description = description;
    }

    public final String description;

    public static final AdmissionType[] TYPES = values();
}
