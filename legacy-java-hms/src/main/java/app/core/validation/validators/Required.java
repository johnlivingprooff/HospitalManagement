package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "required")
public class Required extends ValidationFilterImpl<Object, Object> {

    @Override
    public Object apply(String name, Object input, List<String> args) throws DataValidator.ValidationException {

        if (isString(input)) {
            if (!LocaleUtil.isNullOrEmpty((String) input)) {
                return input;
            }
        } else if (input != null) {
            return input;
        }

        throw new DataValidator.ValidationException(name + " field is required");
    }
}
