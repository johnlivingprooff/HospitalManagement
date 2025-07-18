package app.models.patient;

public enum VitalsType {
    Admission("Admission"),
    NursesStation("Nurse's Station");

    VitalsType(String description) {
        this.description = description;
    }

    public final String description;
}
