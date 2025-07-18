package app.models.scheduling;

import app.core.annotations.Inject;
import app.models.pharmacy.medicine.Medicine;
import app.models.pharmacy.medicine.StockAlertConfiguration;
import app.services.messaging.MessagingService;
import app.services.pharmacy.PharmacyService;
import app.services.scheduling.Task;
import app.services.scheduling.TaskSchedulerService;
import org.quartz.JobExecutionContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Task(
        name = "StockLevelAlertTask",
        cron = AutoTask.EVERY_MINUTE_CRON_PATTERN, // every minute
        group = AutoTask.ALERTS_GROUP,
        description = "Sends out low medicine stock level email alerts."
)
public class StockLevelAlertTask extends AutoTask {

    private static final int HOURS_24 = 24;
    private static final Object LOCK = new Object();

    @Inject
    private PharmacyService pharmacyService;

    @Inject
    MessagingService messagingService;

    public StockLevelAlertTask(TaskSchedulerService service) {
        super(service);
    }

    @Override
    protected void run(JobExecutionContext ctx) {
        long hours;
        String subject;
        String template;
        LocalDateTime lastSent;
        Map<String, Object> model;
        List<Medicine> medicineList;
        StockAlertConfiguration config;

        debug("Checking stock level...");
        config = pharmacyService.getStockAlertConfiguration();

        synchronized (LOCK) {
            if (config.isNotifyStockLevel()) {
                lastSent = pharmacyService.getLastStockLevelAlertTime();

                // When was the last email sent
                if ((hours = ChronoUnit.HOURS.between(lastSent, LocalDateTime.now())) >= HOURS_24) {
                    // Get all medicine running low
                    medicineList = pharmacyService.getLowStockMedicines();
                    if (!medicineList.isEmpty()) {
                        model = new LinkedHashMap<>();
                        model.put("medicines", medicineList);

                        subject = "Low Medicine Stock Level Alert";
                        template = "alerts/low-stock-level.html";
                        messagingService.sendEmail(config.getNotificationEmail(),
                                config.getNotificationEmail(), subject, template, model);
                        pharmacyService.updateLastStockLevelAlertTime(LocalDateTime.now());
                    }
                }
                debug("Last alert was sent at {}. {} hour ago.", lastSent, hours);
            } else {
                debug("Skipping stock level checks because it's disabled.");
            }
        }

    }
}
