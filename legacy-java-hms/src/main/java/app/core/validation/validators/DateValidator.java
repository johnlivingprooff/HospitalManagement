package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Validator(name = "date")
public class DateValidator extends ValidationFilterImpl<String, Date> {

    @Override
    public Date apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) return null;
        final Date today;
        final Date date;
        final DateComparators comparison;
        final String type = args.size() >= 1 ? args.get(0) : DateComparators.None.name();

        today = new Date();

        try {
            comparison = DateComparators.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new DataValidator.ValidationException("Date function misconfiguration: " +
                    "Unknown comparison flag specified (" + type + " for field: " + name + ")");
        }

        try {
            date = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US).parse(input);
        } catch (ParseException e) {
            throw new DataValidator.ValidationException(name + " has an invalid date format");
        }

        return comparison.compare(name, date, today);
    }

    interface DateComparator {
        Date compare(String name, Date theDate, Date today) throws DataValidator.ValidationException;
    }

    public enum DateComparators implements DateComparator {
        None {
            @Override
            public Date compare(String name, Date theDate, Date today) {
                return theDate;
            }
        },
        Equal {
            @Override
            public Date compare(String name, Date theDate, Date today) throws DataValidator.ValidationException {
                if (LocaleUtil.compareDatesOnly(theDate, today) == 0) {
                    return theDate;
                }
                throw new DataValidator.ValidationException(name + " must be today");
            }
        },
        LessThan {
            @Override
            public Date compare(String name, Date theDate, Date today) throws DataValidator.ValidationException {
                if (LocaleUtil.compareDatesOnly(theDate, today) < 0) {
                    return theDate;
                }
                throw new DataValidator.ValidationException(name + " may only be before today");
            }
        },
        GreaterThan {
            @Override
            public Date compare(String name, Date theDate, Date today) throws DataValidator.ValidationException {
                if (LocaleUtil.compareDatesOnly(theDate, today) > 0) {
                    return theDate;
                }
                throw new DataValidator.ValidationException(name + " may only be after today");
            }
        },
        LessThanOrEqualTo {
            @Override
            public Date compare(String name, Date theDate, Date today) throws DataValidator.ValidationException {
                if (LocaleUtil.compareDatesOnly(theDate, today) <= 0) {
                    return theDate;
                }
                throw new DataValidator.ValidationException(name + " must at most be today or before");
            }
        },
        GreaterThanOrEqualTo {
            @Override
            public Date compare(String name, Date theDate, Date today) throws DataValidator.ValidationException {
                if (LocaleUtil.compareDatesOnly(theDate, today) >= 0) {
                    return theDate;
                }
                throw new DataValidator.ValidationException(name + " must be at least today or after");
            }
        }
    }
}
