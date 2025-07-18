package app.services.scheduling;

import app.Configuration;
import app.core.Service;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.scheduling.AutoTask;
import org.atteo.classindex.ClassIndex;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.util.Map;
import java.util.Properties;

@ServiceDescriptor
public final class TaskSchedulerService extends ServiceImpl {
    private final Scheduler scheduler;

    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public TaskSchedulerService(Configuration configuration) throws SchedulerException {
        super(configuration);
        final Properties properties = new Properties();

        properties.put("org.quartz.scheduler.instanceName", "PostgresScheduler");
        properties.put("org.quartz.scheduler.instanceId", "AUTO");
        properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.put("org.quartz.threadPool.threadCount", "4");
        properties.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        properties.put("org.quartz.jobStore.dataSource", "quartzDS");
        properties.put("org.quartz.dataSource.quartzDS.driver", "org.postgresql.Driver");
        properties.put("org.quartz.dataSource.quartzDS.URL", configuration.DataSource);
        properties.put("org.quartz.dataSource.quartzDS.user", configuration.DatabaseUser);
        properties.put("org.quartz.dataSource.quartzDS.password", configuration.DatabasePassword);

        SchedulerFactory schedulerFactory = new StdSchedulerFactory(properties);

        // Add our job factory so we can do dependency injection and stuff
        scheduler = schedulerFactory.getScheduler();
        scheduler.setJobFactory(new HmsTaskFactory(this));

        // schedule all necessary tasks
        scheduleSystemTasks();
    }

    public void startScheduler() throws SchedulerException {
        scheduler.start();
    }

    @SuppressWarnings("unchecked")
    private void scheduleSystemTasks() {
        Iterable<Class<?>> taskClasses;

        taskClasses = ClassIndex.getAnnotated(Task.class);
        for (Class<?> taskClass : taskClasses) {
            if (AutoTask.class.isAssignableFrom(taskClass)) {
                scheduleTask((Class<? extends AutoTask>) taskClass, null);
            } else {
                throw new IllegalArgumentException(taskClass + " must extend " + AutoTask.class);
            }
        }
    }

    @Override
    public <T extends Service> T getService(Class<T> service) {
        return super.getService(service);
    }

    private void scheduleTask(Class<? extends AutoTask> jobClass, Map<String, Object> data) {
        final Task task;
        final Trigger trigger;
        final JobDetail jobDetail;

        task = jobClass.getAnnotation(Task.class);
        if (task == null) {
            throw new IllegalArgumentException("Job must have the " + Task.class + " annotation.");
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Scheduling task {} with cron {}.", task.name(), task.cron());
        }

        jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(task.name(), task.group())
                .usingJobData(new JobDataMap(data == null ? Map.of() : data))
                .withDescription(task.description())
                .storeDurably()
                .build();

        try {
            trigger = CronScheduleBuilder.cronSchedule(task.cron())
                    .withMisfireHandlingInstructionFireAndProceed()
                    .build();

            ((CronTriggerImpl) trigger).setName(jobDetail.getKey().getName());

            if (!scheduler.checkExists(jobDetail.getKey())) {
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                getLogger().info("Not adding job {} because it already exists in store.",
                        jobIdentity(jobDetail.getKey()));
            }
        } catch (Exception e) {
            getLogger().error("Error when attempting to schedule job {}.",
                    jobIdentity(jobDetail.getKey()), e);
        }
    }

    private static String jobIdentity(JobKey jobKey) {
        return jobKey.getGroup() + "#" + jobKey.getName();
    }
}
