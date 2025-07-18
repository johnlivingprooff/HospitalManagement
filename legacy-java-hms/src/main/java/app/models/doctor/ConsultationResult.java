package app.models.doctor;

import lib.gintec_rdl.jbeava.validation.annotations.Filter;

import java.time.LocalDateTime;

public class ConsultationResult {
    private long id;
    @Filter(label = "Summary notes", filters = {"trim", "required", "length(1,2048)"})
    private String notes;
    private long performedBy;
    private long patientId;

    @Filter(label = "Consultation", filters = {"required", "long"})
    private long procedureId;
    private String attachment;
    private LocalDateTime createdAt;

    private String department;

    @Filter(
            label = "Patient MRN",
            name = "mrn",
            filters = {"trim", "required", "mrn"}
    )
    private String patientMrn;
    private String performer;
    private String patientName;
    private String procedureName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(long performedBy) {
        this.performedBy = performedBy;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public long getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(long procedureId) {
        this.procedureId = procedureId;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPatientMrn() {
        return patientMrn;
    }

    public void setPatientMrn(String patientMrn) {
        this.patientMrn = patientMrn;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public String toString() {
        return "{id=" + id + ", procedureId=" + procedureId + "}";
    }
}
