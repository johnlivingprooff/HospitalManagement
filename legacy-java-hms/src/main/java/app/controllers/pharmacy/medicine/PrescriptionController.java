package app.controllers.pharmacy.medicine;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.Contexts;
import app.models.account.Account;
import app.models.patient.PatientInfo;
import app.models.permission.AclPermission;
import app.models.pharmacy.medicine.Medicine;
import app.models.pharmacy.medicine.Prescription;
import app.models.pharmacy.medicine.PrescriptionDrug;
import app.services.audit.AuditService;
import app.services.patient.PatientService;
import app.services.pharmacy.PharmacyService;
import app.services.templating.TemplateService;
import app.util.LocaleUtil;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Prescriptions")
public class PrescriptionController extends Controller {

    @Inject
    private PharmacyService pharmacyService;

    @Inject
    private PatientService patientService;

    @Inject
    private AuditService auditService;

    @Inject
    private TemplateService templateService;

    private String getMyPrescriptions(Request request, Prescription.Status status) {
        Model model;
        long myAccountId;

        model = createModel(request);
        myAccountId = getCurrentUser(request).getId();
        model.put("status", status);
        model.put("listTitle", format("%s Prescriptions", status));
        model.put("prescriptions", pharmacyService.getPrescriptionsByFiler(myAccountId, status));
        return renderView("pharmacy/medicine/prescriptions/list.html", model);
    }

    @Action(path = "/", permission = AclPermission.ReadPrescriptions)
    public String getMyFiledPrescriptions(Request request, Response response) {
        return getMyPrescriptions(request, Prescription.Status.Filed);
    }

    @Action(path = "/Pending", permission = AclPermission.ReadPrescriptions)
    public String getMyPendingPrescriptions(Request request, Response response) {
        return getMyPrescriptions(request, Prescription.Status.Pending);
    }

    @Action(path = "/Dispensed", permission = AclPermission.ReadPrescriptions)
    public String getMyDispensedPrescriptions(Request request, Response response) {
        return getMyPrescriptions(request, Prescription.Status.Dispensed);
    }

    @Action(path = "/New", permission = AclPermission.WritePrescriptions)
    public String newPrescription(Request request, Response response) {
        return renderView("pharmacy/medicine/prescriptions/new.html", createModel(request));
    }

