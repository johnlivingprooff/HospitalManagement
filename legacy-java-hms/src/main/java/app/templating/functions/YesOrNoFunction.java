package app.templating.functions;

import app.core.templating.HelperFunction;
import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class YesOrNoFunction implements HelperFunction<FunctionRequest> {
    @Override
    public String name() {
        return "yesOrNo";
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        List<Object> args = functionRequest.getArguments();
        return (args != null && args.size() == 1)
                && Boolean.valueOf(String.valueOf(args.get(0))) ? "Yes" : "No";
    }
}
