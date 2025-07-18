package app.services.appointments;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.appointments.Appointment;
import app.models.patient.Patient;

import java.time.LocalDateTime;
import java.util.List;

@ServiceDescriptor
public class AppointmentService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public AppointmentService(Configuration configuration) {
        super(configuration);
    }

    public List<Appointment> getActiveAppointments() {
        return executeSelect(connection -> connection
                .createQuery("select * from active_appointments_v order by start_time asc")
                .executeAndFetch(Appointment.class));
    }

    public List<Appointment> getCancelledAppointments() {
        return executeSelect(connection -> connection
                .createQuery("select * from cancelled_appointments_v order by start_time asc")
                .executeAndFetch(Appointment.class));
    }

    public List<Appointment> getCompletedAppointments() {
        return executeSelect(connection -> connection
                .createQuery("select * from completed_appointments_v order by start_time asc")
                .executeAndFetch(Appointment.class));
    }

    public List<Appointment> getActiveAppointmentsByDoctorId(long doctorId) {
        return executeSelect(connection -> connection
                .createQuery("select * from active_appointments_v where doctor_id = :doctor_id order by start_time asc")
                .addParameter("doctor_id", doctorId)
                .executeAndFetch(Appointment.class));
    }

    public List<Appointment> getCancelledAppointmentsByDoctorId(long doctorId) {
        return executeSelect(connection -> connection
                .createQuery("select * from cancelled_appointments_v where doctor_id = :doctor_id order by start_time desc")
                .addParameter("doctor_id", doctorId)
                .executeAndFetch(Appointment.class));
    }

    public List<Appointment> getCompletedAppointmentsByDoctorId(long doctorId) {
        return executeSelect(connection -> connection
                .createQuery("select * from completed_appointments_v where doctor_id = :doctor_id order by start_time desc")
                .addParameter("doctor_id", doctorId)
                .executeAndFetch(Appointment.class));
    }

    public void addAppointment(Appointment appointment) {
        appointment.setId(executeUpdate(connection -> {
            String sql;
            sql = "insert into appointments(patient_id, doctor_id, start_time, end_time, created_at, " +
                    "cancelled, completed, details, created_by, schedule) values " +
                    "(:patient_id,:doctor_id,:start_time,:end_time,:created_at,:cancelled,:completed,:details,:created_by, :schedule)";
            return connection.createQuery(sql)
                    .addParameter("patient_id", appointment.getPatientId())
                    .addParameter("doctor_id", appointment.getDoctorId())
                    .addParameter("start_time", appointment.getStartTime())
                    .addParameter("end_time", appointment.getEndTime())
                    .addParameter("created_at", appointment.getCreatedAt())
                    .addParameter("cancelled", appointment.isCancelled())
                    .addParameter("completed", appointment.isCompleted())
                    .addParameter("details", appointment.getDetails())
                    .addParameter("created_by", appointment.getCreatedBy())
                    .addParameter("schedule", appointment.getSchedule())
                    .executeUpdate()
                    .getKey(Long.class);
        }));
    }

    public void updateAppointment(Appointment appointment) {
        appointment.setId(executeUpdate(connection -> {
            String sql;
            sql = "update appointments set start_time = :start_time, end_time = :end_time, cancelled = :cancelled, " +
                    "completed = :completed, details = :details, cancel_reason = :cancel_reason " +
                    "where id = :id and doctor_id = :doctor_id";
            return connection.createQuery(sql)
                    .addParameter("id", appointment.getId())
                    .addParameter("doctor_id", appointment.getDoctorId())
                    .addParameter("start_time", appointment.getStartTime())
                    .addParameter("end_time", appointment.getEndTime())
                    .addParameter("cancelled", appointment.isCancelled())
                    .addParameter("completed", appointment.isCompleted())
                    .addParameter("details", appointment.getDetails())
                    .addParameter("cancel_reason", appointment.getCancelReason())
                    .executeUpdate()
                    .getKey(Long.class);
        }));
    }

    public Appointment getCollidingAppointment(long doctorId, LocalDateTime start, LocalDateTime end) {
        String sql = "select * from appointments_v " +
                "where doctor_id = :doctorId " +
                "and active = true " +
                "and ((:starts between start_time and end_time) or (:ends between start_time and end_time)) limit 1";
        return executeSelect(connection -> connection
                .createQuery(sql)
                .addParameter("doctorId", doctorId)
                .addParameter("starts", start)
                .addParameter("ends", end)
                .executeAndFetchFirst(Appointment.class));
    }

    public Appointment getAppointmentById(long id, long doctorId) {
        return executeSelect(connection -> connection
                .createQuery("select * from appointments_v where doctor_id = :doctor_id and id = :id")
                .addParameter("doctor_id", doctorId)
                .addParameter("id", id)
                .executeAndFetchFirst(Appointment.class));
    }

    public Appointment getAppointmentById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from appointments_v where id = :id")
                .addParameter("id", id)
                .executeAndFetchFirst(Appointment.class));
    }

    public void cancelMissedAppointments() {
        executeUpdate(connection -> {
            connection.createQuery("update appointments " +
                    "set cancelled = true, " +
                    "cancel_reason = 'Missed appointment cancelled by system.' " +
                    "where (not cancelled and not completed) and current_timestamp > end_time")
                    .executeUpdate();
            return null;
        });
    }

    private List<Appointment> getPatientAppointments(Patient patient, Appointment.Type type) {
        StringBuilder builder = new StringBuilder("select * from appointments_v where patient_id = :patientId ");

        switch (type) {
            case Completed:
                builder.append("and completed");
                break;
            case Cancelled:
                builder.append("and cancelled");
                break;
            case Active:
                builder.append("and active");
                break;
            default:
                throw new IllegalArgumentException("");
        }

        builder.append(" order by start_time desc");

        return executeSelect(connection -> connection
                .createQuery(builder.toString())
                .addParameter("patientId", patient.getId())
                .executeAndFetch(Appointment.class));
    }

    public List<Appointment> getActivePatientAppointments(Patient patient) {
        return getPatientAppointments(patient, Appointment.Type.Active);
    }

    public List<Appointment> getCancelledPatientAppointments(Patient patient) {
        return getPatientAppointments(patient, Appointment.Type.Cancelled);
    }

    public List<Appointment> getCompletedPatientAppointments(Patient patient) {
        return getPatientAppointments(patient, Appointment.Type.Completed);
    }
}
