package app.models.patient;

import app.core.validation.FilterNode;
import app.core.validation.ValidationChain;
import app.core.validation.validators.*;
import app.models.account.Sex;
import validators.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Birth extends Vitals {
    private String patient;
    private String registeredBy;
    private LocalDateTime dob;

    @ValidationChain(label = "Child's name",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"1", "200"})
            }
    )
    private String name;

    @ValidationChain(
            label = "Sex",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Gender.class)
            }
    )
    private Sex sex;

    @ValidationChain(label = "Date of birth", filters = {@FilterNode(Required.class), @FilterNode(LocalDateValidator.class)})
    private LocalDate date;

    @ValidationChain(label = "Time of birth", filters = {@FilterNode(Required.class), @FilterNode(LocalTimeValidator.class)})
    private LocalTime time;

    @ValidationChain(label = "Length", filters = {@FilterNode(Required.class), @FilterNode(DoubleValidator.class), @FilterNode(value = Range.class, parameters = {"1", "500"})})
    private double height;

    @ValidationChain(label = "Weight", filters = {@FilterNode(Required.class), @FilterNode(DoubleValidator.class), @FilterNode(value = Range.class, parameters = {"1", "500"})})
    private double weight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public LocalDateTime getDob() {
        return dob;
    }

    public void setDob(LocalDateTime dob) {
        this.dob = dob;
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

    public String getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }
}
