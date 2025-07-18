package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.util.LocaleUtil;
import org.jtwig.functions.FunctionRequest;

import java.math.BigDecimal;
import java.util.List;

@TemplateFunction
public class BigDecimalPrint extends HelperFunctionImpl {
    public BigDecimalPrint() {
        super("printBigDecimal");
    }


    @Override
    public Object execute(FunctionRequest request) {
        List<Object> args = request.minimumNumberOfArguments(1).getArguments();
        BigDecimal value = getArgument(args, 0, BigDecimal.class, BigDecimal.ZERO);
        return LocaleUtil.dec2str(value);
    }
}
