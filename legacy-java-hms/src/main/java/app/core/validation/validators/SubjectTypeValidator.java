package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.models.Subject;

import java.util.List;

@Validator(name = "SubjectTypeValidator")
public final class SubjectTypeValidator extends ValidationFilterImpl<String, Subject.SubjectType> {
    public static final String FLAG_USE_ORDINAL = "UseOrdinal";
    public static final String FLAG_USE_NAME = "UseName";

    private interface SubjectTypeConverter {
        Subject.SubjectType convert(String input) throws Exception;
    }

    private enum Strategy implements SubjectTypeConverter {
        UseOrdinal {
            @Override
            public Subject.SubjectType convert(String input) {
                return Subject.SubjectType.VALUES[Integer.parseInt(input)];
            }
        },
        UseName {
            @Override
            public Subject.SubjectType convert(String input) {
                return Subject.SubjectType.valueOf(input);
            }
        }
    }

    @Override
    public Subject.SubjectType apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        Strategy strategy;
        Subject.SubjectType subjectType;

        if (args.isEmpty()) {
            strategy = Strategy.UseName;
        } else {
            try {
                strategy = Strategy.valueOf(args.get(0));
            } catch (Exception e) {
                throw new DataValidator.ValidationException("Invalid resolution strategy in filter configuration for field " + name);
            }
        }

        try {
            subjectType = strategy.convert(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException(name + " contains invalid subject type value");
        }
        return subjectType;
    }
}
