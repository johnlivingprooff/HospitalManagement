package app.models.doctor.schedule;

import app.core.annotations.HtmlFieldDisplay;
import app.util.DateUtils;
import app.util.LocaleUtil;
import com.google.gson.annotations.SerializedName;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@HtmlFieldDisplay(value = "getId()")
public class Schedule {

    @SerializedName("")
    private long id;
    private long doctorId;
    private boolean expired;
    private LocalDateTime starts;
    private LocalDateTime ends;
    private String doctorName;

    @Filter(label = "Start date", filters = {"required", "date(yyyy-MM-dd)"})
    private LocalDate startDate;

    @Filter(label = "End date", filters = {"date(yyyy-MM-dd)"})
    private LocalDate endDate;

    @Filter(label = "Start time", filters = {"required", "time(HH:mm)"})
    private LocalTime startTime;

    @Filter(label = "End time", filters = {"required","time(HH:mm)"})
    private LocalTime endTime;

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(long doctorId) {
        this.doctorId = doctorId;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public LocalDateTime getStarts() {
        return starts;
    }

    public void setStarts(LocalDateTime starts) {
        this.starts = starts;
    }

    public LocalDateTime getEnds() {
        return ends;
    }

    public void setEnds(LocalDateTime ends) {
        this.ends = ends;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String toString() {
        return LocaleUtil.formatDate(starts, false) + ", " + getStartingTime() + " - " + getEndingTime();
    }

    public String getStartingTime() {
        return starts.toLocalTime().toString();
    }

    public String getEndingTime() {
        return ends.toLocalTime().toString();
    }

    public String getDuration() {
        return DateUtils.duration(starts.toLocalTime(), ends.toLocalTime());
    }
}
