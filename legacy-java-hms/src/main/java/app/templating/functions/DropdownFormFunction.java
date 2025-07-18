package app.templating.functions;

import app.core.annotations.HtmlFieldDisplay;
import app.core.templating.TemplateFunction;
import app.util.ElementBuilder;
import app.util.LocaleUtil;
import app.util.ReflectionUtil;
import org.jtwig.functions.FunctionRequest;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@TemplateFunction
public class DropdownFormFunction extends HelperFunctionImpl {
    public DropdownFormFunction() {
        super("formDropDown");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        try {
            // formDropdown(items, id, name, selected, className, required)
            final List<Object> args = functionRequest.minimumNumberOfArguments(1)
                    .maximumNumberOfArguments(6)
                    .getArguments();

            final Object items = args.get(0);
            final String id = args.size() >= 2 ? args.get(1).toString() : "";
            final String name = getArgument(args, 2, String.class, "");
            final Object selected = getArgument(args, 3, Object.class, "");
            final String className = "custom-select " + getArgument(args, 4, String.class, "");
            final String optionalLabel = getArgument(args, 5, String.class, "Select Option");
            boolean anItemWasSelected = false;

            final StringBuilder sb = new StringBuilder("<select required class=")
                    .append('"').append(className).append('"');

            if (!LocaleUtil.isNullOrEmpty(id)) {
                sb.append(" id=").append('"').append(id).append('"');
            }

            if (!LocaleUtil.isNullOrEmpty(name)) {
                sb.append(" name=").append('"').append(name).append('"');
            }

            sb.append('>');

            if (items instanceof Map) {
                // If it's a map, simply use the natural key:value pair mappings
                for (Object key : ((Map) items).keySet()) {
                    sb.append("<option value=").append('"').append(key.toString()).append('"');
                    if (valueEquals(key, selected)) {
                        sb.append(" selected");
                        anItemWasSelected = true;
                    }
                    final Object value = ((Map) items).get(key);
                    sb.append(">").append(escape(value.toString(), functionRequest.getEnvironment())).append("</option>");
                }
            } else if (items instanceof Iterable) {
                HtmlFieldDisplay fieldDisplay = null;
                for (Object item : (Iterable) items) {
                    if (fieldDisplay == null) {
                        fieldDisplay = Objects.requireNonNull(item.getClass().getAnnotation(HtmlFieldDisplay.class));
                    }
                    final Object value = getFieldValue(item, fieldDisplay);
                    final String extra = getFieldExtraValue(item, fieldDisplay);
                    sb.append("<option value=").append('"').append(value).append('"')
                            .append(" data-extra=").append('"').append(extra).append('"');
                    if (valueEquals(value, selected)) {
                        sb.append(" selected");
                        anItemWasSelected = true;
                    }
                    sb.append(">")
                            .append(escape(getDisplayText(item, fieldDisplay), functionRequest.getEnvironment()))
                            .append("</option>");
                }
            } else if (ReflectionUtil.isArray(items)) {
                HtmlFieldDisplay fieldDisplay = null;
                for (int i = 0; i < Array.getLength(items); i++) {
                    final Object item = Array.get(items, i);
                    if (fieldDisplay == null) {
                        fieldDisplay = Objects.requireNonNull(item.getClass().getAnnotation(HtmlFieldDisplay.class));
                    }
                    final Object value = getFieldValue(item, fieldDisplay);
                    sb.append("<option value=").append('"').append(value).append('"');
                    if (valueEquals(value, selected)) {
                        sb.append(" selected");
                        anItemWasSelected = true;
                    }
                    sb.append(">")
                            .append(escape(getDisplayText(item, fieldDisplay), functionRequest.getEnvironment()))
                            .append("</option>");
                }
            } else {
                throw new IllegalArgumentException("Items must be iterable or array");
            }
            // Add '.default-option' class for access from script
            sb.append("<option class=\"default-option\" disabled");
            if (!anItemWasSelected) {
                sb.append(" selected");
            }
            sb.append(">").append(escape(optionalLabel, functionRequest.getEnvironment())).append("</option>");
            return sb.append("</select>").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ElementBuilder("select")
                .classes("custom-select")
                .addChild(
                        new ElementBuilder("option")
                                .attribute("selected", "selected")
                                .attribute("disabled", "disabled")
                                .text("Error in formDropDown")
                ).build();
    }
}
