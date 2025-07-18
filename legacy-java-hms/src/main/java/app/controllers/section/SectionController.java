package app.controllers.section;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.models.permission.AclPermission;
import app.services.section.SectionService;
import spark.Request;
import spark.Response;

import java.util.Map;

@RouteController(path = "/Hms/Sections")
@SuppressWarnings("unused")
public final class SectionController extends Controller {

    @Action(path = "/", permission = AclPermission.ReadSections)
    public String getSectionList(Request request, Response response) {
        Map<String, Object> model = createModel(request);
        model.put("sectionList", getService(SectionService.class).getVisibleSections());
        return renderView("sections/SectionList.html", model);
    }
}
