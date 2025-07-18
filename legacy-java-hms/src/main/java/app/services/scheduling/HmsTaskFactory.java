package app.services.scheduling;

import app.models.scheduling.AutoTask;
import app.util.ServiceInjector;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;

class HmsTaskFactory extends SimpleJobFactory {
    private final TaskSchedulerService schedulerService;

    HmsTaskFactory(TaskSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        final Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
        final Job job;
        if (AutoTask.class.isAssignableFrom(jobClass)) {
            try {
                job = jobClass.getConstructor(TaskSchedulerService.class).newInstance(schedulerService);
                ServiceInjector.inject(job, schedulerService::getService);
                return job;
            } catch (Exception e) {
                throw new SchedulerException("Problem instantiating job " + jobInfo(bundle.getJobDetail()), e);
            }
        } else {
            getLog().info("Job is not of type {}. Passing to default factory", AutoTask.class);
            return super.newJob(bundle, scheduler);
        }
    }

    private String jobInfo(JobDetail jobDetail) {
        return "[identity=" + jobDetail.getKey().getGroup() + "." + jobDetail.getKey().getName()
                + ", class=" + jobDetail.getJobClass() + "]";
    }
}