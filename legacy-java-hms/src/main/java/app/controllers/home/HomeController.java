package app.controllers.home;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import spark.Request;
import spark.Response;

@RouteController
@SuppressWarnings("unused")
public final class HomeController extends Controller {

    @Action(path = "/Hms/Home", checkPermission = false)
    public String index(Request request, Response response) {
        return renderView("home/Home.html", createModel(request));
    }
}
