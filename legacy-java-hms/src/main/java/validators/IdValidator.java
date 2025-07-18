package validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.ValidationFilterImpl;
import app.models.patient.IdType;

import java.util.List;

@Validator(name = "idType")
public class IdValidator extends ValidationFilterImpl<String, IdType> {

    @Override
    public IdType apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        try {
            return IdType.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException(name + " is not a valid ID type");
        }
    }
}
