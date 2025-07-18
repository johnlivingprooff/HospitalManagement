package validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.Length;
import app.core.validation.validators.StringValidationFilter;
import app.util.LocaleUtil;

import java.util.List;

/**
 * This filter does not have length constrains and so should be used in combination with the {@link Length} filter.
 */
@Validator(name = "name")
public class Name extends StringValidationFilter {

    @Override
    public String apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) return null;
        try {
            final String regex = "^([a-zA-Z]+('|\\s)?)+$";
            if (!input.matches(regex)) {
                throw new Exception();
            }
            return input;
        } catch (Exception e) {
            throw new DataValidator.ValidationException(name + " contains invalid characters. Valid characters are a-z, A-Z, ', and space.");
        }
    }
}
