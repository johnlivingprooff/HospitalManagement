package app.models.scheduling;

import app.core.annotations.Inject;
import app.services.audit.AuditService;
import app.services.scheduling.Task;
import app.services.scheduling.TaskSchedulerService;
import org.quartz.JobExecutionContext;

import static app.models.scheduling.AutoTask.EVERY_MIDNIGHT_CRON_PATTERN;

@Task(
        name = "AuditLogArchivingJob",
        group = "Nightly",
        cron = EVERY_MIDNIGHT_CRON_PATTERN,
        description = "Runs every midnight and archives audit logs older 14 years"
)
public class AuditLogArchiver extends AutoTask {

    @Inject
    AuditService auditService;

    public AuditLogArchiver(TaskSchedulerService service) {
        super(service);
    }

    @Override
    protected void run(JobExecutionContext ctx) {

        auditService = getService(AuditService.class);
        auditService.archiveExpiredLogs();
        auditService.system("Archived audit logs older than 14 days.",
                AuditLogArchiver.class.getCanonicalName(), "127.0.0.1");

        auditService.deleteExpiredLog();
        auditService.system("Deleted archived audit logs older than 1 month.",
                AuditLogArchiver.class.getCanonicalName(), "127.0.0.1");
    }
}