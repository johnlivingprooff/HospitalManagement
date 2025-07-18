package app.models;

import java.util.Date;

public interface Subject {
    enum SubjectType {
        STP("Patient"),
        STA("Account");

        SubjectType(String name) {
            this.name = name;
        }

        public final String name;

        public static final SubjectType[] VALUES = values();
    }

    long getId();

    default boolean isPatient() {
        return getSubjectType() == SubjectType.STP;
    }

    int getAge();

    SubjectType getSubjectType();

    String getPassword();

    Date getCreated();

    boolean isActive();

    String getEmail();

    String getFullName();

    default String getAuditLogString() {
        String type = "";
        if (isPatient()) {
            type = "Patient, ";
        }
        return getFullName() + "(" + type + getId() + ")";
    }
}
