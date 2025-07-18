package app.core.validation.validators;

import app.core.validation.Validator;

@Validator(name = "lower")
public class Lower extends CaseTransformer {
    public Lower() {
        super(Transformation.lower);
    }
}
