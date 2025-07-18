package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Validator(name = "localDate")
public class LocalDateValidator extends ValidationFilterImpl<String, LocalDate> {

    @Override
    public LocalDate apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) return null;
        final String pattern;

        pattern = args.size() >= 1 ? args.get(0) : "yyyy-MM-dd";

        try {
            return LocalDate.parse(input, DateTimeFormatter.ofPattern(pattern));
        } catch (IllegalArgumentException e) {
            throw new DataValidator.ValidationException("Invalid date format for " + name);
        }
    }
}
