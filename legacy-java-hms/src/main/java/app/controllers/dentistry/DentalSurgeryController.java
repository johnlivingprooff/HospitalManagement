package app.controllers.dentistry;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.account.Account;
import app.models.dentistry.DentalSurgery;
import app.models.medical.Procedure;
import app.models.patient.PatientInfo;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import app.services.dentistry.DentalSurgeryService;
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

@RouteController(path = "/Hms/Dentistry/Surgeries")
public class DentalSurgeryController extends Controller {
    @Inject
    private DentalSurgeryService surgeryService;

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
            if ((surgery = surgeryService.getSurgeryById(id, performer)) != null) {
                return surgery;
            }
        }
        setSessionErrorMessage("Selected dental surgery does not exist.", request);
        return null;
    }

    private String renderViewWithBaseDirectory(String view, Model model) {
        return renderView("dentistry/surgeries/" + view, model);
    }

    private List<Procedure> getProcedures() {
        return procedureService.getProceduresByType(Procedure.ProcedureType.Dental);
    }

    @Action(path = "/", permission = AclPermission.ReadDentalSurgeries)
    public String getMyPerformedSurgeries(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("surgeries", surgeryService.getSurgeryByPerformer(getCurrentUser(request).getId()));
        return renderViewWithBaseDirectory("list.html", model);
    }

    @Action(path = "/:id/Details", permission = AclPermission.ReadDentalSurgeries)
    public String surgeryDetails(Request request, Response response) {
        Model model;
        DentalSurgery surgery;

        if ((surgery = getSelectedSurgery(request)) != null) {
            model = createModel(request);
            model.put("surgery", surgery);
            return renderViewWithBaseDirectory("details.html", model);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Attachment", permission = AclPermission.ReadDentalSurgeries)
    public Object getSurgery(Request request, Response response) {
        File file;
        DentalSurgery surgery;

        if ((surgery = getSelectedSurgery(request)) != null) {
            if (surgery.getAttachment() != null) {
                file = new File(getAttachmentDirectory(), surgery.getAttachment());
                if (file.exists()) {
                    return serveFile(response, file, format("%s_dental_surgery%s",
                            surgery.getPatientName(), LocaleUtil.getFileExtensionWithPeriod(file.getName())));
                }
            }
            setSessionErrorMessage("Attachment does not exist.", request);
            return temporaryRedirect(withBaseUrl(Long.toString(surgery.getId()), "Details"), response);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/New", permission = AclPermission.WriteDentalSurgeries)
    public String newSurgery(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("procedures", getProcedures());
        return renderViewWithBaseDirectory("new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteDentalSurgeries)
    public String addSurgery(Request request, Response response) {
        Model model;
        DentalSurgery surgery;
        Account account;
        String message;
        File attachment;
        Procedure procedure;
        boolean fileUploadError;
        PatientInfo patientInfo;
        ValidationResults results;

        message = null;
        fileUploadError = false;
        results = validate(DentalSurgery.class, Options.defaults().sticky(true), request);

        if (results.success()) {
            surgery = results.getBean();
            if ((patientInfo = patientService.findPatientInfoByMrn(surgery.getPatientMrn())) != null) {
                if (patientInfo.isEligibleForLabTest()) {
                    procedure = procedureService.getProcedureById(surgery.getProcedureId(), Procedure.ProcedureType.Dental);
                    if (procedure != null) {
                        if (handleUploadedFile("attachment", "attachment", getAttachmentDirectory(), false, request)) {
                            account = getCurrentUser(request);
                            surgery.setPerformedBy(account.getId());
                            surgery.setCreatedAt(LocalDateTime.now());
                            surgery.setPatientId(patientInfo.getId());

                            if ((attachment = getUploadedFile("attachment", request)) != null) {
                                surgery.setAttachment(attachment.getName());
                            }
                            surgeryService.addDentalSurgery(surgery);
                            auditService.log(format("%s uploaded surgery results %s.", account, surgery), request);
                            setSessionSuccessMessage("Results uploaded successfully.", request);
                            return temporaryRedirect(getBaseUrl(), response);
                        } else {
                            fileUploadError = true;
                        }
                    } else {
                        message = "Selected procedure does not exist.";
                    }
                } else {
                    message = patientInfo.getLabTestIneligibilityReason();
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
