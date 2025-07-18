package app.controllers.location;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.location.Region;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import app.services.location.LocationService;
import app.types.Bool;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.util.Date;

@RouteController(path = "/Hms/Regions")
@SuppressWarnings("unused")
public class RegionController extends Controller {

    @Action(path = "/", permission = AclPermission.ReadRegions)
    public String getRegions(Request request, Response response) {
        Model model;
        LocationService locationService;

        locationService = getService(LocationService.class);

        model = createModel(request);
        model.put("regions", locationService.getRegions(false));
        return renderView("location/region/list.html", model);
    }

    private String newRegionModel(Model model) {
        model.put("booleans", Bool.VALUES);
        return renderView("location/region/new.html", model);
    }

    private String editRegionModel(Model model, Region region) {
        model.put("id", region.getId());
        model.put("code", region.getCode());
        model.put("name", region.getName());
        model.put("active", region.isActive());
        model.put("booleans", Bool.VALUES);
        return renderView("location/region/edit.html", model);
    }

    private Region getSelectedRegion(Request request) {
        Long id;
        Region region;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((region = getService(LocationService.class).getRegionById(id)) != null) {
                if (!region.isHidden()) {
                    return region;
                }
            }
        }
        setSessionErrorMessage("Selected region does not exist", request);
        return null;
    }

    @Action(path = "/New", permission = AclPermission.WriteRegions)
    public String newRegion(Request request, Response response) {
        Model model;
        model = createModel(request);
        return newRegionModel(model);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteRegions)
    public String editRegion(Request request, Response response) {
        Model model;
        Region region;
        LocationService locationService;

        if ((region = getSelectedRegion(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        return editRegionModel(model, region);
    }

    @Action(path = "/Add", permission = AclPermission.WriteRegions, method = HttpMethod.post)
    public String addRegion(Request request, Response response) {
        Date date;
        Model model;
        Region region;
        LocationService locationService;

        if (!validatePostData(request, Region.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newRegionModel(model);
        }

        locationService = getService(LocationService.class);

        if (locationService.isRegionCodeInUse(requestAttribute("code", request))) {
            setSessionErrorMessage("Selected code is already in use", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newRegionModel(model);
        }

        date = new Date();
        region = new Region();
        region.setCode(requestAttribute("code", request));
        region.setName(requestAttribute("name", request));
        region.setActive(requestAttribute("active", request));
        region.setCreated(date);
        region.setModified(date);

        locationService.addRegion(region, AuditService.createLogEntry(request));

        setSessionSuccessMessage("Region successfully added!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", permission = AclPermission.WriteRegions, method = HttpMethod.post)
    public String updateRegion(Request request, Response response) {
        final Date date;
        final Model model;
        final Region region;
        final LocationService locationService;

        if (!validatePostData(request, Region.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newRegionModel(model);
        }

        locationService = getService(LocationService.class);

        if ((region = locationService.getRegionById(requestAttribute("id", request))) == null) {
            setSessionErrorMessage("Selected region does not exist", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (region.isHidden()) {
            setSessionErrorMessage("Selected region does not exist", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (region.isSystem()) {
            setSessionErrorMessage("Selected region cannot be edited", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (!region.getCode().equalsIgnoreCase(requestAttribute("code", request))) {
            if (locationService.isRegionCodeInUse(requestAttribute("code", request))) {
                setSessionErrorMessage("Selected code is already in use", request);
                model = createModel(request);
                copyRawPostDataToModel(model, request);
                model.put("booleans", Bool.VALUES);
                return renderView("location/region/edit.html", model);
            }
            region.setCode(requestAttribute("code", request));
        }

        region.setName(requestAttribute("name", request));
        region.setActive(requestAttribute("active", request));
        region.setModified(new Date());

        locationService.updateRegion(region, AuditService.createLogEntry(request));

        setSessionSuccessMessage("Region successfully updated!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }
}
