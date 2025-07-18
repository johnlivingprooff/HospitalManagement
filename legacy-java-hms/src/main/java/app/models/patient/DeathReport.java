package app.models.patient;

import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.validators.LocalDateValidator;
import app.core.validation.validators.LocalTimeValidator;
import app.core.validation.validators.Required;
import app.models.account.Sex;
import app.util.LocaleUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class DeathReport implements Validatable {
    private long id;
    private long patientId;
    private LocalDateTime dod;
    private Date dob;
    private LocalDateTime createdAt;
    private long createdBy;
    private String attachment;
    private Sex sex;

    @ValidationChain(label = "Date of death", filters = {@FilterNode(Required.class), @FilterNode(LocalDateValidator.class)})
    private LocalDate date;

    @ValidationChain(label = "Time of death", filters = {@FilterNode(Required.class), @FilterNode(LocalTimeValidator.class)})
    private LocalTime time;

    private String mrn;
    private String patient;
    private String reporter;

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

    public LocalDateTime getDod() {
        return dod;
    }

    public void setDod(LocalDateTime dod) {
        this.dod = dod;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return patient + ", " + LocaleUtil.formatDate(dob, false) + " to " + LocaleUtil.formatDate(dod);
    }
}
