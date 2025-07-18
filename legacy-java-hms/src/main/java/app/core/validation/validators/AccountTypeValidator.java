package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.core.validation.Validator;
import app.models.account.AccountType;
import app.util.LocaleUtil;

import java.util.List;

@Validator(name = "accountType")
public class AccountTypeValidator implements DataValidator.ValidationFilter<String> {
    @Override
    public AccountType apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (LocaleUtil.isNullOrEmpty(input)) {
            return null;
        }
        try {
            return AccountType.valueOf(input);
        } catch (Exception e) {
            throw new DataValidator.ValidationException("Invalid account type.");
        }
    }
}
