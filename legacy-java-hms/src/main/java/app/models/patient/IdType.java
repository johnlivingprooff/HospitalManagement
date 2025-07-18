package app.models.patient;


import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(label = "description", value = "name()")
public enum IdType {
    License("Drivers License"),
    GovernmentId("Government Issued ID"),
    Passport("Passport");

    IdType(String description) {
        this.description = description;
    }

    public final String description;

    public static final IdType[] ID_TYPES = values();
}
