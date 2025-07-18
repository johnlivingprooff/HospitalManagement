package app.core.validation.validators;

import app.core.validation.Validator;

@Validator(name = "int")
public class IntValidator extends NumericValidator<Integer> {
    public IntValidator() {
        super(Integer.class);
    }
}
