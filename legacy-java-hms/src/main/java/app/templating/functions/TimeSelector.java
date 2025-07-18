package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.ElementBuilder;
import app.util.LocaleUtil;
import org.jtwig.functions.FunctionRequest;

import java.time.LocalTime;
import java.util.List;

@TemplateFunction
public class TimeSelector extends HelperFunctionImpl {
    public TimeSelector() {
        super("timeSelector");
    }

    // timeSelector(true, "id", value, [extra classes])

    @Override
    public Object execute(FunctionRequest functionRequest) {
        final List<Object> args = functionRequest.minimumNumberOfArguments(3)
                .maximumNumberOfArguments(4)
                .getArguments();
        final boolean required = getArgument(args, 0, Boolean.class, true);
        final String identifier = getArgument(args, 1, String.class, Long.toHexString(System.currentTimeMillis()));
        final String dateValue = getArgument(args, 2, String.class, LocaleUtil.timeToString(LocalTime.now()));
        final String classes = "form-control " + getArgument(args, 3, String.class, "");

        final ElementBuilder.Input input = new ElementBuilder
                .Input()
                .type("time")
                .id(identifier)
                .autofocus()
                .attribute("value", escape(dateValue, functionRequest.getEnvironment()))
                .classes(classes)
                .name(identifier);

        if (required) {
            input.attribute("required", "required");
        }

        return input.build();
    }
}
