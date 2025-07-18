package validators;

import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.exceptions.JBeavaException;
import lib.gintec_rdl.jbeava.validation.filters.ValidationFilterImpl;
import org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator;

import java.util.List;

public class EmailFilter extends ValidationFilterImpl<String, String> {
    private static final String MESSAGE_TEMPLATE = "${name} contains an invalid email address.";

    public EmailFilter() {
        super("email");
    }

    @Override
    public String filter(String name, String message, String input, List<String> args) throws JBeavaException {
        if (LocaleUtil.isNullOrEmpty(input)) {
            return null;
        }
        if (!EmailAddressValidator.isValid(input)) {
            throw new JBeavaException(generateExceptionMessage(name, input, args, messageTemplate(message, MESSAGE_TEMPLATE)));
        }
        return input;
    }
}
