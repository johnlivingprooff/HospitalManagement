package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.models.permission.Permission;
import app.util.ElementBuilder;
import app.util.PermissionUtil;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

/**
 * <p>Example usage</p>
 * <pre>
 *     protectedDropdownNavItem('/my/link', 'Link Label', 'Access My Links', userPermissions)
 * </pre>
 */
@TemplateFunction
public class ProtectedDropdownNavItem extends HelperFunctionImpl {

    public ProtectedDropdownNavItem() {
        super("protectedDropdownNavItem");
    }

    @Override
    public Object execute(FunctionRequest parameters) {
        String link;
        String label;
        String permission;
        List<Object> arguments;
        String[] accessPermissions;
        List<Permission> userPermissions;

        arguments = parameters.minimumNumberOfArguments(4).maximumNumberOfArguments(4).getArguments();

        link = (String) arguments.get(0);
        label = (String) arguments.get(1);
        permission = (String) arguments.get(2);
        // noinspection unchecked
        userPermissions = (List<Permission>) arguments.get(3);

        accessPermissions = permission.split("/");

        if (PermissionUtil.hasPermissions(accessPermissions, userPermissions)) {
            return new ElementBuilder("a")
                    .classes("dropdown-item")
                    .attribute("href", link)
                    .text(escape(label, parameters.getEnvironment()))
                    .build();
        }
        return null;
    }
}


