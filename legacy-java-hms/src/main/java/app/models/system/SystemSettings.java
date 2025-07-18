package app.models.system;

import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.validators.Length;
import app.core.validation.validators.Required;
import app.core.validation.validators.Trim;

import java.util.Date;

public final class SystemSettings implements Validatable {
    private long id;

    @ValidationChain(
            label = "Banner",
            filters = {
                    @FilterNode(Trim.class),
                    @FilterNode(Required.class),
                    @FilterNode(value = Length.class, parameters = {"3", "100"})
            }
    )
    private String banner;
    private String logo;
    private Date modified;
    private long modifiedBy;

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
