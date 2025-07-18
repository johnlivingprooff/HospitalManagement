package app.core.factories;

import app.core.Context;
import app.core.Controller;
import app.core.annotations.RouteController;

public interface ControllerFactory {
    <C extends Controller> C getInstance(Class<C> type, RouteController meta, Context context) throws Exception;
}
