package app.models.reception;

import app.models.account.Sex;

import java.util.Date;

public class PatientLookup {
    private final String firstName;
    private final String lastName;
    private final Sex sex;
    private final Date dateOfBirth;


    public PatientLookup(String firstName, String lastName, Sex sex, Date dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Sex getSex() {
        return sex;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }
}
