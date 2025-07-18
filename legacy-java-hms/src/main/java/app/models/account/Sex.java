package app.models.account;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(label = "desc", value = "name()")
public enum Sex {
    Female("Female"),
    Male("Male");

    Sex(String s) {
        this.desc = s;
    }

    public final String desc;

    public static final Sex[] VALUES = values();
}
