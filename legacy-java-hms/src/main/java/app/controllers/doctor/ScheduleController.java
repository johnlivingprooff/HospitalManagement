package app.controllers.doctor;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.Subject;
import app.models.doctor.schedule.Schedule;
import app.models.permission.AclPermission;
import app.services.doctor.ScheduleService;
import app.util.DateUtils;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Schedules")
public class ScheduleController extends Controller {

    @Inject
    private ScheduleService scheduleService;

    private Schedule getSelectedSchedule(Request request) {
        Long id;
        Schedule schedule;

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((schedule = scheduleService.getScheduleById(id, getCurrentUser(request).getId())) != null) {
                return schedule;
            }
        }
        setSessionErrorMessage("Selected day not found.", request);
        return null;
    }

    @Action(path = "/", permission = AclPermission.AccessScheduleModule)
    public String getMySchedule(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("listTitle", "My Schedule");
        model.put("days", scheduleService.getDays(getCurrentUser(request).getId()));
        return renderView("doctors/schedule/list.html", model);
    }

    @Action(path = "/:id/Delete", permission = AclPermission.AccessScheduleModule)
    public String deleteSchedule(Request request, Response response) {
        Schedule schedule;

        if ((schedule = getSelectedSchedule(request)) != null) {
            if (!scheduleService.doesScheduleHasAppointmentReference(schedule)) {
                scheduleService.deleteSchedule(schedule);
                setSessionSuccessMessage("Day removed from schedule.", request);
            } else {
                setSessionErrorMessage("You cannot delete this day from your schedule because you have appointments " +
                        "scheduled on the same day.<br ><br >Cancel any appointments scheduled on this day first.", request);
            }
        }

        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/NewByRange", permission = AclPermission.AccessScheduleModule)
    public String newScheduleByRange(Request request, Response response) {
        return newScheduleView(request, null, true);
    }

    @Action(path = "/New", permission = AclPermission.AccessScheduleModule)
    public String newMySchedule(Request request, Response response) {
        return newScheduleView(request, null, false);
    }

    private String newScheduleView(Request request, Model model, boolean byRange) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("range", byRange);
        return renderView("doctors/schedule/new.html", model);
    }

    private String addSchedule(Request request, Response response, boolean range) {
        Model model;
        Subject subject;
        Schedule schedule;
        Schedule collision;
        LocalDateTime today;
        ValidationResults results;

        subject = getCurrentSubject(request);

        results = validate(Schedule.class, Options.defaults().sticky(true).map(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return newScheduleView(request, model, range);
        }

        schedule = results.getBean();

        if (range) {
            if (schedule.getEndDate() == null) {
                setSessionErrorMessage("End date is required when specifying multiple days.", request);
                model = createModel(request);
                copyRawPostDataToModel(model, results);
                return newScheduleView(request, model, true);
            }
        } else {
            schedule.setEndDate(schedule.getStartDate());
        }

        schedule.setDoctorId(subject.getId());
        schedule.setEnds(LocalDateTime.of(schedule.getEndDate(), schedule.getEndTime()));
        schedule.setStarts(LocalDateTime.of(schedule.getStartDate(), schedule.getStartTime()));

        today = LocalDateTime.now();

        if (schedule.getStarts().isBefore(today.plusHours(1))) {
            setSessionErrorMessage("Start time must be at least 1 hour from now to make time for appointment bookings.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newScheduleView(request, model, range);
        }

        if (!DateUtils.areDatesAtLeastTheseMinutesApart(schedule.getStarts(), schedule.getEnds(), 15)) {
            setSessionErrorMessage("Schedule time must be at least 15 minutes.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newScheduleView(request, model, range);
        }

        if ((collision = scheduleService.getCollidingSchedule(schedule)) != null) {
            setSessionErrorMessage(format("Specified date collides with scheduled date on <strong>%s</strong>.",
                    collision.toString()), request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return newScheduleView(request, model, range);
        }

        if (range) {
            scheduleService.addMultipleScheduleDays(schedule);
        } else {
            scheduleService.addSingleScheduleDay(schedule);
        }

        setSessionSuccessMessage("Time submitted.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/SubmitRange", method = HttpMethod.post, permission = AclPermission.AccessScheduleModule)
    public String submitRangedSchedule(Request request, Response response) {
        return addSchedule(request, response, true);
    }

    @Action(path = "/Submit", method = HttpMethod.post, permission = AclPermission.AccessScheduleModule)
    public String submitSchedule(Request request, Response response) {
        return addSchedule(request, response, false);
    }
}
