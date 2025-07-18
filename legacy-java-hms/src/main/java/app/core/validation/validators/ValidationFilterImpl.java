package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.util.ReflectionUtil;

import java.util.List;

public abstract class ValidationFilterImpl<T, R> implements DataValidator.ValidationFilter<T> {

    public abstract R apply(String name, T input, List<String> args) throws DataValidator.ValidationException;

    protected static boolean isArray(Object o) {
        return ReflectionUtil.isArray(o);
    }

    protected static boolean isString(Object o) {
        return o instanceof String;
    }
}
