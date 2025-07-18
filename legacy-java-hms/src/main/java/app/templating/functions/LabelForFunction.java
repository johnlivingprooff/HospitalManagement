package app.templating.functions;

import app.core.templating.HelperFunction;
import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

import java.util.List;

@TemplateFunction
public class LabelForFunction extends SimpleJtwigFunction implements HelperFunction<FunctionRequest> {

    @Override
    public String name() {
        return "labelFor";
    }

    @Override
    public String execute(FunctionRequest functionRequest) {
        List<Object> args = functionRequest.minimumNumberOfArguments(2)
                .maximumNumberOfArguments(3).getArguments();
        final String elementId = args.get(0).toString();
        final String label = args.get(1).toString();
        final boolean required = args.size() == 3 ? Boolean.valueOf(args.get(2).toString()) : false;
        return "<label " + (required ? "class=\"required\"" : "") + " for=\"" + elementId + "\">" + label + "</label>";
    }
}
