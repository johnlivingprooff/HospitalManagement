package app.controllers.pharmacy.dispensary;


import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.Contexts;
import app.models.account.Account;
import app.models.billing.bills.Bill;
import app.models.billing.bills.PrescriptionBill;
import app.models.permission.AclPermission;
import app.models.pharmacy.medicine.Prescription;
import app.services.audit.AuditService;
import app.services.billing.BillingService;
import app.services.pharmacy.PharmacyService;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Medicines/Dispensary")
public class DispensaryController extends Controller {

    @Inject
    private PharmacyService pharmacyService;

    @Inject
    private AuditService auditService;

    @Inject
    private BillingService billingService;

    private Prescription getSelectedPrescription(Request request, Prescription.Status status) {
        Long id;
        Prescription prescription;

        if ((id = getNumericQueryParameter(request, "id", Long.class)) != null) {
            if ((prescription = pharmacyService.getPrescriptionById(id, status)) != null) {
                return prescription;
            }
        }
        setSessionErrorMessage("Selected prescription does not exist.", request);
        return null;
    }

    private String getPrescriptions(Request request, Prescription.Status status) {
        Model model;
        model = createModel(request);
        switch (status) {
            case Pending:
                model.put("listTitle", "Pending Prescriptions");
                break;
            case Dispensed:
                model.put("listTitle", "Dispensed Prescriptions");
                break;
        }
        model.put("status", status);
        model.put("prescriptions", pharmacyService.getPrescriptionsByStatus(status));
        return renderView("pharmacy/medicine/dispensary/prescriptions.html", model);
    }

    @Action(path = "/", permission = AclPermission.DispenseMedicine)
    public String getPendingPrescriptions(Request request, Response response) {
        return getPrescriptions(request, Prescription.Status.Pending);
    }

    @Action(path = "/Dispensed", permission = AclPermission.DispenseMedicine)
    public String getDispensedPrescriptions(Request request, Response response) {
        return getPrescriptions(request, Prescription.Status.Dispensed);
    }

    @Action(path = "/:id/DispensedDetails", permission = AclPermission.DispenseMedicine)
    public String getDispensedPrescriptionDetails(Request request, Response response) {
        return getPrescriptionDetails(request, response, Prescription.Status.Dispensed);
    }

    @Action(path = "/:id/PendingDetails", permission = AclPermission.DispenseMedicine)
    public String getPendingPrescriptionDetails(Request request, Response response) {
        return getPrescriptionDetails(request, response, Prescription.Status.Pending);
    }

    private String getPrescriptionDetails(Request request, Response response, Prescription.Status status) {
        Model model;
        Prescription prescription;

        if ((prescription = getSelectedPrescription(request, status)) == null) {
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
                model.put("returnUrl", "/");
                break;
        }
        return renderView("pharmacy/medicine/dispensary/details.html", model);
    }

    @Action(path = "/Dispense", method = HttpMethod.post, permission = AclPermission.DispenseMedicine)
    public String dispenseMedicine(Request request, Response response) {
        Account account;
        double totalDue;
        ValidationResults results;
        Prescription prescription;
        PrescriptionBill prescriptionBill;

        results = validate(Prescription.class, Options.defaults().map(false).context(Contexts.FIND), request);

        if (!results.success()) {
            setSessionErrorMessage("Selected prescription not found.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        prescription = pharmacyService.getPrescriptionById(
                (long) results.getResults().get("id"),
                Prescription.Status.Pending
        );

        if (prescription == null) {
            setSessionErrorMessage("Selected prescription not found.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (prescription.getDrugs() == 0) {
            setSessionErrorMessage("There is nothing to dispense. Prescription has no medicine.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        // Make sure there is at least one medicine to dispense base on the calculated quantity.
        // That way, when creating an unpaid bill, it will factually be correct and reflect any outstanding balance.
        if (pharmacyService.getTotalPrescriptionMedicineQuantity(prescription) == 0) {
            setSessionErrorMessage("There is not enough medicine to dispense. Please check your medicine stock levels.", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        account = getCurrentUser(request);
        totalDue = pharmacyService.getPrescriptionTotalCost(prescription);

        prescription.setUpdatedAt(LocalDateTime.now());
        prescription.setStatus(Prescription.Status.Dispensed);
        prescription.setUpdaterId(account.getId());

        prescriptionBill = new PrescriptionBill();
        prescriptionBill.setPaid(0);
        prescriptionBill.setBalance(totalDue);
        prescriptionBill.setCreatedAt(LocalDateTime.now());
        prescriptionBill.setStatus(Bill.BillStatus.UnPaid);
        prescriptionBill.setPrescriptionId(prescription.getId());
        prescriptionBill.setPatientId(prescription.getPatientId());
        prescriptionBill.setUpdatedAt(prescriptionBill.getCreatedAt());

        pharmacyService.dispensePrescription(prescription);
        billingService.addPrescriptionBill(prescriptionBill);

        auditService.log(format("%s dispensed prescription %s.", account, prescription), request);
        setSessionSuccessMessage("Prescription dispensed.", request);
        return temporaryRedirect(withBaseUrl("Dispensed"), response);
    }
}
