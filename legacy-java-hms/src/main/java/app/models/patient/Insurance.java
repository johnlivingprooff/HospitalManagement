package app.models.patient;

import app.core.annotations.Editable;
import app.core.annotations.HtmlFieldDisplay;
import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.validators.Length;
import app.core.validation.validators.Required;
import app.core.validation.validators.Trim;
import validators.Phone;

import java.time.LocalDateTime;

@HtmlFieldDisplay(label = "getInsurer()", value = "getId()")
public class Insurance implements Validatable {
    private long id;

    @Editable
    @ValidationChain(
            label = "Insurance company name",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"2", "100"})
            }
    )
    private String insurer;

    @Editable
    @ValidationChain(
            label = "Membership id",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"2", "20"})
            }
    )
    private String membershipId;
    @Editable
    @ValidationChain(
            label = "Address",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"1", "200"})
            }
    )
    private String address;
    @Editable
    @ValidationChain(
            label = "Phone",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"10", "30"}),
                    @FilterNode(Phone.class)
            }
    )
    private String phone;
    private long patientId;
    private LocalDateTime created;
    private LocalDateTime modified;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInsurer() {
        return insurer;
    }

    public void setInsurer(String insurer) {
        this.insurer = insurer;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
}
