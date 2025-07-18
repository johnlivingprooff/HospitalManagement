package app.models.pharmacy.medicine;

import app.core.annotations.HtmlFieldDisplay;
import app.models.Contexts;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

@HtmlFieldDisplay(label = "getName()", value = "getId()")
public class MedicineLocation {

    @Filter(label = "Category id", filters = {"required", "long", "positive"}, contexts = {Contexts.UPDATE, Contexts.FIND})
    private long id;

    @Filter(label = "Location name", filters = {"trim", "required", "length(2,50)"}, contexts = {Contexts.CREATE, Contexts.UPDATE})
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
        return getName() + " (" + getId() + ")";
    }
}
