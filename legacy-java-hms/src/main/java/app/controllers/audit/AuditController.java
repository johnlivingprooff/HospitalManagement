package app.controllers.audit;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.core.validation.DataValidator;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.util.Map;

@RouteController(path = "/Hms/AuditLogs")
@SuppressWarnings({"WeakerAccess", "unused"})
public final class AuditController extends Controller {

    @Action(path = "/", permission = AclPermission.ReadAuditLogs)
    public String getAuditLogs(Request request, Response response) {
        final AuditService auditService = getService(AuditService.class);
        Map<String, Object> model = createModel(request);
        model.put("auditLogs", auditService.getFreshLogs());
        return renderView("audit/LogList.html", model);
    }

    @Action(path = "/ArchivedLogs", permission = AclPermission.ReadAuditLogs)
    public String getArchivedAuditLogs(Request request, Response response) {
        final AuditService auditService = getService(AuditService.class);
        Map<String, Object> model = createModel(request);
        model.put("auditLogs", auditService.getArchivedLogs());
        return renderView("audit/ArchivedLogList.html", model);
    }

    @DataValidator.Schema("ArchiveAuditLogs")
    @Action(path = "/Archive", permission = AclPermission.ArchiveAuditLogs, method = HttpMethod.post)
    public String archiveAuditLogs(Request request, Response response) {
        if (validatePostData(request, "ArchiveAuditLogs")) {
            final AuditService auditService = getService(AuditService.class);
            long[] logIds = request.attribute("log");

            auditService.archiveLogs(logIds);
            setSessionSuccessMessage("Logs have been archived", request);
        } else {
            setSessionErrorMessage("Select logs to archive first", request);
        }
        return temporaryRedirect("/Hms/AuditLogs", response);
    }
}
