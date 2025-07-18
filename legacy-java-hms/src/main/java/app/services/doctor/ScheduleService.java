package app.services.doctor;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.appointments.SelfBookedAppointment;
import app.models.doctor.schedule.Schedule;
import app.models.doctor.schedule.ScheduledDays;
import app.util.DateUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ServiceDescriptor
public class ScheduleService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public ScheduleService(Configuration configuration) {
        super(configuration);
    }

    public void addSingleScheduleDay(Schedule schedule) {
        String sql = "select * from addSingleScheduleDay(:doctorId, :starts, :ends)";
        schedule.setId(executeSelect(connection ->
                bindParameters(connection.createQuery(sql), schedule).executeAndFetchFirst(Long.class)));
    }

    public void addMultipleScheduleDays(Schedule schedule) {
        String sql = "select * from addMultipleScheduleDays(:doctorId, :startDate, :endDate, :startTime, :endTime)";
        schedule.setId(executeSelect(connection ->
                bindParameters(connection.createQuery(sql), schedule).executeAndFetchFirst(Long.class)));
    }

    public List<Schedule> getDays(long doctorId) {
        return executeSelect(connection -> connection
                .createQuery("select * from schedules_v where doctor_id = :doctorId and not expired order by starts asc, ends asc")
                .addParameter("doctorId", doctorId)
                .executeAndFetch(Schedule.class));
    }

    public Schedule getCollidingSchedule(Schedule schedule) {
        String sql = "select * from schedules_v " +
                "where doctor_id = :doctorId " +
                "and not expired " +
                "and (:starts between starts and ends) or (:ends between starts and ends) " +
                "limit 1";
        return executeSelect(
                connection -> bindParameters(
                        connection.createQuery(sql), schedule).executeAndFetchFirst(Schedule.class)
        );
    }

    public boolean doesScheduleHasAppointmentReference(Schedule schedule) {
        String sql = "select exists (select 1 from active_appointments_v " +
                "where schedule = :schedule and doctor_id = :doctorId limit 1)";
        return executeSelect(connection -> connection.createQuery(sql)
                .addParameter("schedule", schedule.getId())
                .addParameter("doctorId", schedule.getDoctorId())
                .executeAndFetchFirst(Boolean.class));
    }

    public void deleteSchedule(Schedule schedule) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            bindParameters(
                    connection.createQuery("update appointments set schedule = null where schedule = :id"),
                    schedule
            ).executeUpdate();
            bindParameters(
                    connection.createQuery("delete from schedules where id = :id and doctor_id = :doctorId"),
                    schedule
            ).executeUpdate();
            return null;
        });
    }

    public Schedule getScheduleById(long id, long doctorId) {
        return executeSelect(connection -> connection
                .createQuery("select * from schedules_v where id = :id and doctor_id = :doctorId")
                .addParameter("id", id)
                .addParameter("doctorId", doctorId)
                .executeAndFetchFirst(Schedule.class));
    }

    public void cleanExpiredDates() {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("delete from schedules where end_date < current_timestamp");
            return null;
        });
    }

    @Deprecated(forRemoval = true)
    public List<ScheduledDays> getDoctorScheduledDays(long doctorId) {
        String sql = "with t as (select *, start_date::date::timestamptz as date from schedules where doctor_id = :doctorId) " +
                "select id, date from t group by id, date order by date asc";
        return executeSelect(connection -> connection
                .createQuery(sql)
                .addParameter("doctorId", doctorId)
                .executeAndFetch(ScheduledDays.class));
    }

    public List<ScheduledDays> getActiveDoctorScheduledDays(long doctorId) {
        String sql = "with t as (select *, starts::date::timestamptz as date from schedules_v where doctor_id = :doctorId and not expired) " +
                "select id, date from t group by id, date order by date asc";
        return executeSelect(connection -> connection
                .createQuery(sql)
                .addParameter("doctorId", doctorId)
                .executeAndFetch(ScheduledDays.class));
    }

    public boolean isAvailableWithinTimeFrame(long doctorId, LocalDateTime start, LocalDateTime end) {
        String sql = "select exists (" +
                "   select * from schedules_v " +
                "       where doctor_id = :doctorId " +
                "           and not expired " +
                "           and (:starts between starts and ends) and (:ends between starts and ends) " +
                "       limit 1" +
                ")";
        return executeSelect(connection -> connection
                .createQuery(sql)
                .addParameter("doctorId", doctorId)
                .addParameter("starts", start)
                .addParameter("ends", end)
                .executeAndFetchFirst(Boolean.class));
    }

    public boolean isAppointmentWithinSchedule(Schedule schedule, SelfBookedAppointment appointment) {
        LocalTime scheduleStartTime, scheduleEndTime;
        LocalTime meetStartTime, meetEndTime;

        scheduleStartTime = schedule.getStarts().toLocalTime();
        scheduleEndTime = schedule.getEnds().toLocalTime();

        meetStartTime = appointment.getStartTime();
        meetEndTime = appointment.getEndTime();

        return DateUtils.isTimeWithinRange(meetStartTime, scheduleStartTime, scheduleEndTime)
                && DateUtils.isTimeWithinRange(meetEndTime, scheduleStartTime, scheduleEndTime);
    }
}
