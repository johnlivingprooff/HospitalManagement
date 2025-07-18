package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "boolean")
public class BooleanValidator extends ValidationFilterImpl<String, Boolean> {

    @Override
    public Boolean apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) {
            return false;
        }
        try {
            return Boolean.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException(name + " must be a valid boolean value");
        }
    }
}
