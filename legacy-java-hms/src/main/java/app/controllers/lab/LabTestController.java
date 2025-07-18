package app.controllers.lab;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.account.Account;
import app.models.lab.LabTest;
import app.models.medical.Procedure;
import app.models.patient.PatientInfo;
import app.models.permission.AclPermission;
import app.services.audit.AuditService;
import app.services.lab.LabService;
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

@RouteController(path = "/Hms/Lab/Tests")
public class LabTestController extends Controller {

    @Inject
    private LabService labService;

    @Inject
    private AuditService auditService;

    @Inject
    private ProcedureService procedureService;

    @Inject
    private PatientService patientService;

    private LabTest getSelectedLabTest(Request request) {
        Long id;
        long examiner;
        LabTest labTest;

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            examiner = getCurrentUser(request).getId();
            if ((labTest = labService.getLabTestById(id, examiner)) != null) {
                return labTest;
            }
        }
        setSessionErrorMessage("Selected lab test does not exist.", request);
        return null;
    }

    private List<Procedure> getLabProcedures() {
        return procedureService.getProceduresByType(Procedure.ProcedureType.Lab);
    }

    @Action(path = "/", permission = AclPermission.ReadLabTests)
    public String getMyLabTests(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("tests", labService.getLabTestsByExaminer(getCurrentUser(request).getId()));
        return renderView("lab/tests/list.html", model);
    }

    @Action(path = "/:id/Details", permission = AclPermission.ReadLabTests)
    public String labTestDetails(Request request, Response response) {
        Model model;
        LabTest labTest;

        if ((labTest = getSelectedLabTest(request)) != null) {
            model = createModel(request);
            model.put("test", labTest);
            return renderView("lab/tests/details.html", model);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Attachment", permission = AclPermission.ReadLabTests)
    public Object getLabTestAttachment(Request request, Response response) {
        File file;
        LabTest labTest;

        if ((labTest = getSelectedLabTest(request)) != null) {
            if (labTest.getAttachment() != null) {
                file = new File(getAttachmentDirectory(), labTest.getAttachment());
                if (file.exists()) {
                    return serveFile(response, file, format("%s_lab_test%s",
                            labTest.getPatientName(), LocaleUtil.getFileExtensionWithPeriod(file.getName())));
                }
            }
            setSessionErrorMessage("Attachment does not exist.", request);
            return temporaryRedirect(withBaseUrl(Long.toString(labTest.getId()), "Details"), response);
        }
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/New", permission = AclPermission.WriteLabTests)
    public String newLabTest(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("procedures", getLabProcedures());
        return renderView("lab/tests/new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WriteLabTests)
    public String addLabTest(Request request, Response response) {
        Model model;
        LabTest test;
        Account account;
        String message;
        File attachment;
        Procedure procedure;
        boolean fileUploadError;
        PatientInfo patientInfo;
        ValidationResults results;

        message = null;
        fileUploadError = false;
        results = validate(LabTest.class, Options.defaults().sticky(true), request);

        if (results.success()) {
            test = results.getBean();
            if ((patientInfo = patientService.findPatientInfoByMrn(test.getPatientMrn())) != null) {
                if (patientInfo.isEligibleForLabTest()) {
                    procedure = procedureService.getProcedureById(test.getProcedureId(), Procedure.ProcedureType.Lab);
                    if (procedure != null) {
                        if (handleUploadedFile("attachment", "attachment", getAttachmentDirectory(), false, request)) {
                            account = getCurrentUser(request);
                            test.setExaminer(account.getId());
                            test.setCreatedAt(LocalDateTime.now());
                            test.setPatientId(patientInfo.getId());

                            if ((attachment = getUploadedFile("attachment", request)) != null) {
                                test.setAttachment(attachment.getName());
                            }
                            labService.addLabTest(test);
                            auditService.log(format("%s uploaded test results %s.", account, test), request);
                            setSessionSuccessMessage("Test results uploaded successfully.", request);
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
        model.put("procedures", getLabProcedures());
        return renderView("lab/tests/new.html", model);
    }
}
