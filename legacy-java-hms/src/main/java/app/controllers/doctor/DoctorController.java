package app.controllers.doctor;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.account.Account;
import app.models.permission.AclPermission;
import app.services.appointments.AppointmentService;
import app.services.doctor.ScheduleService;
import app.services.user.AccountService;
import spark.Request;
import spark.Response;

@RouteController(path = "/Hms/Doctors")
public final class DoctorController extends Controller {

    @Inject
    private AccountService accountService;

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private AppointmentService appointmentService;

    private Account getSelectedDoctor(Request request) {
        Long id;
        Account account;

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((account = accountService.getDoctorById(id)) != null) {
                return account;
            }
        }
        setSessionErrorMessage("Selected doctor does not exist", request);
        return null;
    }

    @Action(path = "/:id/Schedule", permission = AclPermission.ReadDoctors)
    public String getDoctorSchedule(Request request, Response response) {
        Model model;
        Account doctor;

        if ((doctor = getSelectedDoctor(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        doctor.setPassword(null);

        model = createModel(request);
        model.put("backToDoctors", true);
        model.put("listTitle", format("%s's schedule", doctor.fullname()));
        model.put("days", scheduleService.getDays(doctor.getId()));
        return renderView("doctors/schedule/list.html", model);
    }

    @Action(path = "/:id/Appointments", permission = AclPermission.ReadDoctors)
    public String getDoctorAppointments(Request request, Response response) {
        Model model;
        Account doctor;

        if ((doctor = getSelectedDoctor(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        doctor.setPassword(null);

        model = createModel(request);
        model.put("doctor", doctor);
        model.put("backToDoctors", true);
        model.put("listTitle", format("%s's upcoming appointments", doctor.fullname()));
        model.put("appointments", appointmentService.getActiveAppointmentsByDoctorId(doctor.getId()));
        return renderView("doctors/appointments/active-list.html", model);
    }


    @Action(path = "/", permission = AclPermission.ReadDoctors)
    public String getDoctors(Request request, Response response) {
        Model model;

        model = createModel(request);
        model.put("doctors", accountService.getDoctors());
        return renderView("doctors/list.html", model);
    }
}
