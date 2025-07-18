package validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.StringValidationFilter;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "phone")
public class Phone extends StringValidationFilter {

    @Override
    public String apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        String pattern = "^[0-9()\\-\\x20+]{10,30}$";
        if (!LocaleUtil.isNullOrEmpty(input)) {
            if (input.matches(pattern)) {
                return input;
            }
            throw new DataValidator.ValidationException(name + " is not a valid phone number. Must be at least 10 digits");
        }
        return null;
    }
}
