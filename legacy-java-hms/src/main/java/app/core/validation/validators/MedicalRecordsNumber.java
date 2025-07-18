package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "mrn")
public class MedicalRecordsNumber extends StringValidationFilter {
    @Override
    public String apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) return null;
        try {
            if(input.matches("^[a-zA-Z]{4}[a-fA-F0-9]{3,16}$")){
                return input;
            }
            throw new DataValidator.ValidationException(name + " is not a valid patient ID.");
        }catch (Exception e){
            if(e instanceof DataValidator.ValidationException){
                throw e;
            }else{
                throw new DataValidator.ValidationException(name + " is not a valid patient ID.");
            }
        }
    }
}
