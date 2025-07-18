package app.templating.functions;

import app.util.ElementBuilder;
import app.util.LocaleUtil;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

public class MessageFunction extends HelperFunctionImpl {
    final AlertType alertType;

    MessageFunction(AlertType alertType, String name) {
        super(name);
        this.alertType = alertType;
    }

    @Override
    public Object execute(FunctionRequest parameters) {
        List<Object> args = parameters.minimumNumberOfArguments(1).maximumNumberOfArguments(2).getArguments();
        if (args != null && args.size() >= 1) {
            Object o = args.get(0);
            if (!isUndefinedOrNull(o)) {
                String message = (String) o;
                if (!LocaleUtil.isNullOrEmpty(message)) {
                    final String extras = args.size() >= 2 ? args.get(1).toString() : "";
                    return alertMessagePanel(escape(message, parameters.getEnvironment()), extras, alertType);
                }
            }
        }
        return "";
    }

    private static String alertMessagePanel(String message, String extras, AlertType alertType) {
        return new ElementBuilder("div")
                .classes("alert " + alertType.cssClassName + " " + extras)
                .attribute("role", "alert")
                .addChild(
                        new ElementBuilder("p")
                                .text(message)
                ).build();
    }

    enum AlertType {
        Light("alert-light"),
        Dark("alert-dark"),
        Info("alert-info"),
        Danger("alert-danger"),
        Warning("alert-warning"),
        Success("alert-success");

        AlertType(String cssClassName) {
            this.cssClassName = cssClassName;
        }

        final String cssClassName;
    }
}
