package app.models.doctor;


import app.core.annotations.HtmlFieldDisplay;


@HtmlFieldDisplay(value = "getId()")
public final class Doctor {
    private long id;
    private String department;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (" + department + ")";
    }
}
