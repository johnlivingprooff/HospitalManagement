package app.controllers.lab;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.Contexts;
import app.models.medical.Procedure;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import app.services.medical.ProcedureService;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Lab/Procedures")
public class LabProcedureController extends Controller {
    @Inject
    private ProcedureService procedureService;

    @Inject
    private AuditService auditService;

    private Procedure getSelectedProcedure(Request request) {
        Long id;
        Procedure procedure;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((procedure = procedureService.getProcedureById(id, Procedure.ProcedureType.Lab)) != null) {
                return procedure;
            }
        }
        setSessionErrorMessage("Selected procedure does not exist.", request);
        return null;
    }

    @Action(path = "/", permission = AclPermission.ReadLabProcedures)
    public String getLabProcedures(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("procedures", procedureService.getProceduresByType(Procedure.ProcedureType.Lab));
        return renderView("lab/procedures/list.html", model);
    }

    @Action(path = "/New", permission = AclPermission.WriteLabProcedures)
    public String newLabProcedure(Request request, Response response) {
        return renderView("lab/procedures/new.html", createModel(request));
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteLabProcedures)
    public String addLabProcedure(Request request, Response response) {
        Model model;
        Procedure procedure;
        ValidationResults results;

        results = validate(Procedure.class, Options.defaults().sticky(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("lab/procedures/new.html", model);
        }
        procedure = results.getBean();

        procedure.setCreatedAt(LocalDateTime.now());
        procedure.setUpdatedAt(procedure.getCreatedAt());
        procedure.setProcedureType(Procedure.ProcedureType.Lab);

        procedureService.addProcedure(procedure);

        auditService.log(format("%s added procedure %s.", getCurrentUser(request), procedure), request);
        setSessionSuccessMessage("Procedure added successfully.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteLabProcedures)
    public String updateLabProcedure(Request request, Response response) {
        Model model;
        Procedure procedure;
        ValidationResults results;

        results = validate(Procedure.class, Options.defaults().map(false).sticky(true).context(Contexts.UPDATE), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("lab/procedures/edit.html", model);
        }

        procedure = procedureService.getProcedureById((long) results.getResults().get("id"), Procedure.ProcedureType.Lab);
        if (procedure == null) {
            setSessionErrorMessage("This procedure does not exist.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        results.updateBean(procedure);
        procedure.setUpdatedAt(LocalDateTime.now());

        procedureService.updateProcedure(procedure);

        auditService.log(format("%s updated procedure %s.", getCurrentUser(request), procedure), request);
        setSessionSuccessMessage("Procedure updated successfully.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteLabProcedures)
    public String editLabProcedure(Request request, Response response) {
        Model model;
        Procedure procedure;

        if ((procedure = getSelectedProcedure(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        model.put("id", procedure.getId());
        model.put("cost", procedure.getCost());
        model.put("name", procedure.getName());
        return renderView("lab/procedures/edit.html", model);
    }

    @Action(path = "/:id/Delete", permission = AclPermission.WriteLabProcedures)
    public String deleteLabProcedure(Request request, Response response) {
        Model model;
        Procedure procedure;

        if ((procedure = getSelectedProcedure(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        procedure.setUpdatedAt(LocalDateTime.now());
        procedure.setDeleted(true);
        procedureService.updateProcedure(procedure);
        auditService.log(format("%s deleted procedure %s.", getCurrentUser(request), procedure), request);
        setSessionSuccessMessage("Procedure deleted successfully.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }
}
