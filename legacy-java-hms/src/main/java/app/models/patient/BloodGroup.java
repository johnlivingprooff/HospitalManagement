package app.models.patient;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(label = "description", value = "name()")
public enum BloodGroup {
    APositive("A RhD positive (A+)"),
    ANegative("A RhD negative (A-)"),
    BPositive("B RhD positive (B+)"),
    BNegative("B RhD negative (B-)"),
    OPositive("O RhD positive (O+)"),
    ONegative("O RhD negative (O-)"),
    ABPositive("AB RhD positive (AB+)"),
    ABNegative("AB RhD negative (AB-)"),
    NotAvailable("Not Available");

    BloodGroup(String description) {
        this.description = description;
    }

    public final String description;

    public static final BloodGroup[] GROUPS = values();
}
