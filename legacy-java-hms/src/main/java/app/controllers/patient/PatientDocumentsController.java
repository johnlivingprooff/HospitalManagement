package app.controllers.patient;

import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.patient.Patient;
import app.models.patient.PatientDocument;
import app.models.permission.AclPermission;
import app.services.UploadService;
import app.types.Bool;
import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@RouteController(path = "/Hms/Patients/:id/Documents")
public class PatientDocumentsController extends PatientInfoBaseController {

    private static final List<String> types = new LinkedList<>() {{
        add("application/pdf");
        add("image/jpeg");
        add("image/png");
        add("image/bmp");
        add("image/gif");
        add("text/csv");
    }};

    @Inject
    private UploadService uploadService;

    private PatientDocument getSelectedDocument(Request request, Patient patient) {
        Long id;
        PatientDocument document;

        if ((id = getNumericQueryParameter(request, "doc-id", Long.class)) != null) {
            if ((document = patientService.getPatientDocument(id, patient.getId())) != null) {
                return document;
            }
        }

        setSessionErrorMessage("Selected document does not exist.", request);
        return null;
    }

    @Action(path = "/", permission = AclPermission.ViewPatientAttachments)
    public String getPatientDocuments(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("type", patient.getType());
        model.put("documents", patientService.getPatientDocuments(patient));
        return renderView("patient/documents/list.html", model);
    }

    @Action(path = "/New", permission = AclPermission.AddPatientAttachments)
    public String newPatientDocument(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        model = createModel(request);
        model.put("patient", patient);
        model.put("type", patient.getType());
        model.put("booleans", Bool.VALUES);
        return renderView("patient/documents/new.html", model);
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.AddPatientAttachments)
    public String addPatientDocument(Request request, Response response) {
        Model model;
        Patient patient;
        PatientDocument document;
        ValidationResults results;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        results = validate(PatientDocument.class, Options.defaults().sticky(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            model.put("patient", patient);
            model.put("type", patient.getType());
            model.put("booleans", Bool.VALUES);
            return renderView("patient/documents/new.html", model);
        }

        if (!handleUploadedFile("attachment", "Document", getAttachmentDirectory(), true, request, types)) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            model.put("patient", patient);
            model.put("type", patient.getType());
            model.put("booleans", Bool.VALUES);
            return renderView("patient/documents/new.html", model);
        }

        document = results.getBean();
        document.setCreated(LocalDateTime.now());
        document.setPatientId(patient.getId());
        document.setAttachment(getUploadedFile("attachment", request).getName());

        patientService.addPatientDocument(document);

        setSessionSuccessMessage("Document uploaded successfully!", request);
        return redirectToPatientSubSection(response, patient, "Documents");
    }

    @Action(path = "/:doc-id/Update", method = HttpMethod.post, permission = AclPermission.EditPatientAttachments)
    public String updatePatientDocument(Request request, Response response) {
        File file;
        Model model;
        Patient patient;
        PatientDocument document;
        ValidationResults results;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if ((document = getSelectedDocument(request, patient)) != null) {
            results = validate(PatientDocument.class, Options.defaults().
                    instance(document).map(true).sticky(true), request);

            if (!results.success()) {
                model = createModel(request);
                copyErrorListToModel(model, results);
                copyRawPostDataToModel(model, results);
                model.put("patient", patient);
                model.put("type", patient.getType());
                model.put("booleans", Bool.VALUES);
                return renderView("patient/documents/edit.html", model);
            }

            if (!handleUploadedFile("attachment", "Document", getAttachmentDirectory(), false, request, types)) {
                model = createModel(request);
                copyErrorListToModel(model, results);
                copyRawPostDataToModel(model, results);
                model.put("patient", patient);
                model.put("type", patient.getType());
                model.put("booleans", Bool.VALUES);
                return renderView("patient/documents/edit.html", model);
            }

            if (((file = getUploadedFile("attachment", request))) != null) {
                deleteAttachmentFile(document.getName());
                document.setAttachment(file.getName());
            }
            patientService.updatePatientDocument(document);
            setSessionSuccessMessage("Document updated successfully!", request);
        } else {
            setSessionErrorMessage("Document not found", request);
        }

        return redirectToPatientSubSection(response, patient, "Documents");
    }

    @Action(path = "/:doc-id/Edit", permission = AclPermission.EditPatientAttachments)
    public String editDocument(Request request, Response response) {
        Model model;
        Patient patient;
        PatientDocument document;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if ((document = getSelectedDocument(request, patient)) != null) {
            model = createModel(request);
            model.put("type", patient.getType());
            model.put("patient", patient);
            model.put("booleans", Bool.VALUES);
            copyEditableFieldsToModel(document, model);
            return renderView("patient/documents/edit.html", model);
        }

        setSessionErrorMessage("Document not found.", request);
        return redirectToPatientSubSection(response, patient, "Documents");
    }

    @Action(path = "/:doc-id/Delete", permission = AclPermission.EditPatientAttachments)
    public String deleteDocument(Request request, Response response) {
        Model model;
        Patient patient;
        PatientDocument document;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if ((document = getSelectedDocument(request, patient)) != null) {
            patientService.deletePatientDocument(document);
            setSessionSuccessMessage("Document deleted", request);
        } else {
            setSessionErrorMessage("Document not found.", request);
        }
        return redirectToPatientSubSection(response, patient, "Documents");
    }

    @Action(path = "/:doc-id/Download", permission = AclPermission.ViewPatientAttachments)
    public Object downloadAttachment(Request request, Response response) {
        File file;
        Patient patient;
        PatientDocument document;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response);
        }

        if ((document = getSelectedDocument(request, patient)) != null) {
            if (document.getAttachment() != null) {
                file = new File(getAttachmentDirectory(), document.getAttachment());
                if (file.exists()) {
                    return serveFile(response, file, format("%s_%s%s",
                            patient.fullname(),
                            document.getName(), LocaleUtil.getFileExtensionWithPeriod(file.getName())));
                }
            }
            setSessionErrorMessage("Attachment does not exist.", request);
            return redirectToPatientList(response, patient.getType());
        }
        return redirectToPatientSubSection(response, patient, "Documents");
    }
}
