package app.controllers.location;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.location.Department;
import app.models.location.WorkStation;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import app.services.location.LocationService;
import app.types.Bool;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.util.Date;

@RouteController(path = "/Hms/Departments")
@SuppressWarnings("unused")
public class DepartmentController extends Controller {

    @Action(path = "/", permission = AclPermission.ReadDepartments)
    public String getDepartments(Request request, Response response) {
        Model model;
        LocationService locationService;

        locationService = getService(LocationService.class);

        model = createModel(request);
        model.put("departments", locationService.getDepartments(false));
        return renderView("location/department/list.html", model);
    }

    private String newDepartmentModel(Model model) {
        model.put("booleans", Bool.VALUES);
        model.put("workstations", getService(LocationService.class).getWorkStations(true));
        return renderView("location/department/new.html", model);
    }

    private String editDepartmentModel(Model model, Department department) {
        model.put("id", department.getId());
        model.put("code", department.getCode());
        model.put("name", department.getName());
        model.put("active", department.isActive());
        model.put("booleans", Bool.VALUES);
        model.put("workstations", getService(LocationService.class).getWorkStations(true));
        model.put("workstationId", department.getWorkStationId());
        return renderView("location/department/edit.html", model);
    }

    private Department getSelectedDepartment(Request request) {
        Long id;
        Department department;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((department = getService(LocationService.class).getDepartmentById(id)) != null) {
                if (!department.isHidden()) {
                    return department;
                }
            }
        }
        setSessionErrorMessage("Selected department does not exist", request);
        return null;
    }

    @Action(path = "/New", permission = AclPermission.WriteDepartments)
    public String newDepartment(Request request, Response response) {
        Model model;
        model = createModel(request);
        return newDepartmentModel(model);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteDepartments)
    public String editDepartment(Request request, Response response) {
        Model model;
        Department department;
        LocationService locationService;

        if ((department = getSelectedDepartment(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        return editDepartmentModel(model, department);
    }

    @Action(path = "/Add", permission = AclPermission.WriteDepartments, method = HttpMethod.post)
    public String addDepartment(Request request, Response response) {
        Date date;
        Model model;
        WorkStation workStation;
        Department department;
        LocationService locationService;

        if (!validatePostData(request, Department.class, ValidationStage.Create)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        }

        locationService = getService(LocationService.class);

        if ((workStation = locationService.getWorkStationById(requestAttribute("workstationId", request))) == null) {
            setSessionErrorMessage("Selected workstation does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        }

        if (workStation.isHidden()) {
            setSessionErrorMessage("Selected workstation does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        } else if (!workStation.isActive()) {
            setSessionErrorMessage("Cannot use inactive workstations", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        }

        if (locationService.isDepartmentCodeInUse(requestAttribute("code", request))) {
            setSessionErrorMessage("Selected code is already in use", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        }

        date = new Date();
        department = new Department();
        department.setCode(requestAttribute("code", request));
        department.setName(requestAttribute("name", request));
        department.setActive(requestAttribute("active", request));
        department.setWorkStationId(workStation.getId());
        department.setCreated(date);
        department.setModified(date);

        locationService.addDepartment(department, AuditService.createLogEntry(request));

        setSessionSuccessMessage("Department successfully added!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", permission = AclPermission.WriteDepartments, method = HttpMethod.post)
    public String updateDepartment(Request request, Response response) {
        final Date date;
        final Model model;
        final Department department;
        final WorkStation workStation;
        final LocationService locationService;

        if (!validatePostData(request, Department.class, ValidationStage.Update)) {
            model = createModel(request);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        }

        locationService = getService(LocationService.class);

        if ((department = locationService.getDepartmentById(requestAttribute("id", request))) == null) {
            setSessionErrorMessage("Selected department does not exist", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((workStation = locationService.getWorkStationById(requestAttribute("workstationId", request))) == null) {
            setSessionErrorMessage("Selected workstation does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        }

        if (workStation.isHidden()) {
            setSessionErrorMessage("Selected workstation does not exist", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        } else if (!workStation.isActive()) {
            setSessionErrorMessage("Cannot use inactive workstations", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newDepartmentModel(model);
        }

        if (department.isHidden()) {
            setSessionErrorMessage("Selected department does not exist", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (department.isSystem()) {
            setSessionErrorMessage("Selected department cannot be edited", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (!department.getCode().equalsIgnoreCase(requestAttribute("code", request))) {
            if (locationService.isDepartmentCodeInUse(requestAttribute("code", request))) {
                setSessionErrorMessage("Selected code is already in use", request);
                model = createModel(request);
                copyRawPostDataToModel(model, request);
                model.put("booleans", Bool.VALUES);
                return renderView("location/department/edit.html", model);
            }
            department.setCode(requestAttribute("code", request));
        }

        department.setName(requestAttribute("name", request));
        department.setActive(requestAttribute("active", request));
        department.setWorkStationId(workStation.getId());
        department.setModified(new Date());

        locationService.updateDepartment(department, AuditService.createLogEntry(request));

        setSessionSuccessMessage("Department successfully updated!", request);

        return temporaryRedirect(getBaseUrl(), response);
    }
}
