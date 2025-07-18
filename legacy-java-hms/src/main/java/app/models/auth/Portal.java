package app.models.auth;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(label = "description", value = "name()")
public enum Portal {
    Staff("Staff Portal"),
    Patient("Patient Portal");

    Portal(String description) {
        this.description = description;
    }

    public final String description;

    public static final Portal[] PORTALS = values();
}
