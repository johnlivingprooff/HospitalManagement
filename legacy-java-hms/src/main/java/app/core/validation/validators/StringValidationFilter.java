package app.core.validation.validators;

import app.core.validation.DataValidator;

import java.util.List;

public abstract class StringValidationFilter extends ValidationFilterImpl<String, String> {
    protected static final String EMPTY_STRING = "";

    @Override
    public abstract String apply(String name, String input, List<String> args) throws DataValidator.ValidationException;
}
