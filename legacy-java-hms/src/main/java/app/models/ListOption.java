package app.models;

import app.core.annotations.HtmlFieldDisplay;

/**
 * <p>Generic list option</p>
 */
@HtmlFieldDisplay(label = "getLabel()", value = "getId()")
public class ListOption {
    private long id;
    private String label;

    public ListOption() {
    }

    public ListOption(long id, String label) {
        this.id = id;
        this.label = label;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ListOption && ((ListOption) obj).id == this.id;
    }
}
