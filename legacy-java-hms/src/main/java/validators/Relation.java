package validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.ValidationFilterImpl;
import app.models.patient.RelationshipType;

import java.util.List;

@Validator(name = "relation")
public class Relation extends ValidationFilterImpl<String, RelationshipType> {

    @Override
    public RelationshipType apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        try {
            return RelationshipType.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Unknown relationship type for field " + name);
        }
    }
}
