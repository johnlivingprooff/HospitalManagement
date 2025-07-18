package app.controllers.billing;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.billing.bills.*;
import app.models.patient.Insurance;
import app.models.patient.PatientInfo;
import app.models.permission.AclPermission;
import app.services.billing.BillingService;
import app.services.patient.PatientService;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.time.LocalDateTime;

@RouteController(path = "/Hms/Billing")
public class BillingController extends Controller {

    @Inject
    private BillingService billingService;

    @Inject
    private PatientService patientService;

    private String redirectToPatientBills(PatientInfo info, Bill.BillStatus status, Response response) {
        return temporaryRedirect(withBaseUrl(Long.toString(info.getId()), status.name() + "Bills"), response);
    }

    private PatientInfo getSelectedPatient(Request request) {
        Long id;
        PatientInfo info;
        if ((id = getNumericQueryParameter(request, "patient-id", Long.class)) != null) {
            if ((info = patientService.findPatientInfoById(id)) != null) {
                return info;
            }
        }
        setSessionErrorMessage("Selected patient does not exist.", request);
        return null;
    }

    private GenericBill getSelectedBill(Request request, Long patientId) {
        Long id;
        GenericBill bill;

        if ((id = getNumericQueryParameter(request, "bill-id", Long.class)) != null) {
            if (patientId != null) {
                bill = billingService.getBillById(id, patientId);
            } else {
                bill = billingService.getBillById(id);
            }
            if (bill != null) {
                return bill;
            }
        }
        setSessionErrorMessage("Selected bill does not exist.", request);
        return null;
    }

    private String getBills(Request request, Bill.BillStatus status) {
        Model model;
        model = createModel(request);
        switch (status) {
            case Paid:
                model.put("bills", billingService.getPaidBills());
                break;
            case UnPaid:
                model.put("bills", billingService.getOutstandingBills());
                break;
        }
        model.put("title", status.title);
        model.put("status", status);
        return renderView("bills/list.html", model);
    }

    @Action(path = "/", permission = AclPermission.AccessBilling)
    public String getOutStandingBills(Request request, Response response) {
        return getBills(request, Bill.BillStatus.UnPaid);
    }

    @Action(path = "/History", permission = AclPermission.AccessBilling)
    public String getPaymentHistory(Request request, Response response) {
        Model model;
        model = createModel(request);
        model.put("payments", billingService.getRecentPayments());
        return renderView("bills/payment/recent.html", model);
    }

    @Action(path = "/Paid", permission = AclPermission.AccessBilling)
    public String getPaidBills(Request request, Response response) {
        return getBills(request, Bill.BillStatus.Paid);
    }

