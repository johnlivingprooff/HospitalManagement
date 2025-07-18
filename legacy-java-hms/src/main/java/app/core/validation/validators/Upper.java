package app.core.validation.validators;

import app.core.validation.Validator;

@Validator(name = "upper")
public class Upper extends CaseTransformer {
    public Upper() {
        super(Transformation.upper);
    }
}
