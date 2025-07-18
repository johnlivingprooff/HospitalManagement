package validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.ValidationFilterImpl;
import app.models.account.Sex;

import java.util.List;

@Validator(name = "sex")
public class Gender extends ValidationFilterImpl<String, Sex> {

    @Override
    public Sex apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        try {
            return Sex.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException(name + " must be Male or Female. Unknown value specified");
        }
    }
}
