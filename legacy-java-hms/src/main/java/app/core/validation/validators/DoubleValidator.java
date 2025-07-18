package app.core.validation.validators;

import app.core.validation.Validator;

@Validator(name = "double")
public class DoubleValidator extends NumericValidator<Double> {

    public DoubleValidator() {
        super(Double.class);
    }
}
