package app.models.account;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(label = "name()", value = "name()")
public enum AccountType {
    Doctor,
    Nurse,
    Regular;

    public static final AccountType[] TYPES = values();
}
