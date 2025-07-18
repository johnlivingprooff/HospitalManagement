package app.types;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(value = "value", label = "label")
public enum Bool {
    True(true, "Yes"),
    False(false, "No");

    Bool(boolean value, String label) {
        this.value = value;
        this.label = label;
    }

    public final boolean value;
    public final String label;

    public static final Bool VALUES[] = Bool.values();
}
