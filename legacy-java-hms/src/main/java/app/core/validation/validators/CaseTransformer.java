package app.core.validation.validators;

import app.core.validation.DataValidator;
import app.util.LocaleUtil;

import java.util.List;

public class CaseTransformer extends StringValidationFilter {
    private interface Transformer {
        String transform(String input);
    }

    enum Transformation implements Transformer {
        upper() {
            @Override
            public String transform(String input) {
                return input.toUpperCase();
            }
        },
        lower() {
            @Override
            public String transform(String input) {
                return input.toLowerCase();
            }
        }
    }

    private Transformation transformation;

    CaseTransformer(Transformation transformation) {
        this.transformation = transformation;
    }

    @Override
    public String apply(String name, String input, List<String> args) throws DataValidator.ValidationException {
        if (!LocaleUtil.isNullOrEmpty(input)) {
            return transformation.transform(input);
        }
        return input;
    }
}
