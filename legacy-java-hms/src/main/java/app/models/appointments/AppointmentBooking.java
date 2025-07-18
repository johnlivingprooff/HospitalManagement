package app.models.appointments;

import lib.gintec_rdl.jbeava.validation.annotations.Filter;

public class AppointmentBooking extends SelfBookedAppointment {

    @Filter(label = "Doctor", filters = {"required", "long"})
    private long doctorId;

    public long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(long doctorId) {
        this.doctorId = doctorId;
    }
}
