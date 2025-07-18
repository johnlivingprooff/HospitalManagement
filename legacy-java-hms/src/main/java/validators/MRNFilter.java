package validators;

import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.exceptions.JBeavaException;
import lib.gintec_rdl.jbeava.validation.filters.ValidationFilterImpl;

import java.util.List;

public class MRNFilter extends ValidationFilterImpl<String, String> {
    private static final String PATTERN = "^[a-zA-Z]{4}[a-fA-F0-9]{3,16}$";
    private static final String MESSAGE_TEMPLATE = "${value} is not a valid medical records number.";

    public MRNFilter() {
        super("mrn");
    }

    @Override
    public String filter(String name, String message, String input, List<String> list) throws JBeavaException {
        if (LocaleUtil.isNullOrEmpty(input)) {
            return null;
        }
        if (!input.matches(PATTERN)) {
            throw new JBeavaException(generateExceptionMessage(name, input, list,
                    messageTemplate(message, MESSAGE_TEMPLATE)));
        }
        return input;
    }
}
