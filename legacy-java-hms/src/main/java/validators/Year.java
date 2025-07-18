package validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.ValidationFilterImpl;
import app.util.DateUtils;

import java.util.Date;
import java.util.List;

@Validator(name = "year")
public class Year extends ValidationFilterImpl<Integer, Integer> {

    @Override
    public Integer apply(String name, Integer input, List<String> args) throws DataValidator.ValidationException {
        final YearComparators yearComparator;

        try {
            yearComparator = !args.isEmpty() ? YearComparators.valueOf(args.get(0)) : YearComparators.None;
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Misconfiguration error for field: " + name);
        }
        return yearComparator.compare(name, input, DateUtils.getYear(new Date()));
    }

    interface YearComparator {
        int compare(String name, int theYear, int thisYear) throws DataValidator.ValidationException;
    }

    enum YearComparators implements YearComparator {
        None {
            @Override
            public int compare(String name, int theYear, int thisYear) {
                return theYear;
            }
        },
        SameAs {
            @Override
            public int compare(String name, int theYear, int thisYear) throws DataValidator.ValidationException {
                if (theYear == thisYear) return theYear;
                throw new DataValidator.ValidationException(name + " must be the same as this year (" + thisYear + ")");
            }
        },
        Before {
            @Override
            public int compare(String name, int theYear, int thisYear) throws DataValidator.ValidationException {
                if (theYear < thisYear) return theYear;
                throw new DataValidator.ValidationException(name + " may only come before this year (" + thisYear + ")");
            }
        },
        After {
            @Override
            public int compare(String name, int theYear, int thisYear) throws DataValidator.ValidationException {
                if (theYear > thisYear) return theYear;
                throw new DataValidator.ValidationException(name + " may only be after this year (" + thisYear + ")");
            }
        },
        BeforeOrSameAs {
            @Override
            public int compare(String name, int theYear, int thisYear) throws DataValidator.ValidationException {
                if (theYear <= thisYear) return theYear;
                throw new DataValidator.ValidationException(name + " may only be the same as or before this year (" + thisYear + ")");
            }
        },
        AfterOrSameAS {
            @Override
            public int compare(String name, int theYear, int thisYear) throws DataValidator.ValidationException {
                if (theYear >= thisYear) return theYear;
                throw new DataValidator.ValidationException(name + " may only be the same as or come after this year (" + thisYear + ")");
            }
        }
    }
}
