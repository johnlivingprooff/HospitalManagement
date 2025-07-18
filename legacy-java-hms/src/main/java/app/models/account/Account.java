package app.models.account;

import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.ValidationStage;
import app.core.validation.validators.*;
import app.models.Subject;
import app.util.DateUtils;
import validators.Gender;
import validators.Name;
import validators.Phone;

import java.util.Date;

public final class Account implements Validatable, Subject {

    @ValidationChain(
            stage = ValidationStage.Update,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class),
                    @FilterNode(PositiveNumber.class)
            }
    )
    private long id;

    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"3", "50"}),
                    @FilterNode(Lower.class),
                    @FilterNode(Email.class)
            }
    )
    private String email;

    @ValidationChain(
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Phone.class),
            }
    )
    private String phone;

    private String password;

    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"2", "50"}),
                    @FilterNode(Name.class),
                    @FilterNode(Capitalize.class)
            }
    )
    private String firstName;

    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"2", "50"}),
                    @FilterNode(Name.class),
                    @FilterNode(Capitalize.class)
            }
    )
    private String lastName;

    @ValidationChain(
            label = "Date of birth",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(value = DateValidator.class, parameters = {"LessThan"}),
            }
    )
    private Date dob;
    private Date created, modified;

    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Gender.class)
            }
    )
    private Sex sex;
    private String roleKey;

    private String picture;

    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(BooleanValidator.class)
            }
    )
    private boolean active;
    private boolean system;
    private boolean hidden;
    private boolean role_modifiable;

    private String roleName;
    private String department;

    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class),
                    @FilterNode(PositiveNumber.class)
            }
    )
    private long departmentId;

    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class),
                    @FilterNode(PositiveNumber.class)
            }
    )
    private long roleId;

    @ValidationChain(
            fieldName = "accountType",
            label = "Account type",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(AccountTypeValidator.class)
            }
    )
    private AccountType account_type;

    public String fullname() {
        return firstName + " " + lastName;
    }

    public long getId() {
        return id;
    }

    @Override
    public SubjectType getSubjectType() {
        return SubjectType.STA;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
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

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public String getAuditLogString() {
        return fullname() + " (" + getId() + ")";
    }

    public boolean isRole_modifiable() {
        return role_modifiable;
    }

    public void setRole_modifiable(boolean role_modifiable) {
        this.role_modifiable = role_modifiable;
    }

    @Override
    public String toString() {
        return getAuditLogString();
    }

    @Override
    public String getFullName() {
        return fullname();
    }

    @Override
    public boolean isPatient() {
        return false;
    }

    public AccountType getAccountType() {
        return account_type;
    }

    public void setAccountType(AccountType accountType) {
        this.account_type = accountType;
    }

    @Override
    public int getAge() {
        return DateUtils.age(dob);
    }
}
