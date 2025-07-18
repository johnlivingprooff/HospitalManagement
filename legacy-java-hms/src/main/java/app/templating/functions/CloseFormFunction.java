package app.templating.functions;

import app.core.templating.HelperFunction;
import app.core.templating.TemplateFunction;
import org.jtwig.functions.FunctionRequest;

@TemplateFunction
public class CloseFormFunction implements HelperFunction<FunctionRequest> {
    @Override
    public String name() {
        return "closeForm";
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        return "</form>";
    }
}
