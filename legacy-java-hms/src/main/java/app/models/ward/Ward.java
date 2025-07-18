package app.models.ward;

import app.core.annotations.HtmlFieldDisplay;
import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.ValidationStage;
import app.core.validation.validators.*;

@HtmlFieldDisplay(label = "getName()", value = "getId()")
public class Ward implements Validatable {

    @ValidationChain(
            label = "Ward id",
            stage = ValidationStage.Update,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class)
            }
    )
    private long id;

    @ValidationChain(
            label = "Code",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Trim.class),
                    @FilterNode(Alphanumeric.class),
                    @FilterNode(value = Length.class, parameters = {"2", "10"})
            }
    )
    private String code;

    @ValidationChain(
            label = "Name",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Trim.class),
                    @FilterNode(Text.class),
                    @FilterNode(value = Length.class, parameters = {"2", "100"})
            }
    )
    private String name;

    @ValidationChain(
            label = "Active",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(BooleanValidator.class)
            }
    )
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return getName() + " (" + getId() + ")";
    }
}
