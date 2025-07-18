package validators;

import lib.gintec_rdl.jbeava.validation.ValidationFilter;
import lib.gintec_rdl.jbeava.validation.ValidatorFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HmsValidatorFactory implements ValidatorFactory {
    private static final Map<String, ValidationFilter<?, ?>> filterMap = new ConcurrentHashMap<>() {{
        put("email", new EmailFilter());
        put("mrn", new MRNFilter());
    }};

    @Override
    public ValidationFilter<?, ?> get(String name) {
        return filterMap.get(name);
    }
}
