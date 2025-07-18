package app.models.scheduling;

import app.core.annotations.Inject;
import app.services.appointments.AppointmentService;
import app.services.scheduling.Task;
import app.services.scheduling.TaskSchedulerService;
import org.quartz.JobExecutionContext;

import static app.models.scheduling.AutoTask.EVERY_MINUTE_CRON_PATTERN;

@Task(
        name = "AppointmentTerminator",
        group = "Cleaners",
        cron = EVERY_MINUTE_CRON_PATTERN,
        description = "Runs every minute and cancels missed appointments."
)
public class AppointmentTerminator extends AutoTask {

    @Inject
    private AppointmentService appointmentService;

    public AppointmentTerminator(TaskSchedulerService service) {
        super(service);
    }

    @Override
    protected void run(JobExecutionContext ctx) {
        appointmentService.cancelMissedAppointments();
    }
}