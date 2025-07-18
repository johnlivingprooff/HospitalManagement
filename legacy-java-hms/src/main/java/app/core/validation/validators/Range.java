package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;
import java.util.Locale;

@Validator(name = "range")
public class Range extends ValidationFilterImpl<Number, Number> {

    @Override
    public Number apply(String name, Number input, List<String> args) throws DataValidator.ValidationException {
        double min, max;
        if (input == null) return null;
        try {
            min = Double.parseDouble(args.get(0));
            max = Double.parseDouble(args.get(1));
            if (min > max) {
                throw new DataValidator.ValidationException("Lower bound range value must not overlap higher bound value.");
            }
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Expected 2 (min and max) parameters.");
        }
        if (!LocaleUtil.isNumberWithinRange(input.doubleValue(), min, max)) {
            throw new DataValidator.ValidationException(getErrorMessage(name, input, min, max));
        }
        return input;
    }

    private String getErrorMessage(String name, Number value, Number min, Number max) {
        String message = name + " must be between ";
        if (value instanceof Double || value instanceof Float) {
            message += String.format(Locale.US, "%.2f and %.2f.", min.doubleValue(), max.doubleValue());
        } else {
            message += String.format(Locale.US, "%d and %d.", min.longValue(), max.longValue());
        }
        return message;
    }
}
