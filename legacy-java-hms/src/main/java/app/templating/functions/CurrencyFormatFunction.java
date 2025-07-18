package app.templating.functions;

import app.core.templating.HelperFunction;
import app.core.templating.TemplateFunction;
import app.util.LocaleUtil;
import org.jtwig.functions.FunctionRequest;

import java.math.BigDecimal;
import java.util.Locale;

@TemplateFunction
public class CurrencyFormatFunction implements HelperFunction<FunctionRequest> {
    @Override
    public String name() {
        return "formatCurrency";
    }

    @Override
    public String execute(FunctionRequest request) {
        Object value = request.minimumNumberOfArguments(1).get(0);

        if (value != null) {
            if (value instanceof Double) {
                return String.format(Locale.US, "%,.02f", value);
            } else if (value instanceof BigDecimal) {
                return LocaleUtil.dec2str((BigDecimal) value);
            } else {
                throw new IllegalArgumentException("Unsupported type " + value.getClass());
            }
        }
        
        return null;
    }
}
