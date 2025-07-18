package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.models.patient.IdType;

import java.util.List;

@Validator(name = "idType")
public class IdTypeValidator implements DataValidator.ValidationFilter<String> {
    @Override
    public IdType apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        try {
            return IdType.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Invalid ID type");
        }
    }
}
