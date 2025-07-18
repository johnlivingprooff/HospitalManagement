package app.controllers.patient;

import app.core.Controller;
import app.core.Country;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.ValidationStage;
import app.models.account.Account;
import app.models.account.Sex;
import app.models.patient.*;
import app.models.permission.AclPermission;
import app.services.UploadService;
import app.services.audit.AuditService;
import app.services.auth.AuthService;
import app.services.messaging.MessagingService;
import app.services.patient.PatientService;
import app.services.system.SystemService;
import app.types.Bool;
import app.util.LocaleUtil;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.*;
import java.nio.file.Files;
import java.util.Date;

@RouteController(path = "/Hms/Patients")
@SuppressWarnings({"WeakerAccess", "unused"})
public final class PatientController extends Controller {

    @Inject
    private PatientService patientService;

    @Inject
    private AuthService authService;

    @Inject
    private SystemService systemService;

    @Inject
    private UploadService uploadService;

    private Patient getSelectedPatient(Request request) {
        Long id;
        Patient patient;
        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((patient = patientService.getPatientByAccountId(id)) != null) {
                return patient;
            }
        }
        setSessionErrorMessage("Selected patient does not exist.", request);
        return null;
    }

    private String getPatients(Request request, boolean inpatient) {
        final Model model;
        final PatientService patientService;

        model = createModel(request);
        patientService = getService(PatientService.class);

        if (inpatient) {
            model.put("listTitle", "Inpatient List");
            model.put("patients", patientService.getPatientsByType(PatientType.Inpatient));
            return renderView("patient/list.html", model);
        } else {
            model.put("listTitle", "Outpatient List");
            model.put("patients", patientService.getPatientsByType(PatientType.Outpatient));
            return renderView("patient/list.html", model);
        }
    }

    private String redirectToPatientList(Response response, PatientType type) {
        String path;
        if (type == PatientType.Inpatient) {
            path = "Inpatient";
        } else {
            path = "Outpatient";
        }
        return temporaryRedirect(withBaseUrl(path), response);
    }

    @Action(path = "/Inpatient", permission = AclPermission.ViewInPatients)
    public String getInpatientPatients(Request request, Response response) {
        return getPatients(request, true);
    }

    @Action(path = "/Outpatient", permission = AclPermission.ViewOutPatients)
    public String getOutpatientPatients(Request request, Response response) {
        return getPatients(request, false);
    }

    @Action(path = "/New", permission = AclPermission.AddPatients)
    public String newPatient(Request request, Response response) {
        final Model model;
        model = createModel(request);
        return newPatientModel(model);
    }

    @Action(path = "/Add", permission = AclPermission.AddPatients, method = HttpMethod.post)
    public String addPatient(Request request, Response response) {
        final Model model;
        final Date date;
        final Country country;
        final Patient patient;
        final File pictureFile;
        final Account thisAccount;
        final String email, idNumber;

        if (!validatePostData(request, Patient.class, ValidationStage.Create)) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            copyErrorListToModel(model, request);
            return newPatientModel(model);
        }

        email = requestAttribute("email", request);
        idNumber = requestAttribute("idNumber", request);

        // Email can ben null
        if (email != null) {
            if (patientService.isEmailAddressInUse(email)) {
                setSessionErrorMessage("Email address already in use.", request);
                model = createModel(request);
                copyRawPostDataToModel(model, request);
                copyErrorListToModel(model, request);
                return newPatientModel(model);
            }
        }

        if (patientService.isIdNumberInUse(idNumber)) {
            setSessionErrorMessage("ID number already in use.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            copyErrorListToModel(model, request);
            return newPatientModel(model);
        }

        thisAccount = getCurrentUser(request);

        date = new Date();
        patient = new Patient();
        copyValidatedData(request, patient, ValidationStage.Create);

        patient.setCreated(date);
        patient.setModified(date);
        patient.setStatus(PatientStatus.Registered);
        patient.setCreatedBy(thisAccount.getId());
        patient.setPassword(authService.generatePassword());

        if (!handleUploadedFile("picture", "picture", systemService.getSystemConfiguration().ImageDirectory,
                false, request, IMAGE_TYPES)) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return newPatientModel(model);
        }

        if ((pictureFile = getUploadedFile("picture", request)) != null) {
            patient.setPicture(pictureFile.getName());
        }

        patientService.add(patient);

        getService(AuditService.class)
                .log(thisAccount.getAuditLogString() + " registered a new patient " + patient, request);

        // send email
        sendRegistrationEmail(patient);

        setSessionSuccessMessage("Patient successfully registered", request);
        return redirectToPatientList(response, patient.getType());
    }

    private String editPatientModel(Model model) {
        model.put("sexes", Sex.VALUES);
        model.put("types", PatientType.TYPES);
        model.put("booleans", Bool.VALUES);
        model.put("countries", Country.COUNTRIES);
        model.put("idTypes", IdType.ID_TYPES);
        model.put("bloodGroups", BloodGroup.GROUPS);
        return renderView("patient/edit.html", model);
    }

    @Action(path = "/Update", permission = AclPermission.WritePatients, method = HttpMethod.post)
    public String updatePatient(Request request, Response response) {
        final Date date;
        final Model model;
        final String email;
        final Country country;
        final Patient patient;
        final String idNumber;
        final File pictureFile;
        final Account thisAccount;

        if (!validatePostData(request, Patient.class, ValidationStage.Update)) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            copyErrorListToModel(model, request);
            return editPatientModel(model);
        }

        thisAccount = getCurrentUser(request);

        if ((patient = patientService.getPatientByAccountId(requestAttribute("id", request))) == null) {
            setSessionErrorMessage("Selected patient does not exist.", request);
            return redirectToPatientList(response, PatientType.Inpatient);
        }

        email = requestAttribute("email", request);
        idNumber = requestAttribute("idNumber", request);

        if (email != null) {
            if (!email.equalsIgnoreCase(patient.getEmail())) {
                if (patientService.isEmailAddressInUse(email)) {
                    setSessionErrorMessage("Email address is already in use.", request);
                    model = createModel(request);
                    copyRawPostDataToModel(model, request);
                    copyErrorListToModel(model, request);
                    return editPatientModel(model);
                }
            }
        }

        if (!patient.getIdNumber().equalsIgnoreCase(idNumber)) {
            if (patientService.isIdNumberInUse(idNumber)) {
                setSessionErrorMessage("ID number already in use.", request);
                model = createModel(request);
                copyRawPostDataToModel(model, request);
                copyErrorListToModel(model, request);
                return editPatientModel(model);
            }
        }

        date = new Date();
        copyValidatedData(request, patient, ValidationStage.Update);

        patient.setModified(date);

        if (!handleUploadedFile("picture", "picture", systemService.getSystemConfiguration().ImageDirectory,
                false, request, IMAGE_TYPES)) {
            model = createModel(request);
            copyRawPostDataToModel(model, request);
            return editPatientModel(model);
        }

        if ((pictureFile = getUploadedFile("picture", request)) != null) {
            deleteProfileImageFile(patient.getPicture());
            patient.setPicture(pictureFile.getName());
        }

        patientService.update(patient);

        getService(AuditService.class)
                .log(thisAccount.getAuditLogString() + " updated patient " + patient, request);

        setSessionSuccessMessage("Patient successfully updated.", request);
        return redirectToPatientList(response, patient.getType());
    }

    @Action(path = "/ProfileImage/:account-id", checkPermission = false)
    public Object getPatientProfileImage(Request request, Response response) {
        final File file;
        final Long userId;
        final Patient patient;

        userId = getNumericQueryParameter(request, "account-id", Long.class);
        if (userId == null) {
            return resourceNotFound(response);
        }

        patient = patientService.getPatientByAccountId(userId);

        if (patient == null) {
            return resourceNotFound(response);
        }

        try {
            if ((file = patientService.getUserProfileImage(userId)) == null) {
                // serve default (placeholder) file
                try (InputStream inputStream = getClass().getClassLoader()
                        .getResourceAsStream("public/assets/static/placeholder-face-big.png")) {
                    return serveFile(response, inputStream, "profile.png", "image/png");
                }
            } else {
                try (InputStream inputStream = new FileInputStream(file)) {
                    return serveFile(response, inputStream, file.getName(), Files.probeContentType(file.toPath()));
                }
            }
        } catch (FileNotFoundException fne) {
            getLogger().error("Profile image not found.", fne);
            return resourceNotFound(response);
        } catch (IOException e) {
            getLogger().error("Exception when serving file.", e);
            return serverError(response);
        }
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WritePatients)
    public String editPatient(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response, PatientType.Inpatient);
        }

        model = createModel(request);
        copyEditableFieldsToModel(patient, model);
        return editPatientModel(model);
    }

    private String newPatientModel(Model model) {
        model.put("sexes", Sex.VALUES);
        model.put("booleans", Bool.VALUES);
        model.put("countries", Country.COUNTRIES);
        model.put("types", PatientType.TYPES);
        model.put("idTypes", IdType.ID_TYPES);
        model.put("bloodGroups", BloodGroup.GROUPS);
        return renderView("patient/new.html", model);
    }

    @Action(path = "/:id/Vitals", permission = AclPermission.ViewPatientVitals)
    public String getPatientVitals(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response, PatientType.Inpatient);
        }

        model = createModel(request);
        model.put("returnUrl", makeReturnUrl(patient));
        model.put("patient", patient);
        model.put("vitals", patientService.getPatientVitals(patient));
        return renderView("patient/vitals/list.html", model);
    }

    @Action(path = "/:id/BillHistory", permission = AclPermission.ViewPatientBills)
    public String getPatientBills(Request request, Response response) {
        Model model;
        Patient patient;

        if ((patient = getSelectedPatient(request)) == null) {
            return redirectToPatientList(response, PatientType.Inpatient);
        }

        model = createModel(request);
        model.put("returnUrl", makeReturnUrl(patient));
        model.put("patient", patient);
        model.put("bills", patientService.getPatientBills(patient));
        return renderView("patient/bills/list.html", model);
    }

    @Action(path = "/DeathReports", permission = AclPermission.ReadDeathReports)
    public String getDeathReports(Request request, Response response) {
        Model model;

        model = createModel(request);
        model.put("reports", patientService.getDeathReports());
        return renderView("patient/death/global-report.html", model);
    }

    @Action(path = "/DeathReports/:id/Download", permission = AclPermission.ReadDeathReports)
    public Object downloadDeathReport(Request request, Response response) {
        File file;
        Long id;
        String message;
        Patient patient;
        DeathReport report;

        message = "Selected death report does not exist.";

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((report = patientService.getDeathReportById(id)) != null) {
                if (report.getAttachment() != null) {
                    file = new File(getAttachmentDirectory(), report.getAttachment());
                    if (file.exists()) {
                        return serveFile(response, file, format("%s_death-report%s",
                                report.getPatient(), LocaleUtil.getFileExtensionWithPeriod(file.getName()))
                        );
                    }
                }
                message = "Selected death report does not have an attachment.";
            }
        }
        setSessionErrorMessage(message, request);
        return temporaryRedirect(withBaseUrl("DeathReports"), response);
    }

    private String makeReturnUrl(Patient patient) {
        return withBaseUrl(patient.getType().name());
    }

    private void sendRegistrationEmail(Patient patient) {
        if (!LocaleUtil.isNullOrEmpty(patient.getEmail())) {
            getService(MessagingService.class).sendPatientAccountSetupInstructions(patient);
        }
    }


}
