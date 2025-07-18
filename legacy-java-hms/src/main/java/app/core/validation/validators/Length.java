package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;
import app.util.ReflectionUtil;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Validator(name = "length")
public class Length extends ValidationFilterImpl<Object, Object> {

    @Override
    public Object apply(String name, Object input, List<String> args) throws DataValidator.ValidationException {
        final int min, max, diff;

        if (input == null) {
            return null;
        }

        try {
            min = Integer.parseInt(args.get(0));
            max = Integer.parseInt(args.get(1));
            diff = max - min;
        } catch (Exception e) {
            throw new DataValidator.ValidationException("LENGTH filter argument error. " +
                    "Filter function requires minimum and maximum length values to be valid integers", e);
        }

        if (diff < 0) {
            throw new DataValidator.ValidationException("Difference between minimum and maximum number of " +
                    "possible items for field '" + name + "' must at least be 1");
        }

        // check type and length (We do not check for null here, that should be done in "required" function
        if (isString(input)) {
            if (!LocaleUtil.isNullOrEmpty((String) input)) {
                if (!LocaleUtil.isStringLengthWithinRange(input.toString(), min, max)) {
                    throw new DataValidator.ValidationException(String.format(Locale.US,
                            "%s must be within %,d and %,d characters long", name, min, max));
                }
            }
            return input;
        }

        // Check if the object is an array or collection
        final boolean array;
        if ((array = isArray(input)) || ReflectionUtil.isCollection(input)) {
            if (array) {
                if (ReflectionUtil.isArrayLengthWithinRange(input, min, max)) {
                    return input;
                }
            } else {
                if (ReflectionUtil.isCollectionSizeWithinRange((Collection) input, min, max)) {
                    return input;
                }
            }

            // boundary constraint check failed
            throw new DataValidator.ValidationException(String.format(Locale.US,
                    "%s field must contain at least %,d items and no more than %,d.", name, min, max));
        }

        // Type not supported
        throw new DataValidator.ValidationException(String.format(Locale.US,
                "There was an error processing the %s field. (Unsupported type %s)",
                name, input.getClass().getCanonicalName()));
    }
}
