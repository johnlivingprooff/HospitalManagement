package app.models.pharmacy.medicine;

import lib.gintec_rdl.jbeava.validation.annotations.Filter;

public class StockAlertConfiguration {
    private long id;

    @Filter(label = "Expiration date threshold", filters = {"required", "long", "positive", "range(1,999)"})
    private long days;

    @Filter(label = "Expiration alerts", filters = {"required", "bool"})
    private boolean notifyExpiration;

    @Filter(label = "Stock level alerts", filters = {"required", "bool"})
    private boolean notifyStockLevel;

    @Filter(label = "Notification email", filters = {"trim", "required", "length(3,50)", "email"})
    private String notificationEmail;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDays() {
        return days;
    }

    public void setDays(long days) {
        this.days = days;
    }

    public boolean isNotifyExpiration() {
        return notifyExpiration;
    }

    public void setNotifyExpiration(boolean notifyExpiration) {
        this.notifyExpiration = notifyExpiration;
    }

    public boolean isNotifyStockLevel() {
        return notifyStockLevel;
    }

    public void setNotifyStockLevel(boolean notifyStockLevel) {
        this.notifyStockLevel = notifyStockLevel;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }
}
