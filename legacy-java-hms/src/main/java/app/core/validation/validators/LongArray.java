package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;

import java.util.*;

@Validator(name = "longArray")
public class LongArray extends ValidationFilterImpl<String[], long[]> {

    public long[] apply(String name, String[] input, List<String> args) throws DataValidator.ValidationException {
        final Set<String> set;
        final long[] array;

        if (input == null) {
            // Return null without instantiating any object because they end up getting discarded
            return null;
        }

        set = new LinkedHashSet<>(Arrays.asList(input)); // <-- to remove duplicates
        array = new long[set.size()];

        try {
            int i = 0;
            for (String value : set) {
                array[i++] = Long.valueOf(value);
            }
        } catch (Exception e) {
            throw new DataValidator.ValidationException(String.format(Locale.US,
                    "One of the values contained in the %s field is not a valid number", name));
        }

        return array;
    }
}
