package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;
import java.util.Locale;

@Validator(name = "alphanumeric")
public class Alphanumeric extends StringValidationFilter {

    @Override
    public String apply(String name, String input, List args) throws DataValidator.ValidationException {
        if (!LocaleUtil.isNullOrEmpty(input)) {
            if (!LocaleUtil.isAlphaNumericText(input)) {
                throw new DataValidator.ValidationException(String.format(Locale.US, "%s can only be alphanumeric", name));
            }
            return input;
        }
        return EMPTY_STRING;
    }
}
