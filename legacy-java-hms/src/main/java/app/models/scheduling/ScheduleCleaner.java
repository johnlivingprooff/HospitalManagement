package app.models.scheduling;

import app.core.annotations.Inject;
import app.services.audit.AuditService;
import app.services.doctor.ScheduleService;
import app.services.scheduling.Task;
import app.services.scheduling.TaskSchedulerService;
import org.quartz.JobExecutionContext;

import static app.models.scheduling.AutoTask.EVERY_MIDNIGHT_CRON_PATTERN;

@Task(
        name = "AuditLogArchivingJob",
        group = "Nightly",
        cron = EVERY_MIDNIGHT_CRON_PATTERN,
        description = "Runs every midnight and cleans expired dates from doctors schedules."
)
public class ScheduleCleaner extends AutoTask {

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private AuditService auditService;

    public ScheduleCleaner(TaskSchedulerService service) {
        super(service);
    }

    @Override
    protected void run(JobExecutionContext ctx) {
        scheduleService.cleanExpiredDates();
        auditService.log("System cleaned expired schedule dates.");
    }
}