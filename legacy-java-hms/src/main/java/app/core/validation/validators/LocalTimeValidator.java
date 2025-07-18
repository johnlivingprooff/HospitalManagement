package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Validator(name = "localTime")
public class LocalTimeValidator extends ValidationFilterImpl<String, LocalTime> {

    @Override
    public LocalTime apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) return null;
        final String pattern;

        pattern = args.size() >= 1 ? args.get(0) : "HH:mm";

        try {
            return LocalTime.parse(input, DateTimeFormatter.ofPattern(pattern));
        } catch (IllegalArgumentException e) {
            throw new DataValidator.ValidationException("Invalid time format for " + name);
        }
    }
}
