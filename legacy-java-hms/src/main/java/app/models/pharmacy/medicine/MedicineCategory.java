package app.models.pharmacy.medicine;

import app.core.annotations.HtmlFieldDisplay;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

@HtmlFieldDisplay(label = "getName()", value = "getId()")
public class MedicineCategory {

    public static final int CONTEXT_CREATE = 0;
    public static final int CONTEXT_UPDATE = 1;
    public static final int CONTEXT_FIND = 2;

    @Filter(label = "Category id", filters = {"required", "long", "positive"}, contexts = {CONTEXT_UPDATE, CONTEXT_FIND})
    private long id;

    @Filter(filters = {"trim", "required", "length(2,50)"}, contexts = {CONTEXT_CREATE, CONTEXT_UPDATE})
    private String name;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
