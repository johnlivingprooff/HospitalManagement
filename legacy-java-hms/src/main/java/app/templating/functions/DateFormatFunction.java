package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.LocaleUtil;
import org.jtwig.functions.FunctionRequest;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@TemplateFunction
public class DateFormatFunction extends HelperFunctionImpl {
    public DateFormatFunction() {
        super("formatDate");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        List<Object> args = functionRequest.minimumNumberOfArguments(1).maximumNumberOfArguments(2).getArguments();
        final Object o = args.get(0);
        final boolean withTime = getArgument(args, 1, Boolean.class, false);

        if (o instanceof Date) {
            return LocaleUtil.formatDate((Date) o, withTime);
        } else if (o instanceof LocalDateTime) {
            return LocaleUtil.formatDate((LocalDateTime) o, withTime);
        }
        return "";
    }
}
