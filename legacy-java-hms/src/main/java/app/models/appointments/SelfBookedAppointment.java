package app.models.appointments;

import lib.gintec_rdl.jbeava.validation.annotations.Filter;

import java.time.LocalTime;

public class SelfBookedAppointment {

    @Filter(label = "Patient ID", filters = {"trim", "required", "mrn"})
    private String mrn;

    @Filter(label = "Appointment date", filters = {"required", "long"})
    private long date;

    @Filter(label = "Start time", filters = {"required", "time(HH:mm)"})
    private LocalTime startTime;

    @Filter(label = "End time", filters = {"required", "time(HH:mm)"})
    private LocalTime endTime;

    @Filter(label = "Notes", filters = {"trim", "required", "length(3,500)"})
    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
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

    @Override
    public String toString() {
        return getStartTime() + " - " + getEndTime();
    }
}
