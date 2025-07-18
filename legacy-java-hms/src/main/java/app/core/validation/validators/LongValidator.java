package app.core.validation.validators;

import app.core.validation.Validator;

@Validator(name = "long")
public class LongValidator extends NumericValidator<Long> {

    public LongValidator() {
        super(Long.class);
    }
}
