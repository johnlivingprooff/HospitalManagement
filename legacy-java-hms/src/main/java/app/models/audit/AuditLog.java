package app.models.audit;

import app.services.audit.AuditService;

import java.util.Date;

public final class AuditLog {
    private long Id;
    private AuditService.LogType LogType;
    private String UserAgent, Action, Location, ActionName, Address;
    private Date Created;

    public boolean isArchived() {
        return Archived;
    }

    public void setArchived(boolean archived) {
        Archived = archived;
    }

    private boolean Archived;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public AuditService.LogType getLogType() {
        return LogType;
    }

    public void setLogType(AuditService.LogType logType) {
        LogType = logType;
    }

    public String getUserAgent() {
        return UserAgent;
    }

    public void setUserAgent(String userAgent) {
        UserAgent = userAgent;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getActionName() {
        return ActionName;
    }

    public void setActionName(String actionName) {
        ActionName = actionName;
    }

    public Date getCreated() {
        return Created;
    }

    public void setCreated(Date created) {
        Created = created;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
