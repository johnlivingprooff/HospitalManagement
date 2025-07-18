package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;
import java.util.Locale;

@Validator(name = "alpha")
public class AlphaOnly extends StringValidationFilter {

    public String apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (!LocaleUtil.isNullOrEmpty(input)) {
            if (!LocaleUtil.isAlphaText(input)) {
                throw new DataValidator.ValidationException(String.format(Locale.US, "%s must only contain letters", name));
            }
            return input;
        }
        return EMPTY_STRING;
    }
}
