package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.LocaleUtil;
import org.jtwig.functions.FunctionRequest;

import java.time.LocalTime;
import java.util.List;

@TemplateFunction
public class TimeFormatFunction extends HelperFunctionImpl {
    public TimeFormatFunction() {
        super("formatTime");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        List<Object> args = functionRequest.minimumNumberOfArguments(1).maximumNumberOfArguments(1).getArguments();
        final Object o = args.get(0);

        if (o instanceof LocalTime) {
            return LocaleUtil.formatTime((LocalTime) o);
        }
        return "";
    }
}
