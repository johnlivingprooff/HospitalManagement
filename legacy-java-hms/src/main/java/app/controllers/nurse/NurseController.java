package app.controllers.nurse;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.permission.AclPermission;
import app.services.user.AccountService;
import spark.Request;
import spark.Response;

@RouteController(path = "/Hms/Nurses")
public final class NurseController extends Controller {

    @Inject
    private AccountService accountService;

    @Action(path = "/", permission = AclPermission.ReadNurses)
    public String getDoctors(Request request, Response response) {
        Model model;

        model = createModel(request);
        model.put("nurses", accountService.getNurses());
        return renderView("nurses/list.html", model);
    }
}
