package app.templating.functions;

import app.core.templating.HelperFunction;
import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;

import java.util.List;
import java.util.Locale;

@TemplateFunction
public class NumberPrintFunction implements HelperFunction<FunctionRequest> {
    @Override
    public String name() {
        return "printNumber";
    }

    @Override
    public Object execute(FunctionRequest request) {
        List<Object> args = request.getArguments();
        if (args != null && args.size() >= 1) {
            final double value = Double.valueOf(String.valueOf(args.get(0)));
            final int precision = args.size() >= 2 ? (int) args.get(1) : 0;
            String format = "%,." + precision + "f";
            return String.format(Locale.US, format, value);
        }
        throw new IllegalArgumentException("Expected number to print but no argument given");
    }
}
