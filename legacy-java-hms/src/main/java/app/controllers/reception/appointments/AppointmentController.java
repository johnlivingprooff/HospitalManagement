package app.controllers.reception.appointments;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.account.Account;
import app.models.appointments.Appointment;
import app.models.appointments.AppointmentBooking;
import app.models.doctor.schedule.Schedule;
import app.models.patient.PatientInfo;
import app.models.patient.PatientStatus;
import app.models.permission.AclPermission;
import app.services.appointments.AppointmentService;
import app.services.doctor.ScheduleService;
import app.services.messaging.MessagingService;
import app.services.patient.PatientService;
import app.services.user.AccountService;
import app.util.DateUtils;
import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;
import java.util.List;

@RouteController(path = "/Hms/Reception/Appointments")
public class AppointmentController extends Controller {
    @Inject
    private AppointmentService appointmentService;

    @Inject
    private AccountService accountService;

    @Inject
    private PatientService patientService;

    @Inject
    private MessagingService messagingService;

    @Inject
    private ScheduleService scheduleService;

    private Appointment getSelectedAppointment(Request request) {
        Long id;
        Appointment appointment;

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((appointment = appointmentService.getAppointmentById(id)) != null) {
                return appointment;
            }
        }
        setSessionErrorMessage("Selected appointment does not exist.", request);
        return null;
    }

    private String getAppointments(Request request, Appointment.Type type) {
        Model model;
        model = createModel(request);
        switch (type) {
            case Active:
                model.put("appointments", appointmentService.getActiveAppointments());
                break;
            case Cancelled:
                model.put("appointments", appointmentService.getCancelledAppointments());
                break;
            case Completed:
                model.put("appointments", appointmentService.getCompletedAppointments());
                break;
        }
        model.put("type", type);
        model.put("listTitle", type.title);
        return renderView("reception/appointments/list.html", model);
    }

    @Action(path = "/GetDoctorsSchedule/:doctor-id", permission = AclPermission.AccessReception)
    public String getDoctorsSchedule(Request request, Response response) {
        Long doctor;
        List<Schedule> list;

        response.type("application/json");
        if ((doctor = getNumericQueryParameter(request, "doctor-id", Long.class)) != null) {
            list = scheduleService.getDays(doctor);
        } else {
            list = List.of();
        }

        return getGson().toJson(list);
    }

    @Action(path = "/:id/Cancel", permission = AclPermission.AccessReception)
    public String cancelAppointment(Request request, Response response) {
        Account doctor;
        Account me;
        Appointment appointment;
        PatientInfo patientInfo;

        if ((appointment = getSelectedAppointment(request)) != null) {
            if (appointment.isActive()) {
                me = getCurrentUser(request);
                doctor = accountService.getDoctorById(appointment.getDoctorId());
                patientInfo = patientService.findPatientInfoByMrn(appointment.getPatientMrn());

                appointment.setCancelled(true);
                appointment.setActive(false);
                appointment.setCancelReason(
                        format(
                                "Cancelled by %s on %s.", me.fullname(),
                                LocaleUtil.formatDate(LocalDateTime.now())
                        )
                );

                appointmentService.updateAppointment(appointment);

                sendEmailToDoctor(
                        doctor,
                        "Appointment With Patient Cancelled",
                        format(
                                "Your appointment with patient %s (ID# %s) that was scheduled for %s was cancelled.",
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

    @Action(path = "/:id/Details", permission = AclPermission.AccessReception)
    public String getAppointmentDetails(Request request, Response response) {
        Model model;
        Appointment appointment;

        model = new Model();

        response.type("application/json");
        if ((appointment = getSelectedAppointment(request)) != null) {
            model.put("details", appointment.getDetails());
        } else {
            model.put("details", "This appointment does not exist.");
        }
        return getGson().toJson(model);
    }

    @Action(path = "/", permission = AclPermission.AccessReception)
    public String getActiveAppointments(Request request, Response response) {
        return getAppointments(request, Appointment.Type.Active);
    }

    @Action(path = "/Cancelled", permission = AclPermission.AccessReception)
    public String getCancelledAppointments(Request request, Response response) {
        return getAppointments(request, Appointment.Type.Cancelled);
    }

    @Action(path = "/Completed", permission = AclPermission.AccessReception)
    public String getCompletedAppointments(Request request, Response response) {
        return getAppointments(request, Appointment.Type.Completed);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.AccessReception)
    public String addAppointment(Request request, Response response) {
        Model model;
        Account doctor;
        Schedule schedule;
        Account receptionist;
        Appointment collision;
        PatientInfo patientInfo;
        LocalDateTime start, end;
        ValidationResults results;
        Appointment theAppointment;
        AppointmentBooking appointment;

        results = validate(AppointmentBooking.class, Options.defaults().sticky(true).depth(2), request);

        if (!results.success()) {
            model = createModel(request);
            model.put("error", true);
            model.put("autoSelect", true);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        appointment = results.getBean();
        receptionist = getCurrentUser(request);

        if ((doctor = accountService.getDoctorById(appointment.getDoctorId())) == null) {
            setSessionErrorMessage("Selected doctor does not exist.", request);
            model = createModel(request);
            model.put("error", true);
            model.put("autoSelect", true);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if ((patientInfo = patientService.findPatientInfoByMrn(appointment.getMrn())) == null) {
            setSessionErrorMessage("Could not find patient by that ID.", request);
            model = createModel(request);
            model.put("error", true);
            model.put("autoSelect", true);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if (patientInfo.getStatus() == PatientStatus.Expired) {
            setSessionErrorMessage("Cannot create appointment with this patient at this time.", request);
            model = createModel(request);
            model.put("error", true);
            model.put("autoSelect", true);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if (!DateUtils.areTimesAtLeastTheseMinutesApart(appointment.getStartTime(), appointment.getEndTime(), 5L)) {
            setSessionErrorMessage("The duration of appointment must be at least 5 minutes long.", request);
            model = createModel(request);
            model.put("error", true);
            model.put("autoSelect", true);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if ((schedule = scheduleService.getScheduleById(appointment.getDate(), doctor.getId())) == null) {
            setSessionErrorMessage("You cannot make an appointment within the specified timeframe. " +
                    "Please check against this doctor's schedule.", request);
            model = createModel(request);
            model.put("error", true);
            model.put("autoSelect", true);
            copyRawPostDataToModel(model, results);
            return newAppointmentView(request, model);
        }

        if (schedule.isExpired() || !scheduleService.isAppointmentWithinSchedule(schedule, appointment)) {
            setSessionErrorMessage("You cannot make an appointment within the specified timeframe. " +
                    "Please check against this doctor's schedule.", request);
            model = createModel(request);
            model.put("error", true);
            model.put("autoSelect", true);
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
            model.put("error", true);
            model.put("autoSelect", true);
            return newAppointmentView(request, model);
        }

        theAppointment = new Appointment();
        theAppointment.setSchedule(schedule.getId());
        theAppointment.setPatientId(patientInfo.getId());
        theAppointment.setDoctorId(doctor.getId());
        theAppointment.setStartTime(LocalDateTime.of(schedule.getStarts().toLocalDate(), appointment.getStartTime()));
        theAppointment.setEndTime(LocalDateTime.of(schedule.getStarts().toLocalDate(), appointment.getEndTime()));
        theAppointment.setCreatedBy(receptionist.getId());
        theAppointment.setCreatedAt(LocalDateTime.now());
        theAppointment.setDetails(appointment.getNotes());
        theAppointment.setCancelled(false);
        theAppointment.setCompleted(false);

        appointmentService.addAppointment(theAppointment);

        sendEmailToDoctor(
                doctor,
                "Patient Appointment Scheduled",
                format(
                        "This is to notify you that you have an appointment with patient %s (ID# %s) for %s from %s to %s.",
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

    @Action(path = "/New", permission = AclPermission.AccessReception)
    public String newAppointment(Request request, Response response) {
        return newAppointmentView(request, null);
    }

    private String newAppointmentView(Request request, Model model) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("dates", List.of());
        model.put("doctors", accountService.getAvailableDoctors());
        return renderView("reception/appointments/new.html", model);
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
