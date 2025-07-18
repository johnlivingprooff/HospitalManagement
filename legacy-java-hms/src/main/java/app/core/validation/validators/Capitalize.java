package app.core.validation.validators;

import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "capitalize")
public class Capitalize extends StringValidationFilter {

    @Override
    public String apply(String name, String input, List<String> args) {
        return !LocaleUtil.isNullOrEmpty(input) ? LocaleUtil.capitalize(input) : "";
    }
}
