package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.LocaleUtil;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class PluralStringFunction extends HelperFunctionImpl {

    public PluralStringFunction() {
        super("pluralString");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        List<Object> args = functionRequest.minimumNumberOfArguments(2)
                .maximumNumberOfArguments(2).getArguments();
        return LocaleUtil.pluralString(
                Long.valueOf(args.get(0).toString()),
                String.valueOf(args.get(1))
        );
    }
}
