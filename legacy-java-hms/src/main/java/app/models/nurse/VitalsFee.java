package app.models.nurse;

import app.core.annotations.Editable;
import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.validators.DecimalValidator;
import app.core.validation.validators.PositiveNumber;
import app.core.validation.validators.Required;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VitalsFee implements Validatable {

    @Editable
    @ValidationChain(
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(DecimalValidator.class),
                    @FilterNode(PositiveNumber.class)
            }
    )
    private BigDecimal fee;
    private LocalDateTime updatedAt;

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
