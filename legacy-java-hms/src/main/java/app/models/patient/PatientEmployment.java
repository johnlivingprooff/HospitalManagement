package app.models.patient;

import app.util.LocaleUtil;

import java.util.Date;

public class PatientEmployment {
    private long id;
    private long patientId;
    private String employer;
    private String employeeId;
    private Date created;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPatientId() {
        return patientId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployer() {
        return employer;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public State getState() {
        if (!LocaleUtil.isNullOrEmpty(employer) && !LocaleUtil.isNullOrEmpty(employeeId)) {
            return State.AllAvailable;
        }
        if (!LocaleUtil.isNullOrEmpty(employer) && LocaleUtil.isNullOrEmpty(employeeId)) {
            return State.EmployerOnly;
        }
        if (!LocaleUtil.isNullOrEmpty(employeeId) && LocaleUtil.isNullOrEmpty(employer)) {
            return State.EmployeeIdOnly;
        }
        return State.NotAvailable;
    }

    public enum State {
        NotAvailable(""),
        AllAvailable(""),
        EmployerOnly("Employee ID is missing in employment details"),
        EmployeeIdOnly("You specified employment details but only specified employee ID without specifying employer");

        State(String msg) {
            this.msg = msg;
        }

        public final String msg;
    }
}
