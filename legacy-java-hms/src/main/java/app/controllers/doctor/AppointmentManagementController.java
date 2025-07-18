package app.controllers.doctor;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.account.Account;
import app.models.appointments.Appointment;
import app.models.appointments.SelfBookedAppointment;
import app.models.doctor.schedule.Schedule;
import app.models.patient.PatientInfo;
import app.models.patient.PatientStatus;
import app.models.permission.AclPermission;
import app.services.appointments.AppointmentService;
import app.services.doctor.ScheduleService;
import app.services.messaging.MessagingService;
import app.services.patient.PatientService;
import app.util.DateUtils;
import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

/**
 * This controller is for doctors to manage (add, edit, delete, and cancel) their appointments.
 */
@RouteController(path = "/Hms/Appointments")
public class AppointmentManagementController extends Controller {

    @Inject
    private AppointmentService appointmentService;

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private PatientService patientService;

    @Inject
    private MessagingService messagingService;

    private String getAppointments(Request request, Appointment.Type type) {
        long id;
        Model model;

        model = createModel(request);
        id = getCurrentUser(request).getId();

        model.put("type", type);
        switch (type) {
            case Cancelled:
                model.put("listTitle", "Cancelled Appointments");
                model.put("appointments", appointmentService.getCancelledAppointmentsByDoctorId(id));
                break;
            case Completed:
                model.put("listTitle", "Completed Appointments");
                model.put("appointments", appointmentService.getCompletedAppointmentsByDoctorId(id));
                break;
            case Active:
                model.put("listTitle", "Upcoming Appointments");
                model.put("appointments", appointmentService.getActiveAppointmentsByDoctorId(id));
                break;
        }
        return renderView("doctors/appointments/list.html", model);
    }

