package app.models.bed;

import app.core.annotations.HtmlFieldDisplay;
import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.ValidationStage;
import app.core.validation.validators.*;

@HtmlFieldDisplay(label = "getName()", value = "getId()")
public class Bed implements Validatable {

    @ValidationChain(
            label = "Bed",
            stage = ValidationStage.Update,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class)
            }
    )
    private long id;

    @ValidationChain(
            label = "Ward",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class)
            }
    )
    private long wardId;

    @ValidationChain(
            label = "Code",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Trim.class),
                    @FilterNode(Alphanumeric.class),
                    @FilterNode(value = Length.class, parameters = {"1", "10"})
            }
    )
    private String code;
    private String oldCode;

    private boolean vacant;
    private boolean deleted;

    private String ward;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWardId() {
        return wardId;
    }

    public void setWardId(long wardId) {
        this.wardId = wardId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isVacant() {
        return vacant;
    }

    public void setVacant(boolean vacant) {
        this.vacant = vacant;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getOldCode() {
        return oldCode;
    }

    public void setOldCode(String oldCode) {
        this.oldCode = oldCode;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", code=" + code + "]";
    }
}
