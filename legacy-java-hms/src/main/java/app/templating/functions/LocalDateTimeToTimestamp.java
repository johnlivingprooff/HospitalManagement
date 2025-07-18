package app.templating.functions;

import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@TemplateFunction
public class LocalDateTimeToTimestamp extends HelperFunctionImpl {
    public LocalDateTimeToTimestamp() {
        super("ldt2t");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        List<Object> args = functionRequest.minimumNumberOfArguments(1).maximumNumberOfArguments(1).getArguments();
        Object object = args.get(0);
        if (object instanceof LocalDateTime) {
            return ((LocalDateTime) object).toEpochSecond(ZoneOffset.UTC);
        }
        return null;
    }
}
