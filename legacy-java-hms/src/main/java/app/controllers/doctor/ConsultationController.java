package app.controllers.doctor;

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

@RouteController(path = "/Hms/Consultations")
public class ConsultationController extends Controller {
    @Inject
    private ProcedureService procedureService;

    @Inject
    private AuditService auditService;

    private Procedure.ProcedureType getProcedureType() {
        return Procedure.ProcedureType.Consultation;
    }

    private String renderViewWithBaseDirectory(String view, Model model) {
        return renderView("consultation/" + view, model);
    }

    private Procedure getSelectedProcedure(Request request) {
        Long id;
        Procedure procedure;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((procedure = procedureService.getProcedureById(id, getProcedureType())) != null) {
                return procedure;
            }
        }
        setSessionErrorMessage("Selected consultation does not exist.", request);
        return null;
    }

    @Action(path = "/", permission = AclPermission.ReadConsultations)
    public String getProcedures(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("procedures", procedureService.getProceduresByType(getProcedureType()));
        return renderViewWithBaseDirectory("procedures/list.html", model);
    }

    @Action(path = "/New", permission = AclPermission.WriteConsultations)
    public String newProcedure(Request request, Response response) {
        return renderViewWithBaseDirectory("procedures/new.html", createModel(request));
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteConsultations)
    public String addProcedure(Request request, Response response) {
        Model model;
        Procedure procedure;
        ValidationResults results;

        results = validate(Procedure.class, Options.defaults().sticky(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderViewWithBaseDirectory("procedures/new.html", model);
        }
        procedure = results.getBean();

        procedure.setCreatedAt(LocalDateTime.now());
        procedure.setUpdatedAt(procedure.getCreatedAt());
        procedure.setProcedureType(getProcedureType());

        procedureService.addProcedure(procedure);

        auditService.log(format("%s added consultation %s.", getCurrentUser(request), procedure), request);
        setSessionSuccessMessage("Consultation added successfully.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteConsultations)
    public String updateProcedure(Request request, Response response) {
        Model model;
        Procedure procedure;
        ValidationResults results;

        results = validate(Procedure.class, Options.defaults().map(false).sticky(true).context(Contexts.UPDATE), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderViewWithBaseDirectory("procedures/edit.html", model);
        }

        procedure = procedureService.getProcedureById((long) results.getResults().get("id"), getProcedureType());
        if (procedure == null) {
            setSessionErrorMessage("This procedure does not exist.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        results.updateBean(procedure);
        procedure.setUpdatedAt(LocalDateTime.now());

        procedureService.updateProcedure(procedure);

        auditService.log(format("%s updated consultation %s.", getCurrentUser(request), procedure), request);
        setSessionSuccessMessage("Consultation updated successfully.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WriteConsultations)
    public String editProcedure(Request request, Response response) {
        Model model;
        Procedure procedure;

        if ((procedure = getSelectedProcedure(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        model.put("id", procedure.getId());
        model.put("cost", procedure.getCost());
        model.put("name", procedure.getName());
        return renderViewWithBaseDirectory("procedures/edit.html", model);
    }

    @Action(path = "/:id/Delete", permission = AclPermission.WriteConsultations)
    public String deleteProcedure(Request request, Response response) {
        Procedure procedure;

        if ((procedure = getSelectedProcedure(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        procedure.setUpdatedAt(LocalDateTime.now());
        procedure.setDeleted(true);
        procedureService.updateProcedure(procedure);
        auditService.log(format("%s deleted consultation %s.", getCurrentUser(request), procedure), request);
        setSessionSuccessMessage("Consultation deleted successfully.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }
}
