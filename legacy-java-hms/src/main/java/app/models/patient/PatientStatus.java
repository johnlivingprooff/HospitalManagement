package app.models.patient;

/**
 * Describes the current status of patients
 */
public enum PatientStatus {
    Registered("Patient has just been registered into the system for the first time."),
    Expired("Patient is deceased."),
    UnderTreatment("Patient is currently being treated or receiving medical services."),
    Recovered("Patient was discharged due to recovery."),
    Treated("Patient received expected services.");

    PatientStatus(String description) {
        this.description = description;
    }

    public final String description;
}
