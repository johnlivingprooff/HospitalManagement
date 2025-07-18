package app.models.scheduling;

import app.Configuration;
import app.core.Service;
import app.services.scheduling.TaskSchedulerService;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public abstract class AutoTask implements Job {
    static final String EVERY_MIDNIGHT_CRON_PATTERN = "0 0 0 * * ?";
    static final String EVERY_MINUTE_CRON_PATTERN = "0 0/1 * 1/1 * ? *";
    static final String ALERTS_GROUP = "Alerts";

    private final Logger logger;
    private final TaskSchedulerService taskSchedulerService;

    public AutoTask(TaskSchedulerService service) {
        this.taskSchedulerService = service;
        logger = LoggerFactory.getLogger(getClass());
    }

    protected final Configuration getConfiguration() {
        return taskSchedulerService.getSystemConfiguration();
    }

    protected final <T extends Service> T getService(Class<T> service) {
        return taskSchedulerService.getService(service);
    }

    protected final Logger getLogger() {
        return logger;
    }

    @Override
    public final void execute(JobExecutionContext context) {
        try {
            getLogger().info("Job {} entered at {}.", jobName(context.getJobDetail()), LocalDateTime.now());
            run(context);
        } catch (Exception e) {
            getLogger().error("Error when executing job {}: {}.",
                    context.getJobDetail().getKey().getName(), e.getMessage());
        }
        getLogger().info("Job {} exited at {}.", jobName(context.getJobDetail()), LocalDateTime.now());
    }

    /**
     * Run
     *
     * @param ctx Job execution context
     */
    protected abstract void run(JobExecutionContext ctx);

    protected final String jobName(JobDetail detail) {
        return detail.getKey().getGroup() + "." + detail.getKey().getName();
    }

    protected final void error(String msg, Object... args) {
        if (getLogger().isErrorEnabled()) {
            getLogger().error(msg, args);
        }
    }

    protected final void debug(String msg, Object... args) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(msg, args);
        }
    }
}