package app.models.patient.visit;

import java.util.Date;

/**
 * Patient visit under which all activities will be
 */
public class Visit {

    enum VisitType {
        Appointment("Appointment"),
        WalkIn("Walk In");

        VisitType(String description) {
            this.description = description;
        }

        public final String description;
    }

    private long id;
    private long patientId;
    private long accountId;
    private Date exited;
    private Date entered;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public Date getExited() {
        return exited;
    }

    public void setExited(Date exited) {
        this.exited = exited;
    }

    public Date getEntered() {
        return entered;
    }

    public void setEntered(Date entered) {
        this.entered = entered;
    }
}
