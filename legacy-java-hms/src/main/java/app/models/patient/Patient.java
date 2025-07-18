package app.models.patient;

import app.core.Country;
import app.core.annotations.Editable;
import app.core.annotations.HtmlFieldDisplay;
import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.ValidationStage;
import app.core.validation.validators.*;
import app.models.Subject;
import app.models.account.Sex;
import app.util.DateUtils;
import validators.*;

import java.util.Date;

@HtmlFieldDisplay(label = "fullname()", value = "getId()")
public class Patient implements Subject, Validatable {

    @Editable
    @ValidationChain(
            stage = ValidationStage.Update,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class)
            }
    )
    private long id;

    private String mrn;

    private long createdBy; // Useful for reports

    @Editable
    @ValidationChain(
            label = "First name",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Name.class),
                    @FilterNode(value = Length.class, parameters = {"2", "50"})
            }
    )
    private String firstName;

    @Editable
    @ValidationChain(
            label = "First name",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Name.class),
                    @FilterNode(value = Length.class, parameters = {"2", "50"})
            }
    )
    private String lastName;

    @Editable
    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Gender.class)
            }
    )
    private Sex sex;

    @Editable
    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(BloodGroupValidator.class)
            }
    )
    private BloodGroup bloodGroup;

    @Editable
    @ValidationChain(
            label = "Date of birth",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(value = DateValidator.class, parameters = "LessThan")
            }
    )
    private Date dob;
    private Date created;
    private Date modified;
    private String picture;

    @Editable
    @ValidationChain(
            label = "Email",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(value = Length.class, parameters = {"3", "50"}),
                    @FilterNode(Lower.class),
                    @FilterNode(Email.class)
            }
    )
    private String email;

    @Editable
    @ValidationChain(
            label = "Phone number",
            filters = {
                    @FilterNode(Phone.class)
            }
    )
    private String phone;

    @Editable
    @ValidationChain(
            label = "Address",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Text.class),
                    @FilterNode(value = Length.class, parameters = {"3", "100"})
            }
    )
    private String address;

    @Editable
    @ValidationChain(
            label = "Country of Nationality",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(CountryValidator.class)
            }
    )
    private Country nationality;
    private PatientStatus status;
    private String password;

    @Editable
    @ValidationChain(
            label = "Patient type",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(PatientTypeValidator.class)
            }
    )
    private PatientType type;

    @Editable
    @ValidationChain(
            label = "ID Type",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(IdTypeValidator.class)
            }
    )
    private IdType idType;

    @Editable
    @ValidationChain(
            label = "ID Number",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(Text.class),
                    @FilterNode(value = Length.class, parameters = {"3", "20"})
            }
    )
    private String idNumber;

    @Editable
    @ValidationChain(
            label = "ID Expiration Date",
            filters = {
                    @FilterNode(DateValidator.class)
            }
    )
    private Date idExpiration;

    @Editable
    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(BooleanValidator.class)
            }
    )
    private boolean active;

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public Country getNationality() {
        return nationality;
    }

    public void setNationality(Country nationality) {
        this.nationality = nationality;
    }

    public long getId() {
        return id;
    }

    @Override
    public SubjectType getSubjectType() {
        return SubjectType.STP;
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

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Date getDob() {
        return dob;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PatientType getType() {
        return type;
    }

    public void setType(PatientType type) {
        this.type = type;
    }

    public String fullname() {
        return firstName + " " + lastName;
    }

    public String getAuditLogString() {
        return fullname() + " (" + getId() + ")";
    }

    @Override
    public String toString() {
        return fullname();
    }

    public PatientStatus getStatus() {
        return status;
    }

    public void setStatus(PatientStatus status) {
        this.status = status;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long attendedBy) {
        this.createdBy = attendedBy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public Date getIdExpiration() {
        return idExpiration;
    }

    public void setIdExpiration(Date idExpiration) {
        this.idExpiration = idExpiration;
    }

    @Override
    public boolean isPatient() {
        return true;
    }

    @Override
    public int getAge() {
        return DateUtils.age(dob);
    }

    @Override
    public String getFullName() {
        return fullname();
    }
}
