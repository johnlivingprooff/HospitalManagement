package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "text")
public class Text extends StringValidationFilter {

    @Override
    public String apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        final boolean multiline = args.size() >= 1 && Boolean.valueOf(args.get(0));
        if (!LocaleUtil.isNullOrEmpty(input)) {
            final String pattern;
            if (multiline) {
                pattern = "^[\\x20-\\x3B=\\x3F-\\x7F\\x0A\\x0D]+$";
            } else {
                pattern = "^[\\x20-\\x3B=\\x3F-\\x7F]+$";
            }
            if (input.matches(pattern)) {
                return input;
            }
            throw new DataValidator.ValidationException(name + " contains invalid characters");
        }
        return null;
    }
}
