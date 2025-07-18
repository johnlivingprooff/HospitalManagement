package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.util.LocaleUtil;

import java.math.BigDecimal;
import java.util.List;

public class NumericValidator<R extends Number> extends ValidationFilterImpl<String, R> {
    private final Class<R> type;

    public NumericValidator(Class<R> type) {
        this.type = type;
    }

    @Override
    public R apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (!LocaleUtil.isNullOrEmpty(input)) {
            BigDecimal value;
            try {
                value = new BigDecimal(input.replaceAll("[^0-9.]", ""));
            } catch (Exception e) {
                throw new DataValidator.ValidationException(name + " must be a valid numeric value");
            }
            return cast(value, name);
        }
        // In case this filter is not chained with the required filter, simply return 0
        return cast((double) 0, name);
    }

    private <V extends Number> R cast(V input, String name) throws DataValidator.ValidationException {
        switch (getType()) {
            case TYPE_DECIMAL:
                return type.cast(input);
            case TYPE_DOUBLE:
                return type.cast(input.doubleValue());
            case TYPE_FLOAT:
                return type.cast(input.floatValue());
            case TYPE_INT:
                return type.cast(input.intValue());
            case TYPE_LONG:
                return type.cast(input.longValue());
            case TYPE_SHORT:
                return type.cast(input.shortValue());
            case TYPE_UNKNOWN:
            default:
                throw new DataValidator.ValidationException("Unsupported type for field " + name);
        }
    }

    final int getType() {
        if (type == BigDecimal.class) return TYPE_DECIMAL;
        if (type == Double.TYPE || type == double.class || type == Double.class) return TYPE_DOUBLE;
        if (type == Float.TYPE || type == float.class || type == Float.class) return TYPE_FLOAT;
        if (type == Integer.TYPE || type == int.class || type == Integer.class) return TYPE_INT;
        if (type == Short.TYPE || type == short.class || type == Short.class) return TYPE_SHORT;
        if (type == Long.TYPE || type == long.class || type == Long.class) return TYPE_LONG;
        return TYPE_UNKNOWN;
    }

    private final static int TYPE_UNKNOWN = -1;
    private final static int TYPE_SHORT = 0;
    private final static int TYPE_INT = 1;
    private final static int TYPE_LONG = 2;
    private final static int TYPE_FLOAT = 3;
    private final static int TYPE_DOUBLE = 4;
    private final static int TYPE_DECIMAL = 5;
}
