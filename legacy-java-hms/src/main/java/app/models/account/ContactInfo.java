package app.models.account;

public final class ContactInfo {
    private long Id;
    private String PhysicalAddress;
    private String MailingAddress;
    private String Phone1;
    private String Phone2;
    private long UserID;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getPhysicalAddress() {
        return PhysicalAddress;
    }

    public void setPhysicalAddress(String physicalAddress) {
        PhysicalAddress = physicalAddress;
    }

    public String getMailingAddress() {
        return MailingAddress;
    }

    public void setMailingAddress(String mailingAddress) {
        MailingAddress = mailingAddress;
    }

    public String getPhone1() {
        return Phone1;
    }

    public void setPhone1(String phone1) {
        Phone1 = phone1;
    }

    public String getPhone2() {
        return Phone2;
    }

    public void setPhone2(String phone2) {
        Phone2 = phone2;
    }

    public long getUserID() {
        return UserID;
    }

    public void setUserID(long userID) {
        UserID = userID;
    }
}
