package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "email")
public class Email extends StringValidationFilter {

    @Override
    public String apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (!LocaleUtil.isNullOrEmpty(input)) {
            if (!input.matches("^[a-z0-9]+(([._])?[a-z0-9])+@[a-z0-9]+(\\.[a-z]+)?$")) {
                throw new DataValidator.ValidationException(name + " format is invalid");
            }
            return input;
        }
        return EMPTY_STRING;
    }
}
