package app.models.section;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(value = "getId()", label = "getSectionName()")
public final class Section {
    private long Id;
    private String SectionName;
    private boolean Hidden, SystemSection;

    // dynamic/aliased columns
    private long UserCount;
    private String SectionHead;

    public long getUserCount() {
        return UserCount;
    }

    public void setUserCount(long userCount) {
        UserCount = userCount;
    }

    public String getSectionHead() {
        return SectionHead;
    }

    public void setSectionHead(String sectionHead) {
        SectionHead = sectionHead;
    }

    public boolean isSystemSection() {
        return SystemSection;
    }

    public void setSystemSection(boolean systemSection) {
        SystemSection = systemSection;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getSectionName() {
        return SectionName;
    }

    public void setSectionName(String sectionName) {
        SectionName = sectionName;
    }

    public boolean isHidden() {
        return Hidden;
    }

    public void setHidden(boolean hidden) {
        Hidden = hidden;
    }
}
