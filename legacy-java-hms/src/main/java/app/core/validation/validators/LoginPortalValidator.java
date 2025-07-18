package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.models.auth.Portal;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "portal")
public class LoginPortalValidator implements DataValidator.ValidationFilter<String> {
    @Override
    public Portal apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) {
            return null;
        }
        try {
            return Portal.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Invalid login portal.");
        }
    }
}
