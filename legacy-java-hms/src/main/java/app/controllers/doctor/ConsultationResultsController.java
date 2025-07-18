package app.controllers.doctor;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.account.Account;
import app.models.dentistry.DentalSurgery;
import app.models.doctor.ConsultationResult;
import app.models.medical.Procedure;
import app.models.patient.PatientInfo;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import app.services.doctor.ConsultationService;
import app.services.medical.ProcedureService;
import app.services.patient.PatientService;
import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@RouteController(path = "/Hms/Consultations/Results")
public class ConsultationResultsController extends Controller {
    @Inject
    private ConsultationService resultService;

    @Inject
    private AuditService auditService;

    @Inject
    private ProcedureService procedureService;

    @Inject
    private PatientService patientService;

    private DentalSurgery getSelectedSurgery(Request request) {
        Long id;
        long performer;
        DentalSurgery surgery;

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            performer = getCurrentUser(request).getId();
            if ((surgery = resultService.getConsultationResultById(id, performer)) != null) {
                return surgery;
            }
        }
        setSessionErrorMessage("Selected consultation result does not exist.", request);
        return null;
    }

    private String renderViewWithBaseDirectory(String view, Model model) {
        return renderView("consultation/results/" + view, model);
    }

    private List<Procedure> getProcedures() {
        return procedureService.getProceduresByType(Procedure.ProcedureType.Consultation);
    }

    @Action(path = "/", permission = AclPermission.ReadConsultationResults)
    public String getMy(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("results", resultService.getResultsByConsultant(getCurrentUser(request).getId()));
        return renderViewWithBaseDirectory("list.html", model);
    }

    @Action(path = "/:id/Details", permission = AclPermission.ReadConsultationResults)
    public String resultDetails(Request request, Response response) {
        Model model;
        DentalSurgery surgery;

        if ((surgery = getSelectedSurgery(request)) != null) {
            model = createModel(request);
            model.put("result", surgery);
            return renderViewWithBaseDirectory("details.html", model);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Attachment", permission = AclPermission.ReadConsultationResults)
    public Object getResultAttachment(Request request, Response response) {
        File file;
        DentalSurgery surgery;

        if ((surgery = getSelectedSurgery(request)) != null) {
            if (surgery.getAttachment() != null) {
                file = new File(getAttachmentDirectory(), surgery.getAttachment());
                if (file.exists()) {
                    return serveFile(response, file, format("%s_consultation_results%s",
                            surgery.getPatientName(), LocaleUtil.getFileExtensionWithPeriod(file.getName())));
                }
            }
            setSessionErrorMessage("Attachment does not exist.", request);
            return temporaryRedirect(withBaseUrl(Long.toString(surgery.getId()), "Details"), response);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/New", permission = AclPermission.WriteConsultationResults)
    public String newResult(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("procedures", getProcedures());
        return renderViewWithBaseDirectory("new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteConsultationResults)
    public String addResult(Request request, Response response) {
        Model model;
        Account account;
        String message;
        File attachment;
        Procedure procedure;
        boolean fileUploadError;
        PatientInfo patientInfo;
        ConsultationResult result;
        ValidationResults results;

        message = null;
        fileUploadError = false;
        results = validate(ConsultationResult.class, Options.defaults().sticky(true), request);

        if (results.success()) {
            result = results.getBean();
            if ((patientInfo = patientService.findPatientInfoByMrn(result.getPatientMrn())) != null) {
                if (patientInfo.isEligibleForConsultation()) {
                    procedure = procedureService.getProcedureById(result.getProcedureId(), Procedure.ProcedureType.Consultation);
                    if (procedure != null) {
                        if (handleUploadedFile("attachment", "attachment", getAttachmentDirectory(), false, request)) {
                            account = getCurrentUser(request);
                            result.setPerformedBy(account.getId());
                            result.setCreatedAt(LocalDateTime.now());
                            result.setPatientId(patientInfo.getId());

                            if ((attachment = getUploadedFile("attachment", request)) != null) {
                                result.setAttachment(attachment.getName());
                            }
                            resultService.addDConsultationResult(result);
                            auditService.log(format("%s uploaded consultation results %s.", account, result), request);
                            setSessionSuccessMessage("Results uploaded successfully.", request);
                            return temporaryRedirect(getBaseUrl(), response);
                        } else {
                            fileUploadError = true;
                        }
                    } else {
                        message = "Selected consultation does not exist.";
                    }
                } else {
                    message = patientInfo.getConsultationIneligibilityReason();
                }
            } else {
                message = "Selected patient does not exist.";
            }
            if (message != null) {
                setSessionErrorMessage(message, request);
            }
        }
        model = createModel(request);
        if (message == null) {
            if (!fileUploadError) {
                copyErrorListToModel(model, results);
            }
        }
        copyRawPostDataToModel(model, results);
        model.put("procedures", getProcedures());
        return renderViewWithBaseDirectory("new.html", model);
    }
}
