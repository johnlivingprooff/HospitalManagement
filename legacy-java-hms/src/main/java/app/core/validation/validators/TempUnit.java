package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.models.patient.TemperatureUnits;

import java.util.List;

@Validator(name = "tempUnit")
public class TempUnit implements DataValidator.ValidationFilter<String> {
    @Override
    public TemperatureUnits apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        try {
            return TemperatureUnits.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Invalid temperature units.");
        }
    }
}
