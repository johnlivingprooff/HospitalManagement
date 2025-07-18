package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;

import java.util.List;

@Validator(name = "positive")
public class PositiveNumber<T extends Number> extends ValidationFilterImpl<T, T> {

    @Override
    public T apply(String name, T input, List<String> args) throws DataValidator.ValidationException {
        if (input.doubleValue() < 0.00) throw new DataValidator.ValidationException(name + " must be a positive number");
        return input;
    }
}
