package app.models.account;

import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.validators.*;
import validators.Name;

public class AdministratorDetails implements Validatable {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
