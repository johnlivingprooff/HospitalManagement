package app.models.pharmacy.medicine;

import java.time.LocalDateTime;

public class StockAlertLog {
    private LocalDateTime lastExpirationAlertAt;
    private LocalDateTime lastStockLevelAlertAt;

    public LocalDateTime getLastExpirationAlertAt() {
        return lastExpirationAlertAt;
    }

    public void setLastExpirationAlertAt(LocalDateTime lastExpirationAlertAt) {
        this.lastExpirationAlertAt = lastExpirationAlertAt;
    }

    public LocalDateTime getLastStockLevelAlertAt() {
        return lastStockLevelAlertAt;
    }

    public void setLastStockLevelAlertAt(LocalDateTime lastStockLevelAlertAt) {
        this.lastStockLevelAlertAt = lastStockLevelAlertAt;
    }
}