    private String getPatientBills(Request request, Response response, Bill.BillStatus status) {
        Model model;
        PatientInfo info;

        if ((info = getSelectedPatient(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel(request);
        model.put("info", info);
        switch (status) {
            case Paid:
                model.put("bills", billingService.getPatientsPaidBills(info.getId()));
                model.put("returnUrl", "/Hms/Billing/Paid");
                break;
            case UnPaid:
                model.put("bills", billingService.getPatientsUnpaidBills(info.getId()));
                model.put("returnUrl", "/Hms/Billing");
                break;
        }
        model.put("status", status);
        model.put("title", status.title);
        return renderView("bills/itemized/list.html", model);
    }

    @Action(path = "/:patient-id/UnPaidBills", permission = AclPermission.AccessBilling)
    public String getPatientUnpaidBills(Request request, Response response) {
        return getPatientBills(request, response, Bill.BillStatus.UnPaid);
    }

    @Action(path = "/:patient-id/PaidBills", permission = AclPermission.AccessBilling)
    public String getPatientPaidBills(Request request, Response response) {
        return getPatientBills(request, response, Bill.BillStatus.Paid);
    }

    private String payBillView(Model model, PatientInfo info, Insurance insurance, Bill bill, Request request, BillPayment.Payer payer) {
        String template;

        if (model == null) {
            model = createModel(request);
        }

        model.put("bill", bill);
        model.put("info", info);
        model.put("payer", payer);

        switch (payer) {
            case Self:
                template = "patient.html";
                break;
            case Insurance:
                model.put("insurance", insurance);
                template = "insurance.html";
                break;
            case Other:
                template = "other.html";
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment method specified.");
        }

        return renderView(format("bills/payment/%s", template), model);
    }

    private String payBill(Request request, Response response, BillPayment.Payer payer) {
        Bill bill;
        Model model;
        Insurance insurance;
        PatientInfo patientInfo;

        if ((patientInfo = getSelectedPatient(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((bill = getSelectedBill(request, patientInfo.getId())) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (bill.getStatus() == Bill.BillStatus.Paid) {
            setSessionErrorMessage("This bill is already fully paid.", request);
            return redirectToPatientBills(patientInfo, Bill.BillStatus.UnPaid, response);
        }

        model = createModel(request);

        if (payer == BillPayment.Payer.Insurance) {
            if ((insurance = patientService.getInsurance(patientInfo.getId())) == null) {
                setSessionErrorMessage("This patient does not have insurance details set.", request);
                return redirectToPatientBills(patientInfo, bill.getStatus(), response);
            }
        } else {
            insurance = null;
        }

        return payBillView(model, patientInfo, insurance, bill, request, payer);
    }

    @Action(path = "/:patient-id/Bills/:bill-id/PaymentFromPatient", permission = AclPermission.AccessBilling)
    public String paymentFromPatient(Request request, Response response) {
        return payBill(request, response, BillPayment.Payer.Self);
    }

    @Action(path = "/:patient-id/Bills/:bill-id/PaymentFromInsurance", permission = AclPermission.AccessBilling)
    public String paymentFromInsurance(Request request, Response response) {
        return payBill(request, response, BillPayment.Payer.Insurance);
    }

    @Action(path = "/:patient-id/Bills/:bill-id/PaymentFromOther", permission = AclPermission.AccessBilling)
    public String paymentFromOther(Request request, Response response) {
        return payBill(request, response, BillPayment.Payer.Other);
    }

    @Action(path = "/:patient-id/Bills/:bill-id/ProcessPatientPayment", method = HttpMethod.post, permission = AclPermission.AccessBilling)
    public String processPatientPayment(Request request, Response response) {
        Bill bill;
        Model model;
        PatientPayment payment;
        PatientInfo patientInfo;
        BillPayment billPayment;
        ValidationResults results;

        if ((patientInfo = getSelectedPatient(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((bill = getSelectedBill(request, patientInfo.getId())) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (bill.getStatus() == Bill.BillStatus.Paid) {
            setSessionErrorMessage("This bill is already fully paid.", request);
            return redirectToPatientBills(patientInfo, Bill.BillStatus.UnPaid, response);
        }

        results = validate(PatientPayment.class, Options.defaults().sticky(true).depth(2), request);

        if (!results.success()) {
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            copyErrorListToModel(model, results);
            return payBillView(model, patientInfo, null, bill, request, BillPayment.Payer.Self);
        }

        payment = results.getBean();

        if (payment.getAmount() > bill.getBalance()) {
            setSessionErrorMessage("Amount cannot not exceed balance.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return payBillView(model, patientInfo, null, bill, request, BillPayment.Payer.Self);
        }

        billPayment = new BillPayment();
        billPayment.setAmount(payment.getAmount());
        billPayment.setBillId(bill.getId());
        billPayment.setCreatedAt(LocalDateTime.now());
        billPayment.setDetails(payment.getDetails());
        billPayment.setPayer(BillPayment.Payer.Self);
        billPayment.setCreatedBy(getCurrentUser(request).getId());

        bill.setBalance(bill.getBalance() - payment.getAmount());
        bill.setPaid(bill.getPaid() + payment.getAmount());
        bill.setUpdatedAt(billPayment.getCreatedAt());
        bill.setStatus(bill.getBalance() == 0 ? Bill.BillStatus.Paid : Bill.BillStatus.UnPaid);

        billingService.addBillPayment(billPayment, bill);

        setSessionSuccessMessage("Payment from patient recorded successfully!", request);

        return redirectToPatientBills(patientInfo, bill.getStatus(), response);
    }

    @Action(path = "/:patient-id/Bills/:bill-id/ProcessInsurancePayment", method = HttpMethod.post, permission = AclPermission.AccessBilling)
    public String processInsurancePayment(Request request, Response response) {
        Bill bill;
        Model model;
        Insurance insurance;
        GenericPayment payment;
        PatientInfo patientInfo;
        BillPayment billPayment;
        ValidationResults results;

        if ((patientInfo = getSelectedPatient(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((bill = getSelectedBill(request, patientInfo.getId())) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (bill.getStatus() == Bill.BillStatus.Paid) {
            setSessionErrorMessage("This bill is already fully paid.", request);
            return redirectToPatientBills(patientInfo, Bill.BillStatus.UnPaid, response);
        }

        if ((insurance = patientService.getInsurance(patientInfo.getId())) == null) {
            setSessionErrorMessage("Cannot process this payment because this patient is missing insurance details in their file.", request);
            return redirectToPatientBills(patientInfo, Bill.BillStatus.UnPaid, response);
        }

        results = validate(GenericPayment.class, Options.defaults().sticky(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            copyErrorListToModel(model, results);
            return payBillView(model, patientInfo, insurance, bill, request, BillPayment.Payer.Insurance);
        }

        payment = results.getBean();

        if (payment.getAmount() > bill.getBalance()) {
            setSessionErrorMessage("Amount cannot not exceed balance.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return payBillView(model, patientInfo, insurance, bill, request, BillPayment.Payer.Insurance);
        }

        billPayment = new BillPayment();
        billPayment.setAmount(payment.getAmount());
        billPayment.setBillId(bill.getId());
        billPayment.setCreatedAt(LocalDateTime.now());
        billPayment.setDetails(getInsuranceDetails(insurance));
        billPayment.setPayer(BillPayment.Payer.Insurance);
        billPayment.setCreatedBy(getCurrentUser(request).getId());

        bill.setBalance(bill.getBalance() - payment.getAmount());
        bill.setPaid(bill.getPaid() + payment.getAmount());
        bill.setUpdatedAt(billPayment.getCreatedAt());
        bill.setStatus(bill.getBalance() == 0 ? Bill.BillStatus.Paid : Bill.BillStatus.UnPaid);

        billingService.addBillPayment(billPayment, bill);

        setSessionSuccessMessage("Payment from insurance recorded successfully!", request);

        return redirectToPatientBills(patientInfo, bill.getStatus(), response);
    }

    @Action(path = "/:patient-id/Bills/:bill-id/ProcessOtherPayment", method = HttpMethod.post, permission = AclPermission.AccessBilling)
    public String processOtherPayment(Request request, Response response) {
        Bill bill;
        Model model;
        OtherPayment payment;
        PatientInfo patientInfo;
        BillPayment billPayment;
        ValidationResults results;

        if ((patientInfo = getSelectedPatient(request)) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if ((bill = getSelectedBill(request, patientInfo.getId())) == null) {
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (bill.getStatus() == Bill.BillStatus.Paid) {
            setSessionErrorMessage("This bill is already fully paid.", request);
            return redirectToPatientBills(patientInfo, Bill.BillStatus.UnPaid, response);
        }

        results = validate(OtherPayment.class, Options.defaults().sticky(true).depth(3), request);

        if (!results.success()) {
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            copyErrorListToModel(model, results);
            return payBillView(model, patientInfo, null, bill, request, BillPayment.Payer.Other);
        }

        payment = results.getBean();

        if (payment.getAmount() > bill.getBalance()) {
            setSessionErrorMessage("Amount cannot not exceed balance.", request);
            model = createModel(request);
            copyRawPostDataToModel(model, results);
            return payBillView(model, patientInfo, null, bill, request, BillPayment.Payer.Other);
        }

        billPayment = new BillPayment();
        billPayment.setAmount(payment.getAmount());
        billPayment.setBillId(bill.getId());
        billPayment.setCreatedAt(LocalDateTime.now());
        billPayment.setDetails(payment.getDetails());
        billPayment.setPayer(BillPayment.Payer.Other);
        billPayment.setPhone(payment.getPhone());
        billPayment.setName(payment.getName());
        billPayment.setAddress(payment.getAddress());
        billPayment.setCreatedBy(getCurrentUser(request).getId());

        bill.setBalance(bill.getBalance() - payment.getAmount());
        bill.setPaid(bill.getPaid() + payment.getAmount());
        bill.setUpdatedAt(billPayment.getCreatedAt());
        bill.setStatus(bill.getBalance() == 0 ? Bill.BillStatus.Paid : Bill.BillStatus.UnPaid);

        billingService.addBillPayment(billPayment, bill);

        setSessionSuccessMessage("Payment from other individual/organization recorded successfully!", request);

        return redirectToPatientBills(patientInfo, bill.getStatus(), response);
    }

    private String getInsuranceDetails(Insurance insurance) {
        return format(
                "Paid with insurance coverage from vendor %s, %s, %s, with membership ID %s.",
                insurance.getInsurer(),
                insurance.getAddress(),
                insurance.getPhone(),
                insurance.getMembershipId()
        );
    }
}
