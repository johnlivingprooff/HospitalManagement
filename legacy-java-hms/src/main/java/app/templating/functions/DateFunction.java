package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.LocaleUtil;
import org.jtwig.functions.FunctionRequest;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@TemplateFunction
public class DateFunction extends HelperFunctionImpl {
    public DateFunction() {
        super("date");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        List<Object> args;
        Object temporalObject;

        args = functionRequest.getArguments();
        temporalObject = getArgument(args, 0, Object.class, null);

        if (!isUndefinedOrNull(temporalObject)) {
            if (temporalObject instanceof Date) {
                return new SimpleDateFormat("yyyy-MM-dd").format((Date) temporalObject);
            } else if (temporalObject instanceof LocalDate) {
                return LocaleUtil.formatDate((LocalDate) temporalObject);
            }
        }
        return "";
    }
}
