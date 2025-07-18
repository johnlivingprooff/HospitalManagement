package app.templating.functions;

import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
@Deprecated
public class DatePickerFunction extends HelperFunctionImpl {
    public DatePickerFunction() {
        super("datePicker");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        final List<Object> args = functionRequest.minimumNumberOfArguments(3)
                .maximumNumberOfArguments(5)
                .getArguments();
        final String id = (String) args.get(0);
        final String name = (String) args.get(1);
        final String dateValue = String.valueOf(args.get(2));
        final String classes = "form-control" + (args.size() >= 4 ? " " + args.get(3) : "");
        final boolean required = args.size() >= 5 && (Boolean) args.get(4);

        String markup = "<input autofocus type=date";
        markup += " id=" + escape(id, functionRequest.getEnvironment());
        markup += " name= " + escape(name, functionRequest.getEnvironment());
        if (required) {
            markup += " required";
        }
        markup += " class=" + escape(classes, functionRequest.getEnvironment());
        markup += " value=" + dateValue + ">";
        return markup;
    }
}
