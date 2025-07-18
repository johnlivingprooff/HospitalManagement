package app.models.pharmacy.medicine;

import app.core.annotations.HtmlFieldDisplay;
import app.models.Contexts;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@HtmlFieldDisplay(value = "getId()", label = "getLabel()")
public class Medicine {
    @Filter(
            label = "Medicine id",
            filters = {"required", "long", "positive"}, contexts = {Contexts.FIND, Contexts.UPDATE}
    )
    private long id;

    @Filter(label = "Medicine name", filters = {"trim", "required", "length(2,50)"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private String name;

    @Filter(label = "Medicine category", filters = {"required", "long"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private long category;

    @Filter(label = "Medicine location", filters = {"required", "long"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private long location;

    @Filter(label = "Purchase price", filters = {"required", "double", "positive"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private double purchasePrice;

    @Filter(label = "Selling price", filters = {"required", "double", "positive"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private double sellingPrice;

    @Filter(label = "Quantity", filters = {"required", "long", "positive"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private long quantity;

    @Filter(label = "Low stock threshold", filters = {"required", "long", "positive"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private long threshold;

    @Filter(label = "Medicine name", filters = {"trim", "required", "length(2,50)"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private String genericName;

    @Filter(label = "Expiry date", filters = {"required", "date(yyyy-MM-dd)", "future"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
    private LocalDate expires;

    private LocalDateTime updated;
    private String categoryName;
    private String locationName;
    private long daysToExpiration;
    private boolean expiring;
    private boolean runningLow;
    private boolean deleted;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCategory() {
        return category;
    }

    public void setCategory(long category) {
        this.category = category;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public long getLocation() {
        return location;
    }

    public void setLocation(long location) {
        this.location = location;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public LocalDate getExpires() {
        return expires;
    }

    public void setExpires(LocalDate expires) {
        this.expires = expires;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public long getDaysToExpiration() {
        return daysToExpiration;
    }

    public void setDaysToExpiration(long daysToExpiration) {
        this.daysToExpiration = daysToExpiration;
    }

    public boolean isExpiring() {
        return expiring;
    }

    public void setExpiring(boolean expiring) {
        this.expiring = expiring;
    }

    public boolean isRunningLow() {
        return runningLow;
    }

    public void setRunningLow(boolean runningLow) {
        this.runningLow = runningLow;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return getName() + " (" + getId() + ")";
    }

    public String getLabel() {
        return getName() + " (" + getGenericName() + ")";
    }
}
