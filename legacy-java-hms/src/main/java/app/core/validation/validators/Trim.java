package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "trim")
public class Trim extends StringValidationFilter {

    @Override
    public String apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        return !LocaleUtil.isNullOrEmpty(input) ? input.trim() : input;
    }
}
