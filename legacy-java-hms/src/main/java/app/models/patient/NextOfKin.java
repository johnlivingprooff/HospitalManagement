package app.models.patient;

import java.util.Date;

public class NextOfKin {
    private long id;
    private String firstName;
    private String lastName;
    private String address;
    private RelationshipType relationshipType;
    private String phone1;
    private String phone2;
    private Date created;
    private long patientId;

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public String getPhone1() {
        return phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public Date getCreated() {
        return created;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRelationShipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
