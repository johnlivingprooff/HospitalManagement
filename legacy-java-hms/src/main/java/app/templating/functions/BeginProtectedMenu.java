package app.templating.functions;

import app.core.templating.TemplateFunction;
import app.models.permission.Permission;
import app.util.ElementBuilder;
import app.util.PermissionUtil;
import org.jtwig.functions.FunctionRequest;

import java.util.List;

@TemplateFunction
public class BeginProtectedMenu extends HelperFunctionImpl {
    public BeginProtectedMenu() {
        super("beginProtectedMenu");
    }

    @Override
    public Object execute(FunctionRequest parameters) {
        String id;
        String label;
        String fontAwesomeIcon;
        String permission;
        List<Permission> permissions;
        List<Object> arguments;

        arguments = parameters.minimumNumberOfArguments(5).maximumNumberOfArguments(5).getArguments();

        id = (String) arguments.get(0);
        label = (String) arguments.get(1);
        fontAwesomeIcon = (String) arguments.get(2);
        permission = (String) arguments.get(3);
        // noinspection unchecked
        permissions = (List<Permission>) arguments.get(4);

        if (PermissionUtil.hasPermission(permission, permissions)) {
            /**
             * <li class="nav-item dropdown">
             *                     <a class="nav-link dropdown-toggle"
             *                        href="#" id="ID"
             *                        role="button"
             *                        data-toggle="dropdown"
             *                        aria-haspopup="true" aria-expanded="false">
             *                         <i class="FONT-AWESOME-ICON"></i>
             *                         LABEL
             *                     </a>
             *                     <div class="dropdown-menu" aria-labelledby="ID">
             */
            ElementBuilder menu = new ElementBuilder("li", false)
                    .classes("nav-item dropdown")
                    .addChild(
                            new ElementBuilder("a")
                                    .classes("nav-link dropdown-toggle")
                                    .attribute("href", "#")
                                    .attribute("id", id)
                                    .attribute("role", "button")
                                    .attribute("data-toggle", "dropdown")
                                    .attribute("aria-hasPopup", true)
                                    .attribute("aria-expanded", false)
                                    .text(label, false)
                                    .addChild(
                                            new ElementBuilder("i")
                                                    .classes(fontAwesomeIcon)
                                    )
                    ).addChild(
                            new ElementBuilder("div", false)
                                    .classes("dropdown-menu")
                                    .attribute("aria-labelledBy", id)
                    );
            return menu.build();
        }
        return null;
    }
}
