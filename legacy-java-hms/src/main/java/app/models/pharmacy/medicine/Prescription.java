package app.models.pharmacy.medicine;

import app.models.Contexts;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

import java.time.LocalDateTime;

public class Prescription {
    public enum Status {
        Filed,
        Pending,
        Dispensed
    }

    @Filter(label = "Prescription id", filters = {"required", "long"}, contexts = {Contexts.FIND, Contexts.UPDATE})
    private long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long patientId;
    private long drugs;
    private long expiringDrugs;

    @Filter(name = "mrn", label = "Patient MRN", filters = {"trim", "required", "length(5,20)", "upper", "mrn"}, contexts = {Contexts.CREATE})
    private String patientMrn;
    private long filerId;
    private long updaterId;
    private Status status;
    private boolean deleted;

    private String patientName;
    private String filedBy;
    private String updatedBy;

    private String department;

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getFiledBy() {
        return filedBy;
    }

    public void setFiledBy(String filedBy) {
        this.filedBy = filedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public long getFilerId() {
        return filerId;
    }

    public void setFilerId(long filerId) {
        this.filerId = filerId;
    }

    public long getUpdaterId() {
        return updaterId;
    }

    public void setUpdaterId(long updaterId) {
        this.updaterId = updaterId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getPatientMrn() {
        return patientMrn;
    }

    public void setPatientMrn(String patientMrn) {
        this.patientMrn = patientMrn;
    }

    public long getDrugs() {
        return drugs;
    }

    public void setDrugs(long drugs) {
        this.drugs = drugs;
    }

    public long getExpiringDrugs() {
        return expiringDrugs;
    }

    public void setExpiringDrugs(long expiringDrugs) {
        this.expiringDrugs = expiringDrugs;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return " {id=" + getId() + "}";
    }
}
