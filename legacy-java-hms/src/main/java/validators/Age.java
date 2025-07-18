package validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.ValidationFilterImpl;
import app.util.DateUtils;

import java.util.Date;
import java.util.List;

@Validator(name = "age")
public class Age extends ValidationFilterImpl<Date, Date> {

    @Override
    public Date apply(String name, Date input, List<String> args) throws DataValidator.ValidationException {
        final int minAge;

        try {
            minAge = !args.isEmpty() ? Integer.valueOf(args.get(0)) : 18;
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Misconfiguration error for field: " + name);
        }

        try {
            int startYear = DateUtils.getYear(input);
            int currentYear = DateUtils.getYear(new Date());

            if (currentYear - startYear < minAge) {
                throw new Exception();
            }
            return input;
        } catch (Exception e) {
            throw new DataValidator.ValidationException(name + " must be at least " + minAge + " years ago");
        }
    }
}
