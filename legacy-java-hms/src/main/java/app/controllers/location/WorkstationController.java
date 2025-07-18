package app.controllers.location;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.location.District;
import app.models.location.WorkStation;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import app.services.location.LocationService;
import app.types.Bool;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.util.Date;

@RouteController(path = "/Hms/Workstations")
@SuppressWarnings("unused")
public class WorkstationController extends Controller {

    @Action(path = "/", permission = AclPermission.ReadWorkstations)
    public String getWorkstations(Request request, Response response) {
        Model model;
        LocationService locationService;

        locationService = getService(LocationService.class);

        model = createModel(request);
        model.put("workstations", locationService.getWorkStations(false));
        return renderView("location/workstation/list.html", model);
    }

    private String newWorkstationModel(Model model) {
        model.put("booleans", Bool.VALUES);
        model.put("districts", getService(LocationService.class).getDistricts(true));
        return renderView("location/workstation/new.html", model);
    }

    private String editWorkstationModel(Model model, WorkStation workStation) {
        model.put("id", workStation.getId());
        model.put("code", workStation.getCode());
        model.put("name", workStation.getName());
        model.put("active", workStation.isActive());
        model.put("address", workStation.getAddress());
        model.put("booleans", Bool.VALUES);
        model.put("districts", getService(LocationService.class).getDistricts(true));
        model.put("districtId", workStation.getDistrictId());
        return renderView("location/workstation/edit.html", model);
    }

    private WorkStation getSelectedWorkstation(Request request) {
        Long id;
        WorkStation workStation;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((workStation = getService(LocationService.class).getWorkStationById(id)) != null) {
                if (!workStation.isHidden()) {
                    return workStation;
                }
            }
        }
        setSessionErrorMessage("Selected workstation does not exist", request);
        return null;
    }

    @Action(path = "/New", permission = AclPermission.WriteWorkstations)
    public String newWorkstation(Request request, Response response) {
        Model model;
        model = createModel(request);
        return newWorkstationModel(model);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteWorkstations)
    public String editWorkstation(Request request, Response response) {
        Model model;
        WorkStation workStation;
        LocationService locationService;

        if ((workStation = getSelectedWorkstation(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        return editWorkstationModel(model, workStation);
    }

    @Action(path = "/Add", permission = AclPermission.WriteWorkstations, method = HttpMethod.post)
    public String addWorkstation(Request request, Response response) {
        Date date;
        Model model;
        District district;
        WorkStation workStation;
        LocationService locationService;

        if (!validatePostData(request, WorkStation.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        }

        locationService = getService(LocationService.class);

        if ((district = locationService.getDistrictById(requestAttribute("districtId", request))) == null) {
            setSessionErrorMessage("Selected district does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        }

        if (district.isHidden()) {
            setSessionErrorMessage("Selected district does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        } else if (!district.isActive()) {
            setSessionErrorMessage("Cannot use inactive districts", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        }

        if (locationService.isWorkstationCodeInUse(requestAttribute("code", request))) {
            setSessionErrorMessage("Selected code is already in use", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        }

        date = new Date();
        workStation = new WorkStation();
        workStation.setCode(requestAttribute("code", request));
        workStation.setName(requestAttribute("name", request));
        workStation.setActive(requestAttribute("active", request));
        workStation.setDistrictId(district.getId());
        workStation.setAddress(requestAttribute("address", request));
        workStation.setCreated(date);
        workStation.setModified(date);

        locationService.addWorkStation(workStation, AuditService.createLogEntry(request));

        setSessionSuccessMessage("Workstation successfully added!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", permission = AclPermission.WriteWorkstations, method = HttpMethod.post)
    public String updateWorkstation(Request request, Response response) {
        final Date date;
        final Model model;
        final District district;
        final WorkStation workStation;
        final LocationService locationService;

        if (!validatePostData(request, WorkStation.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        }

        locationService = getService(LocationService.class);

        if ((workStation = locationService.getWorkStationById(requestAttribute("id", request))) == null) {
            setSessionErrorMessage("Selected workstation does not exist", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((district = locationService.getDistrictById(requestAttribute("districtId", request))) == null) {
            setSessionErrorMessage("Selected district does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        }

        if (district.isHidden()) {
            setSessionErrorMessage("Selected district does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        } else if (!district.isActive()) {
            setSessionErrorMessage("Cannot use inactive districts", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newWorkstationModel(model);
        }

        if (workStation.isHidden()) {
            setSessionErrorMessage("Selected workstation does not exist", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (workStation.isSystem()) {
            setSessionErrorMessage("Selected workstation cannot be edited", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (!workStation.getCode().equalsIgnoreCase(requestAttribute("code", request))) {
            if (locationService.isWorkstationCodeInUse(requestAttribute("code", request))) {
                setSessionErrorMessage("Selected code is already in use", request);
                model = createModel(request);
                copyRawPostDataToModel(model, request);
                model.put("booleans", Bool.VALUES);
                return renderView("location/workstation/edit.html", model);
            }
            workStation.setCode(requestAttribute("code", request));
        }

        workStation.setName(requestAttribute("name", request));
        workStation.setActive(requestAttribute("active", request));
        workStation.setDistrictId(district.getId());
        workStation.setAddress(requestAttribute("address", request));
        workStation.setModified(new Date());

        locationService.updateWorkStation(workStation, AuditService.createLogEntry(request));

        setSessionSuccessMessage("Workstation successfully updated!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }
}
