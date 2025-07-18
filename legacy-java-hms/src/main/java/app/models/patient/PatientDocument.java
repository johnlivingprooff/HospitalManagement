package app.models.patient;

import app.core.annotations.Editable;
import app.models.Contexts;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

import java.time.LocalDateTime;

public class PatientDocument {

    @Editable
    private long id;

    @Editable
    @Filter(filters = {"trim", "required", "length(2,100)"}, label = "Document name", contexts = {Contexts.UPDATE, Contexts.CREATE})
    private String name;

    @Editable
    @Filter(filters = {"required", "bool"}, label = "Hide from patient", contexts = {Contexts.UPDATE, Contexts.CREATE})
    private boolean hidden;

    private long patientId;
    private String attachment;
    private LocalDateTime created;

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

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
