package app.templating.functions;

import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;

import java.util.List;
import java.util.Locale;

@TemplateFunction
public class FormatLong extends HelperFunctionImpl {
    public FormatLong() {
        super("formatLong");
    }

    @Override
    public Object execute(FunctionRequest request) {
        List<Object> args = request.minimumNumberOfArguments(1).getArguments();
        long value = getArgument(args, 0, Long.class, 0L);
        return String.format(Locale.US, "%,d", value);
    }
}