    @Action(path = "/Add", method = HttpMethod.post, permission = AclPermission.WritePrescriptions)
    public String addPrescription(Request request, Response response) {
        Model model;
        Account account;
        PatientInfo patientInfo;
        Prescription prescription;
        ValidationResults results;
        Prescription existingPrescription;

        account = getCurrentUser(request);
        results = validate(Prescription.class, Options.defaults().sticky(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("pharmacy/medicine/prescriptions/new.html", model);
        }

        prescription = results.getBean();

        if ((patientInfo = patientService.findPatientInfoByMrn(prescription.getPatientMrn())) == null) {
            setSessionErrorMessage("Selected patient does not exit.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return renderView("pharmacy/medicine/prescriptions/new.html", model);
        }

        if (!patientInfo.isEligibleForPrescriptions()) {
            setSessionErrorMessage(patientInfo.getPrescriptionIneligibilityReason(), request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return renderView("pharmacy/medicine/prescriptions/new.html", model);
        }

        if ((existingPrescription = pharmacyService.getUnDispensedPrescriptionForPatient(patientInfo.getId())) != null) {
            if (existingPrescription.getFilerId() == account.getId()) {
                setSessionErrorMessage(format("We found a %s prescription for this patient that you filed on " +
                                "<strong>%s</strong>. You can only file one prescription at a time per patient.",
                        existingPrescription.getStatus().toString().toLowerCase(),
                        LocaleUtil.formatDate(existingPrescription.getCreatedAt())), request);
            } else {
                setSessionErrorMessage(format("We found a %s prescription for this patient filed on " +
                                "<strong>%s</strong> by <strong>%s</strong> from the <strong>%s</strong> " +
                                "department. You can wait until the prescription is dispensed or ask them to delete it.",
                        existingPrescription.getStatus().toString().toLowerCase(),
                        LocaleUtil.formatDate(existingPrescription.getCreatedAt()),
                        templateService.escapeString(existingPrescription.getFiledBy()),
                        templateService.escapeString(existingPrescription.getDepartment())), request);
            }
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return renderView("pharmacy/medicine/prescriptions/new.html", model);
        }

        prescription.setDrugs(0);
        prescription.setDeleted(false);
        prescription.setFilerId(account.getId());
        prescription.setUpdaterId(account.getId());
        prescription.setCreatedAt(LocalDateTime.now());
        prescription.setPatientId(patientInfo.getId());
        prescription.setStatus(Prescription.Status.Filed);
        prescription.setUpdatedAt(prescription.getCreatedAt());

        pharmacyService.addPrescription(prescription);

        auditService.log(format("%s created prescription %s.", account, prescription), request);
        setSessionSuccessMessage("New prescription created. Click the <strong>Toggle Medicine Panel</strong> " +
                "link at the bottom to start adding medicine to the prescription.", request);
        return redirectToPrescription(prescription, response);
    }

    @Action(path = "/Submit", method = HttpMethod.post, permission = AclPermission.WritePrescriptions)
    public String submitPrescriptionToDispensary(Request request, Response response) {
        Account account;
        Prescription prescription;

        if ((prescription = getSelectedPrescription(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (prescription.getStatus() != Prescription.Status.Filed) {
            setSessionErrorMessage("Cannot submit this prescription at the moment.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (prescription.getDrugs() == 0) {
            setSessionErrorMessage("Prescription list needs to have at least one medicine.", request);
            return redirectToPrescription(prescription, response);
        }

        account = getCurrentUser(request);
        prescription.setUpdatedAt(LocalDateTime.now());
        prescription.setStatus(Prescription.Status.Pending);

        pharmacyService.updatePrescription(prescription);

        auditService.log(format("%s submitted prescription %s to dispensary.", account, prescription), request);
        setSessionSuccessMessage("Prescription submitted to dispensary.", request);
        return temporaryRedirect(withBaseUrl("Pending"), response);
    }

    private String redirectToPrescription(Prescription prescription, Response response) {
        return temporaryRedirect(withBaseUrl(Long.toString(prescription.getId()), "Edit"), response);
    }

    private Prescription getSelectedPrescription(Request request) {
        long filerId;
        long prescriptionId;
        ValidationResults results;
        Prescription prescription;

        results = validate(Prescription.class, Options.defaults().map(false).context(Contexts.FIND), request);
        if (!results.success()) {
            setSessionErrorMessage(results.getViolations().get(0), request);
            return null;
        }
        filerId = getCurrentUser(request).getId();
        prescriptionId = (long) results.getResults().get("id");
        if ((prescription = pharmacyService.getPrescriptionById(prescriptionId, filerId)) != null) {
            return prescription;
        }
        setSessionErrorMessage("Selected prescription does not exist.", request);
        return null;
    }

    private Prescription getSelectedFiledPrescription(Request request) {
        Long id;
        long filerId;
        Prescription prescription;

        filerId = getCurrentUser(request).getId();

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((prescription = pharmacyService.getPrescriptionById(id, filerId, Prescription.Status.Filed)) != null) {
                return prescription;
            }
        }
        setSessionErrorMessage("Selected prescription does not exist.", request);
        return null;
    }


    @Action(path = "/:id/Delete", permission = AclPermission.WritePrescriptions)
    public String deletePrescription(Request request, Response response) {
        Prescription prescription;

        if ((prescription = getSelectedPrescription(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (prescription.getStatus() != Prescription.Status.Filed) {
            setSessionErrorMessage("This prescription cannot be deleted at the moment.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        prescription.setDeleted(true);
        prescription.setUpdatedAt(LocalDateTime.now());
        pharmacyService.updatePrescription(prescription);
        auditService.log(format("%s deleted prescription %s.", getCurrentUser(request), prescription), request);

        setSessionSuccessMessage("Prescription deleted.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Edit", permission = AclPermission.WritePrescriptions)
    public String editPrescription(Request request, Response response) {
        Prescription prescription;

        if ((prescription = getSelectedPrescription(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (prescription.getStatus() != Prescription.Status.Filed) {
            setSessionErrorMessage("This prescription cannot be edited at the moment.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        return renderEditView(prescription, null, request);
    }

    @Action(path = "/:id/Recall", permission = AclPermission.WritePrescriptions)
    public String recallPrescription(Request request, Response response) {
        Prescription prescription;

        if ((prescription = getSelectedPrescription(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (prescription.getStatus() != Prescription.Status.Pending) {
            setSessionErrorMessage("This prescription cannot be recalled at the moment.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        prescription.setUpdatedAt(LocalDateTime.now());
        prescription.setStatus(Prescription.Status.Filed);

        pharmacyService.updatePrescription(prescription);
        auditService.log(format("%s recalled prescription %s.", getCurrentUser(request), prescription), request);

        setSessionSuccessMessage("Prescription recalled.", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @Action(path = "/:id/Details", permission = AclPermission.ReadPrescriptions)
    public String getPrescriptionDetails(Request request, Response response) {
        Model model;
        Prescription prescription;

        if ((prescription = getSelectedPrescription(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        model.put("drugs", pharmacyService.getMedicinesOnPrescription(prescription));
        model.put("prescription", prescription);
        switch (prescription.getStatus()) {
            case Dispensed:
                model.put("returnUrl", withBaseUrl("Dispensed"));
                break;
            case Pending:
                model.put("returnUrl", withBaseUrl("Pending"));
                break;
            case Filed:
                model.put("returnUrl", getBaseUrl());
                break;
        }
        return renderView("pharmacy/medicine/prescriptions/details.html", model);
    }

    private String renderEditView(Prescription prescription, Model model, Request request) {
        if (model == null) {
            model = createModel(request);
        }
        model.put("drugs", pharmacyService.getMedicinesOnPrescription(prescription));
        model.put("medicines", pharmacyService.getMedicineForPrescription(prescription.getId()));
        model.put("prescription", prescription);
        return renderView("pharmacy/medicine/prescriptions/edit.html", model);
    }

    @Action(path = "/:id/AddMedicine", method = HttpMethod.post, permission = AclPermission.WritePrescriptions)
    public String addDrugToPrescription(Request request, Response response) {
        Model model;
        Medicine medicine;
        ValidationResults results;
        Prescription prescription;
        PrescriptionDrug prescriptionDrug;

        // Can only add to filed prescriptions
        if ((prescription = getSelectedFiledPrescription(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        results = validate(PrescriptionDrug.class, Options.defaults().sticky(true), request);

        addViewBagItem("collapseMedicinePanel", true, request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderEditView(prescription, model, request);
        }

        prescriptionDrug = results.getBean();

        if ((medicine = pharmacyService.getMedicineById(prescriptionDrug.getMedicineId())) == null) {
            setSessionErrorMessage("Selected medicine does not exist.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return renderEditView(prescription, model, request);
        }

        if (pharmacyService.isMedicineOnPrescriptionList(prescription, medicine)) {
            setSessionErrorMessage(format("<strong>%s</strong> is already on the prescription list.",
                    templateService.escapeString(prescriptionDrug.getMedicineName())), request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return renderEditView(prescription, model, request);
        }

        if (prescriptionDrug.getQuantity() > medicine.getQuantity()) {
            setSessionErrorMessage(format(
                    "Prescription quantity <strong>%,d</strong> for <strong>%s (%s)</strong> exceeds medicine " +
                            "stock quantity (<strong>%,d</strong>).",
                    prescriptionDrug.getQuantity(),
                    templateService.escapeString(medicine.getName()),
                    templateService.escapeString(medicine.getGenericName()),
                    medicine.getQuantity()),
                    request
            );
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return renderEditView(prescription, model, request);
        }

        prescriptionDrug.setPrescriptionId(prescription.getId());
        pharmacyService.addMedicineToPrescription(prescriptionDrug);

        setSessionSuccessMessage("Medicine added to prescription.", request);
        return redirectToPrescription(prescription, response);
    }

    private PrescriptionDrug getSelectedPrescribedDrug(Request request, Prescription prescription) {
        Long id;
        PrescriptionDrug drug;

        if ((id = getNumericQueryParameter(request, "medicine-id", Long.class)) != null) {
            if ((drug = pharmacyService.getPrescribedMedicine(id, prescription.getId())) != null) {
                return drug;
            }
        }
        setSessionErrorMessage("Selected prescribed medicine does not exist.", request);
        return null;
    }

    @Action(path = "/:id/Medicine/:medicine-id/Delete", permission = AclPermission.WritePrescriptions)
    public String removeDrugFromPrescription(Request request, Response response) {
        Prescription prescription;
        PrescriptionDrug prescriptionDrug;

        // Can only remove filed prescriptions
        if ((prescription = getSelectedFiledPrescription(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((prescriptionDrug = getSelectedPrescribedDrug(request, prescription)) == null) {
            return redirectToPrescription(prescription, response);
        }

        pharmacyService.removeMedicineFromPrescription(prescriptionDrug);

        setSessionSuccessMessage("Medicine removed from prescription.", request);
        return redirectToPrescription(prescription, response);
    }
}