    private Appointment getSelectedAppointment(Request request, long doctorId) {
        Long id;
        Appointment appointment;

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((appointment = appointmentService.getAppointmentById(id, doctorId)) != null) {
                return appointment;
            }
        }
        setSessionErrorMessage("Selected appointment does not exist.", request);
        return null;
    }

    @Action(path = "/:id/Details", permission = AclPermission.AccessScheduleModule)
    public String getAppointmentDetails(Request request, Response response) {
        Model model;
        Appointment appointment;

        model = new Model();

        response.type("application/json");
        if ((appointment = getSelectedAppointment(request, getCurrentUser(request).getId())) != null) {
            model.put("details", appointment.getDetails());
        } else {
            model.put("details", "This appointment does not exist.");
        }
        return getGson().toJson(model);
    }

    @Action(path = "/:id/Complete", permission = AclPermission.AccessScheduleModule)
    public String completeAppointment(Request request, Response response) {
        Appointment appointment;

        if ((appointment = getSelectedAppointment(request, getCurrentUser(request).getId())) != null) {
            if (appointment.isActive()) {
                appointment.setCompleted(true);
                appointment.setActive(false);
                appointmentService.updateAppointment(appointment);
                setSessionSuccessMessage("Appointment completed.", request);
            } else {
                setSessionErrorMessage("Cannot mark appointment as completed at this moment.", request);
            }
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Cancel", permission = AclPermission.AccessScheduleModule)
    public String cancelAppointment(Request request, Response response) {
        Account doctor;
        Appointment appointment;
        PatientInfo patientInfo;

        if ((appointment = getSelectedAppointment(request, getCurrentUser(request).getId())) != null) {
            if (appointment.isActive()) {
                doctor = getCurrentUser(request);
                patientInfo = patientService.findPatientInfoByMrn(appointment.getPatientMrn());

                appointment.setCancelled(true);
                appointment.setActive(false);
                appointment.setCancelReason(
                        format(
                                "Cancelled by %s on %s.", appointment.getDoctorName(),
                                LocaleUtil.formatDate(LocalDateTime.now())
                        )
                );

                appointmentService.updateAppointment(appointment);

                sendEmailToDoctor(
                        doctor,
                        "Appointment With Patient Cancelled",
                        format(
                                "You cancelled an appointment with patient %s (ID# %s) that was scheduled for %s.",
                                appointment.getPatientName(),
                                appointment.getPatientMrn(),
                                describeAppointmentTime(appointment)
                        )
                );

                sendEmailToPatient(
                        patientInfo,
                        "Doctor's Appointment Cancelled",
                        format(
                                "Your hospital appointment with %s (%s department) scheduled for %s was cancelled.",
                                appointment.getDoctorName(),
                                appointment.getDepartment(),
                                describeAppointmentTime(appointment)
                        )
                );

                setSessionSuccessMessage("Appointment cancelled.", request);
            } else {
                setSessionErrorMessage("Cannot cancel an inactive appointment.", request);
            }
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    private String describeAppointmentTime(Appointment appointment) {
        return format(
                "%s from %s to %s",
                LocaleUtil.formatDate(appointment.getStartTime().toLocalDate()),
                LocaleUtil.formatTime(appointment.getStartTime().toLocalTime()),
                LocaleUtil.formatTime(appointment.getEndTime().toLocalTime())
        );
    }

    @Action(path = "/", permission = AclPermission.AccessScheduleModule)
    public String getUpcoming(Request request, Response response) {
        return getAppointments(request, Appointment.Type.Active);
    }

    @Action(path = "/Completed", permission = AclPermission.AccessScheduleModule)
    public String getCompleted(Request request, Response response) {
        return getAppointments(request, Appointment.Type.Completed);
    }

    @Action(path = "/Cancelled", permission = AclPermission.AccessScheduleModule)
    public String getCancelled(Request request, Response response) {
        return getAppointments(request, Appointment.Type.Cancelled);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.AccessScheduleModule)
    public String addAppointment(Request request, Response response) {
        Model model;
        Account doctor;
        Schedule schedule;
        Appointment collision;
        PatientInfo patientInfo;
        LocalDateTime start, end;
        ValidationResults results;
        Appointment theAppointment;
        SelfBookedAppointment appointment;

        doctor = getCurrentUser(request);
        results = validate(SelfBookedAppointment.class, Options.defaults().sticky(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        appointment = results.getBean();
        if ((patientInfo = patientService.findPatientInfoByMrn(appointment.getMrn())) == null) {
            setSessionErrorMessage("Could not find patient by that ID.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if (patientInfo.getStatus() == PatientStatus.Expired) {
            setSessionErrorMessage("Cannot create appointment with this patient at this time.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if (!DateUtils.areTimesAtLeastTheseMinutesApart(appointment.getStartTime(), appointment.getEndTime(), 5L)) {
            setSessionErrorMessage("The duration of your appointment must be at least 5 minutes long.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if ((schedule = scheduleService.getScheduleById(appointment.getDate(), doctor.getId())) == null) {
            setSessionErrorMessage("You cannot make an appointment within the specified timeframe. " +
                    "Please check against your schedule.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if (schedule.isExpired() || !scheduleService.isAppointmentWithinSchedule(schedule, appointment)) {
            setSessionErrorMessage("You cannot make an appointment within the specified timeframe. " +
                    "Please check against your schedule.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        start = LocalDateTime.of(schedule.getStarts().toLocalDate(), appointment.getStartTime());
        end = LocalDateTime.of(schedule.getStarts().toLocalDate(), appointment.getEndTime());

        if ((collision = appointmentService.getCollidingAppointment(doctor.getId(), start, end)) != null) {
            setSessionErrorMessage(
                    format(
                            "Appointment time collides with an appointment scheduled <strong>%s</strong>.",
                            describeAppointmentTime(collision)
                    ),
                    request
            );
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        theAppointment = new Appointment();
        theAppointment.setSchedule(schedule.getId());
        theAppointment.setPatientId(patientInfo.getId());
        theAppointment.setDoctorId(doctor.getId());
        theAppointment.setStartTime(LocalDateTime.of(schedule.getStarts().toLocalDate(), appointment.getStartTime()));
        theAppointment.setEndTime(LocalDateTime.of(schedule.getStarts().toLocalDate(), appointment.getEndTime()));
        theAppointment.setCreatedBy(doctor.getId());
        theAppointment.setCreatedAt(LocalDateTime.now());
        theAppointment.setDetails(appointment.getNotes());
        theAppointment.setCancelled(false);
        theAppointment.setCompleted(false);

        appointmentService.addAppointment(theAppointment);

        sendEmailToDoctor(
                doctor,
                "Patient Appointment Scheduled",
                format(
                        "This is to notify you that you booked an appointment with %s (ID# %s) for %s from %s to %s.",
                        patientInfo.getFullName(),
                        appointment.getMrn(),
                        LocaleUtil.formatDate(start.toLocalDate()),
                        LocaleUtil.formatTime(start.toLocalTime()),
                        LocaleUtil.formatTime(end.toLocalTime())
                )
        );

        sendEmailToPatient(
                patientInfo,
                "Doctor Appointment Scheduled",
                format(
                        "This is to notify you that you have an appointment with %s (%s department) on %s from %s to %s.\n" +
                                "Please arrive early and bring a valid legal ID with you.\n\n" +
                                "For your reference, your ID/Medical Records Number in our system is %s.",
                        doctor.fullname(),
                        doctor.getDepartment(),
                        LocaleUtil.formatDate(start.toLocalDate()),
                        LocaleUtil.formatTime(start.toLocalTime()),
                        LocaleUtil.formatTime(end.toLocalTime()),
                        appointment.getMrn()
                )
        );

        setSessionSuccessMessage("Appointment successfully booked!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/New", permission = AclPermission.AccessScheduleModule)
    public String newAppointment(Request request, Response response) {
        return newAppointmentView(request, null);
    }

    private String newAppointmentView(Request request, Model model) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("dates", scheduleService.getActiveDoctorScheduledDays(getCurrentUser(request).getId()));
        return renderView("doctors/appointments/new.html", model);
    }

    private void sendEmailToDoctor(Account account, String subject, String message) {
        messagingService.sendEmail(account.getEmail(), account.getFullName(), subject, message, false);
    }

    private void sendEmailToPatient(PatientInfo patient, String subject, String message) {
        if (!LocaleUtil.isNullOrEmpty(patient.getEmail())) {
            messagingService.sendEmail(patient.getEmail(), patient.getFullName(), subject, message, false);
        }
    }
}
