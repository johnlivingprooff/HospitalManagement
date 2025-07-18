package validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.core.validation.validators.ValidationFilterImpl;
import app.models.patient.BloodGroup;

import java.util.List;

@Validator(name = "bloodGroup")
public class BloodGroupValidator extends ValidationFilterImpl<String, BloodGroup> {

    @Override
    public BloodGroup apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        try {
            return BloodGroup.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException(name + " has invalid blood group value.");
        }
    }
}
