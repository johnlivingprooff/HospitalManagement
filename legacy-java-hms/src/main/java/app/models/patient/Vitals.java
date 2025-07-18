package app.models.patient;

import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.validators.*;

import java.time.LocalDateTime;

public class Vitals implements Validatable {
    private long id;
    private long patientId;
    private long createdBy;

    private long admissionId;

    @ValidationChain(label = "Heart rate", filters = {@FilterNode(Required.class), @FilterNode(IntValidator.class), @FilterNode(value = Range.class, parameters = {"0", "500"})})
    private int pulse;

    @ValidationChain(label = "Respiratory rate", filters = {@FilterNode(Required.class), @FilterNode(IntValidator.class), @FilterNode(value = Range.class, parameters = {"0", "500"})})
    private int breaths;

    @ValidationChain(label = "Systolic pressure", filters = {@FilterNode(Required.class), @FilterNode(IntValidator.class), @FilterNode(value = Range.class, parameters = {"0", "500"})})
    private int systolic;

    @ValidationChain(label = "Diastolic pressure", filters = {@FilterNode(Required.class), @FilterNode(IntValidator.class), @FilterNode(value = Range.class, parameters = {"0", "500"})})
    private int diastolic;

    @ValidationChain(label = "Body temperature", filters = {@FilterNode(Required.class), @FilterNode(DoubleValidator.class), @FilterNode(value = Range.class, parameters = {"0", "500"})})
    private double temperature;

    private VitalsType type;

    private LocalDateTime created;

    private String examiner;

    @ValidationChain(label = "Temperature units", filters = {@FilterNode(Required.class), @FilterNode(TempUnit.class)})
    private TemperatureUnits tempUnits;

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

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public int getPulse() {
        return pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    public int getBreaths() {
        return breaths;
    }

    public void setBreaths(int breaths) {
        this.breaths = breaths;
    }

    public int getSystolic() {
        return systolic;
    }

    public void setSystolic(int systolic) {
        this.systolic = systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(int diastolic) {
        this.diastolic = diastolic;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getExaminer() {
        return examiner;
    }

    public void setExaminer(String examiner) {
        this.examiner = examiner;
    }

    public VitalsType getType() {
        return type;
    }

    public void setType(VitalsType type) {
        this.type = type;
    }

    public long getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(long admissionId) {
        this.admissionId = admissionId;
    }

    public TemperatureUnits getTempUnits() {
        return tempUnits;
    }

    public void setTempUnits(TemperatureUnits tempUnits) {
        this.tempUnits = tempUnits;
    }
}
