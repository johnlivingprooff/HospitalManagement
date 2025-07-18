package app.templating.functions;

import app.core.annotations.HtmlFieldDisplay;
import app.core.templating.TemplateFunction;
import app.util.ReflectionUtil;
import org.jtwig.functions.FunctionRequest;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@TemplateFunction
public class MultipleSelectionDropdownFunction extends HelperFunctionImpl {
    public MultipleSelectionDropdownFunction() {
        super("multiFormDropDown");
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        try {
            //                       0  1   2     3         4          5
            // multiFormDropDown(items, id, name, selected, className, required)
            final List<Object> args =
                    functionRequest.minimumNumberOfArguments(1).maximumNumberOfArguments(6).getArguments();
            final Object items = args.get(0);
            final String id = args.size() >= 2 ? String.valueOf(args.get(1)) : null;
            final String name = args.size() >= 3 ? String.valueOf(args.get(2)) : null;
            final Object selectionList = args.size() >= 4 ? args.get(3) : null;
            final String className = "custom-select " + (args.size() >= 5 ? String.valueOf(args.get(4)) : "");
            final boolean required = args.size() == 6 && (boolean) args.get(5);

            final StringBuilder sb = new StringBuilder("<select multiple class=")
                    .append('"')
                    .append(className)
                    .append('"');

            if (id != null) {
                sb.append(" id=").append('"').append(id).append('"');
            }

            if (name != null) {
                sb.append(" name=").append('"').append(name).append('"');
            }

            if (required) {
                sb.append(" required");
            }

            // close
            sb.append('>');

            if (items instanceof Map) {
                // If it's a map, simply use the natural key:value pair mappings
                for (Object key : ((Map) items).keySet()) {
                    sb.append("<option value=").append('"').append(key.toString()).append('"');
                    if (itemInList(key, selectionList)) {
                        sb.append(" selected");
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
                    sb.append("<option value=").append('"').append(value).append('"');
                    if (itemInList(value, selectionList)) {
                        sb.append(" selected");
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
                    if (itemInList(value, selectionList)) {
                        sb.append(" selected");
                    }
                    sb.append(">")
                            .append(escape(getDisplayText(item, fieldDisplay), functionRequest.getEnvironment()))
                            .append("</option>");
                }
            } else {
                throw new IllegalArgumentException("Items must be iterable or array");
            }

            return sb.append("</select>").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<select class=\"custom-select\"><option selected disabled>Error in formDropDown</option></select>";
        }
    }
}
