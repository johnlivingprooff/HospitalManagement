package app.templating.functions;

import app.core.templating.HelperFunction;
import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class MinFunction implements HelperFunction<FunctionRequest> {
    @Override
    public String name() {
        return "min";
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        List<Object> args = functionRequest.minimumNumberOfArguments(2).maximumNumberOfArguments(2).getArguments();
        return Math.min(Double.valueOf(args.get(0).toString()), Double.valueOf(args.get(1).toString()));
    }
}
