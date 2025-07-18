package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.models.patient.RelationshipType;

import java.util.List;

@Validator(name = "relationshipType")
public class RelationshipTypeValidator implements DataValidator.ValidationFilter<String> {
    @Override
    public RelationshipType apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        try {
            return RelationshipType.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Invalid relationship type selection.");
        }
    }
}
