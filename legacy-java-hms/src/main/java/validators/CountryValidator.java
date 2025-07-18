package validators;

import app.core.Country;
import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.ValidationFilterImpl;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "country")
public class CountryValidator extends ValidationFilterImpl<String, Country> {

    @Override
    public Country apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) {
            return null;
        }
        try {
            return Country.valueOf(input.toUpperCase());
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Unknown country selected for " + name);
        }
    }
}
