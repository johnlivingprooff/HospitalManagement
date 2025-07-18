package app.models.patient;

import app.core.annotations.Editable;
import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.validators.*;
import validators.Phone;

import java.time.LocalDateTime;

public class PatientNextOfKin implements Validatable {
    private long id;

    @Editable
    @ValidationChain(
            label = "First name",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"2", "50"}),
                    @FilterNode(Text.class)
            }
    )
    private String firstName;

    @Editable
    @ValidationChain(
            label = "Last name",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Text.class, parameters = {"2", "50"}),
                    @FilterNode(Text.class)
            }
    )
    private String lastName;

    @Editable
    @ValidationChain(
            label = "Address",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"3", "200"}),
                    @FilterNode(Text.class)
            }
    )
    private String address;

    @Editable
    @ValidationChain(
            label = "Primary phone number",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(Phone.class)
            }
    )
    private String phone1;

    @Editable
    @ValidationChain(
            label = "Secondary phone number",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Phone.class)
            }
    )
    private String phone2;
    private long patientId;
    private LocalDateTime created;
    private LocalDateTime modified;

    @Editable
    @ValidationChain(
            label = "Relationship",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(RelationshipTypeValidator.class)
            }
    )
    private RelationshipType relationship;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public RelationshipType getRelationship() {
        return relationship;
    }

    public void setRelationship(RelationshipType relationship) {
        this.relationship = relationship;
    }
}
