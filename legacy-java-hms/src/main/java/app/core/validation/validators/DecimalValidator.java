package app.core.validation.validators;

import app.core.validation.Validator;

import java.math.BigDecimal;

@Validator(name = "decimal")
public class DecimalValidator extends NumericValidator<BigDecimal> {

    public DecimalValidator() {
        super(BigDecimal.class);
    }
}
