package app.core;

import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Method;

class ActionMethodWrapper implements Route {
    private final Method method;
    private final String permission;
    private final Controller controller;
    private final boolean checkPermission;

    ActionMethodWrapper(Controller controller, Method method, String permission, boolean checkPermission) {
        this.method = method;
        this.permission = permission;
        this.controller = controller;
        this.checkPermission = checkPermission;
        verifyMethod();
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (checkPermission) {
            if (controller.checkPermission(permission, request, response)) {
                return method.invoke(controller, request, response);
            } else {
                return controller.notAuthorized(response);
            }
        } else {
            return method.invoke(controller, request, response);
        }
    }

    private void verifyMethod() {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (!(parameterTypes.length == 2 && parameterTypes[0] == Request.class && parameterTypes[1] == Response.class)) {
            throw new IllegalArgumentException(method + " must have " + Request.class + " and " + Response.class + " parameters");
        }
    }

    @Override
    public String toString() {
        return controller.getClass().getCanonicalName() + "#" + method.getName();
    }
}