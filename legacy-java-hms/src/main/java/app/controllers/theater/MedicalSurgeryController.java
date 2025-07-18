package app.controllers.theater;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.account.Account;
import app.models.medical.Procedure;
import app.models.patient.PatientInfo;
import app.models.permission.AclPermission;
import app.models.theater.MedicalSurgery;
import app.services.audit.AuditService;
import app.services.medical.ProcedureService;
import app.services.patient.PatientService;
import app.services.theater.TheaterService;
import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@RouteController(path = "/Hms/Theater/Surgeries")
public class MedicalSurgeryController extends Controller {
    @Inject
    private TheaterService theaterService;

    @Inject
    private AuditService auditService;

    @Inject
    private ProcedureService procedureService;

    @Inject
    private PatientService patientService;

    private MedicalSurgery getSelectedSurgery(Request request) {
        Long id;
        long performer;
        MedicalSurgery surgery;

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            performer = getCurrentUser(request).getId();
            if ((surgery = theaterService.getSurgeryById(id, performer)) != null) {
                return surgery;
            }
        }
        setSessionErrorMessage("Selected dental surgery does not exist.", request);
        return null;
    }

    private String renderViewWithBaseDirectory(String view, Model model) {
        return renderView("theater/surgeries/" + view, model);
    }

    private List<Procedure> getProcedures() {
        return procedureService.getProceduresByType(Procedure.ProcedureType.Surgery);
    }

    @Action(path = "/", permission = AclPermission.ReadTheaterSurgeries)
    public String getPerformedSurgeries(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("surgeries", theaterService.getSurgeryByPerformer(getCurrentUser(request).getId()));
        return renderViewWithBaseDirectory("list.html", model);
    }

    @Action(path = "/:id/Details", permission = AclPermission.ReadTheaterSurgeries)
    public String labSurgeryDetails(Request request, Response response) {
        Model model;
        MedicalSurgery surgery;

        if ((surgery = getSelectedSurgery(request)) != null) {
            model = createModel(request);
            model.put("surgery", surgery);
            return renderViewWithBaseDirectory("details.html", model);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Attachment", permission = AclPermission.ReadTheaterSurgeries)
    public Object getSurgeryAttachment(Request request, Response response) {
        File file;
        MedicalSurgery surgery;

        if ((surgery = getSelectedSurgery(request)) != null) {
            if (surgery.getAttachment() != null) {
                file = new File(getAttachmentDirectory(), surgery.getAttachment());
                if (file.exists()) {
                    return serveFile(response, file, format("%s_medical_surgery%s",
                            surgery.getPatientName(), LocaleUtil.getFileExtensionWithPeriod(file.getName())));
                }
            }
            setSessionErrorMessage("Attachment does not exist.", request);
            return temporaryRedirect(withBaseUrl(Long.toString(surgery.getId()), "Details"), response);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/New", permission = AclPermission.WriteTheaterSurgeries)
    public String newSurgery(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("procedures", getProcedures());
        return renderViewWithBaseDirectory("new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteTheaterSurgeries)
    public String addSurgery(Request request, Response response) {
        Model model;
        MedicalSurgery surgery;
        Account account;
        String message;
        File attachment;
        Procedure procedure;
        boolean fileUploadError;
        PatientInfo patientInfo;
        ValidationResults results;

        message = null;
        fileUploadError = false;
        results = validate(MedicalSurgery.class, Options.defaults().sticky(true), request);

        if (results.success()) {
            surgery = results.getBean();
            if ((patientInfo = patientService.findPatientInfoByMrn(surgery.getPatientMrn())) != null) {
                if (patientInfo.isEligibleForMedicalSurgery()) {
                    procedure = procedureService.getProcedureById(surgery.getProcedureId(), Procedure.ProcedureType.Surgery);
                    if (procedure != null) {
                        if (handleUploadedFile("attachment", "attachment", getAttachmentDirectory(), false, request)) {
                            account = getCurrentUser(request);
                            surgery.setPerformedBy(account.getId());
                            surgery.setCreatedAt(LocalDateTime.now());
                            surgery.setPatientId(patientInfo.getId());

                            if ((attachment = getUploadedFile("attachment", request)) != null) {
                                surgery.setAttachment(attachment.getName());
                            }
                            theaterService.addMedicalSurgery(surgery);
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
                    message = patientInfo.getMedicalSurgeryIneligibilityReason();
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
