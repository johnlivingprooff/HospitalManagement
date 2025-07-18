package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.models.patient.PatientType;

import java.util.List;

@Validator(name = "patientType")
public class PatientTypeValidator implements DataValidator.ValidationFilter<String> {
    @Override
    public PatientType apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        try {
            return PatientType.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Invalid patient type.");
        }
    }
}
