package app.controllers.location;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.location.District;
import app.models.location.Region;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import app.services.location.LocationService;
import app.types.Bool;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.util.Date;

@RouteController(path = "/Hms/Districts")
@SuppressWarnings("unused")
public class DistrictController extends Controller {

    @Action(path = "/", permission = AclPermission.ReadDistricts)
    public String getDistricts(Request request, Response response) {
        Model model;
        LocationService locationService;

        locationService = getService(LocationService.class);

        model = createModel(request);
        model.put("districts", locationService.getDistricts(false));
        return renderView("location/district/list.html", model);
    }

    private String newDistrictModel(Model model) {
        model.put("booleans", Bool.VALUES);
        model.put("regions", getService(LocationService.class).getRegions(true));
        return renderView("location/district/new.html", model);
    }

    private String editDistrictModel(Model model, District district) {
        model.put("id", district.getId());
        model.put("code", district.getCode());
        model.put("name", district.getName());
        model.put("active", district.isActive());
        model.put("regions", getService(LocationService.class).getRegions(true));
        model.put("booleans", Bool.VALUES);
        model.put("regionId", district.getRegionId());
        return renderView("location/district/edit.html", model);
    }

    private District getSelectedDistrict(Request request) {
        Long id;
        District district;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((district = getService(LocationService.class).getDistrictById(id)) != null) {
                if (!district.isHidden()) {
                    return district;
                }
            }
        }
        setSessionErrorMessage("Selected district does not exist", request);
        return null;
    }

    @Action(path = "/New", permission = AclPermission.WriteDistricts)
    public String newDistrict(Request request, Response response) {
        Model model;
        model = createModel(request);
        return newDistrictModel(model);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteDistricts)
    public String editDistrict(Request request, Response response) {
        Model model;
        District district;
        LocationService locationService;

        if ((district = getSelectedDistrict(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        return editDistrictModel(model, district);
    }

    @Action(path = "/Add", permission = AclPermission.WriteDistricts, method = HttpMethod.post)
    public String addDistrict(Request request, Response response) {
        Date date;
        Model model;
        Region region;
        District district;
        LocationService locationService;

        if (!validatePostData(request, District.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        }

        locationService = getService(LocationService.class);

        if ((region = locationService.getRegionById(requestAttribute("regionId", request))) == null) {
            setSessionErrorMessage("Selected region does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        }

        if (region.isHidden()) {
            setSessionErrorMessage("Selected region does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        } else if (!region.isActive()) {
            setSessionErrorMessage("Cannot use inactive regions", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        }

        if (locationService.isDistrictCodeInUse(requestAttribute("code", request))) {
            setSessionErrorMessage("Selected code is already in use", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        }

        date = new Date();
        district = new District();
        district.setCode(requestAttribute("code", request));
        district.setName(requestAttribute("name", request));
        district.setActive(requestAttribute("active", request));
        district.setRegionId(region.getId());
        district.setCreated(date);
        district.setModified(date);

        locationService.addDistrict(district, AuditService.createLogEntry(request));

        setSessionSuccessMessage("District successfully added!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", permission = AclPermission.WriteDistricts, method = HttpMethod.post)
    public String updateDistrict(Request request, Response response) {
        final Date date;
        final Model model;
        final Region region;
        final District district;
        final LocationService locationService;

        if (!validatePostData(request, District.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        }

        locationService = getService(LocationService.class);

        if ((district = locationService.getDistrictById(requestAttribute("id", request))) == null) {
            setSessionErrorMessage("Selected district does not exist", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((region = locationService.getRegionById(requestAttribute("regionId", request))) == null) {
            setSessionErrorMessage("Selected region does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        }

        if (region.isHidden()) {
            setSessionErrorMessage("Selected region does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        } else if (!region.isActive()) {
            setSessionErrorMessage("Cannot use inactive regions", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDistrictModel(model);
        }

        if (district.isHidden()) {
            setSessionErrorMessage("Selected district does not exist", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (district.isSystem()) {
            setSessionErrorMessage("Selected district cannot be edited", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (!district.getCode().equalsIgnoreCase(requestAttribute("code", request))) {
            if (locationService.isDistrictCodeInUse(requestAttribute("code", request))) {
                setSessionErrorMessage("Selected code is already in use", request);
                model = createModel(request);
                copyRawPostDataToModel(model, request);
                model.put("booleans", Bool.VALUES);
                return renderView("location/district/edit.html", model);
            }
            district.setCode(requestAttribute("code", request));
        }

        district.setName(requestAttribute("name", request));
        district.setActive(requestAttribute("active", request));
        district.setRegionId(requestAttribute("regionId", request));
        district.setModified(new Date());

        locationService.updateDistrict(district, AuditService.createLogEntry(request));

        setSessionSuccessMessage("District successfully updated!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }
}
