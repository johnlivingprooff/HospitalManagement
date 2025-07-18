package app.models.location;

import app.core.annotations.HtmlFieldDisplay;
import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.ValidationStage;
import app.core.validation.validators.*;

import java.util.Date;

@HtmlFieldDisplay(label = "getName()", value = "getId()")
public abstract class Location implements Validatable {

    @ValidationChain(stage = ValidationStage.Update, filters = {
            @FilterNode(value = Required.class),
            @FilterNode(value = LongValidator.class),
            @FilterNode(value = PositiveNumber.class)
    })
    private long id;

    @ValidationChain(filters = {
            @FilterNode(value = Required.class),
            @FilterNode(value = Alphanumeric.class),
            @FilterNode(value = Length.class, parameters = {"2", "10"})
    })
    private String code;

    @ValidationChain(filters = {
            @FilterNode(value = Required.class),
            @FilterNode(value = Alphanumeric.class),
            @FilterNode(value = Length.class, parameters = {"2", "50"})
    })
    private String name;
    private Date created;
    private Date modified;
    private boolean system;
    private boolean hidden;

    @ValidationChain(filters = {
            @FilterNode(value = Required.class),
            @FilterNode(value = BooleanValidator.class)
    })
    private boolean active;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "[id=" + getId() + ", code= " + getCode() + ", name=" + getName() + "]";
    }

    public final boolean isUseable() {
        return !isHidden();
    }

    /**
     * @return Returns true if this location object can be used in regular instances,
     * that is, it's active, not hidden and not system
     */
    public final boolean isGoodForRegularUse() {
        return isActive() && !isHidden() && !isSystem();
    }
}
