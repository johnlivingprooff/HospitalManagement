package app.models.admissions;

import app.models.Contexts;
import app.util.DateUtils;
import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Admission {

    @Filter(label = "Admission id", filters = {"required", "long"}, contexts = {Contexts.FIND, Contexts.UPDATE})
    private long id;
    private long patientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dischargedAt;

    @Filter(label = "Admission date", filters = {"required", "date(yyyy-MM-dd)"}, contexts = {Contexts.CREATE})
    private LocalDate admissionDate;

    // We'll manually combine date and time then compare to current
    @Filter(
            label = "Admission time", filters = {"required", "time(HH:mm)"},
            message = "${name} is not a properly formatted time.",
            contexts = {Contexts.CREATE}
    )
    private LocalTime admissionTime;

    @Filter(
            label = "Termination date", filters = {"required", "date(yyyy-MM-dd)"},
            contexts = {Contexts.UPDATE}
    )
    private LocalDate terminationDate;

    @Filter(
            label = "Termination time", filters = {"required", "time(HH:mm)"},
            message = "${name} is not a properly formatted time.",
            contexts = {Contexts.UPDATE}
    )
    private LocalTime terminationTime;

    @Filter(
            label = "Reason for admission",
            filters = {"trim", "required", "length(1,500)"},
            contexts = {Contexts.CREATE}
    )
    private String reason;
    private String attachment;

    @Filter(
            label = "Reason for termination", filters = {"trim", "required", "length(1,1024)"},
            contexts = {Contexts.UPDATE}
    )
    private String terminationReason;
    private String terminationAttachment;
    private long terminatedBy;

    @Filter(label = "Bed id", filters = {"required", "long"}, contexts = {Contexts.CREATE})
    private long bedId;

    @Filter(label = "Ward", filters = {"required", "long"}, contexts = {Contexts.CREATE})
    private long wardId;
    private long admittedBy;

    @Filter(
            label = "Admission type",
            filters = {"required", "enum(app.models.admissions.AdmissionType)"},
            contexts = {Contexts.CREATE}
    )
    private AdmissionType admissionType;
    private AdmissionStatus status;

    @Filter(label = "Patient MRN", name = "mrn", filters = {"trim", "required", "mrn"}, contexts = {Contexts.CREATE})
    private String patientMrn;
    private String patientName;
    private String terminatorName;
    private String wardCode;
    private String wardName;
    private String bed;

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

    public LocalDateTime getDischargedAt() {
        return dischargedAt;
    }

    public void setDischargedAt(LocalDateTime dischargedAt) {
        this.dischargedAt = dischargedAt;
    }

    public long getBedId() {
        return bedId;
    }

    public void setBedId(long bedId) {
        this.bedId = bedId;
    }

    public long getAdmittedBy() {
        return admittedBy;
    }

    public void setAdmittedBy(long admittedBy) {
        this.admittedBy = admittedBy;
    }

    public AdmissionType getAdmissionType() {
        return admissionType;
    }

    public void setAdmissionType(AdmissionType admissionType) {
        this.admissionType = admissionType;
    }

    public AdmissionStatus getStatus() {
        return status;
    }

    public void setStatus(AdmissionStatus status) {
        this.status = status;
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

    public String getWardCode() {
        return wardCode;
    }

    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getBed() {
        return bed;
    }

    public void setBed(String bed) {
        this.bed = bed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public LocalTime getAdmissionTime() {
        return admissionTime;
    }

    public void setAdmissionTime(LocalTime admissionTime) {
        this.admissionTime = admissionTime;
    }

    public long getWardId() {
        return wardId;
    }

    public void setWardId(long wardId) {
        this.wardId = wardId;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public LocalTime getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(LocalTime terminationTime) {
        this.terminationTime = terminationTime;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public String getTerminationAttachment() {
        return terminationAttachment;
    }

    public void setTerminationAttachment(String terminationAttachment) {
        this.terminationAttachment = terminationAttachment;
    }

    public long getTerminatedBy() {
        return terminatedBy;
    }

    public void setTerminatedBy(long terminatedBy) {
        this.terminatedBy = terminatedBy;
    }

    public String getTerminatorName() {
        return terminatorName;
    }

    public void setTerminatorName(String terminatorName) {
        this.terminatorName = terminatorName;
    }

    public String getDuration() {
        return DateUtils.duration(createdAt, dischargedAt);
    }
}
