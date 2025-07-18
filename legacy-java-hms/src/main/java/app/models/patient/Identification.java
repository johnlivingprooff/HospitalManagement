package app.models.patient;

import java.util.Date;

public final class Identification {
    private long id;
    private String idNumber;
    private IdType idType;
    private Date issued;
    private Date expires;
    private Date created;
    private long patientId;

    public String getIdNumber() {
        return idNumber;
    }

    public IdType getIdType() {
        return idType;
    }

    public Date getIssued() {
        return issued;
    }

    public Date getExpires() {
        return expires;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
