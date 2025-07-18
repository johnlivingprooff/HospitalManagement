package app.models.patient;

public class PatientInfo {
    private long id;
    private String email;
    private boolean active;
    private String fullName;
    private PatientStatus status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public PatientStatus getStatus() {
        return status;
    }

    public void setStatus(PatientStatus status) {
        this.status = status;
    }

    public boolean isEligibleForPrescriptions() {
        return active && status == PatientStatus.UnderTreatment;
    }

    public String getPrescriptionIneligibilityReason() {
        if (!active) {
            return "Patient account is not active.";
        }
        if (status != PatientStatus.UnderTreatment) {
            return "This patient is not eligible for prescriptions at the moment because they are currently not receiving " +
                    "any services. Make sure the patient is properly registered in the system.";
        }
        throw new IllegalStateException("This should not happen.");
    }

    public boolean isEligibleForAdmission() {
        return active && status != PatientStatus.Expired;
    }

    public String getAdmissionIneligibilityReason() {
        if (!active) {
            return "Patient is not active in the system.";
        }
        if (status == PatientStatus.Expired) {
            return "Cannot create admission for expired patients.";
        }
        throw new IllegalStateException("This should not happen.");
    }

    public boolean isEligibleForLabTest() {
        return active && status != PatientStatus.Expired;
    }

    public String getLabTestIneligibilityReason() {
        if (!active) {
            return "Patient is not active in the system.";
        }
        if (status == PatientStatus.Expired) {
            return "Cannot create tests to expired patients.";
        }
        throw new IllegalStateException("This should not happen.");
    }

    public boolean isEligibleForMedicalSurgery() {
        return active && status != PatientStatus.Expired;
    }

    public String getMedicalSurgeryIneligibilityReason() {
        if (!active) {
            return "Patient is not active in the system.";
        }
        if (status == PatientStatus.Expired) {
            return "Cannot upload surgery results for expired patients.";
        }
        throw new IllegalStateException("This should not happen.");
    }

    public boolean isEligibleForConsultation() {
        return active && status != PatientStatus.Expired;
    }

    public String getConsultationIneligibilityReason() {
        if (!active) {
            return "Patient is not active in the system.";
        }
        if (status == PatientStatus.Expired) {
            return "Cannot upload consultation results for expired patients.";
        }
        throw new IllegalStateException("This should not happen.");
    }
}
